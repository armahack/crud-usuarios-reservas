$ErrorActionPreference = "Stop"

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw "No se encontro Java. Instala Java 17 o superior y agregalo al PATH."
}
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    throw "No se encontro Maven. Instala Maven y agregalo al PATH."
}

$password = Read-Host "Contrasena de MySQL para el usuario root" -AsSecureString
if ($password.Length -eq 0) {
    throw "La contrasena no puede estar vacia."
}
$passwordPointer = [IntPtr]::Zero
try {
    $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    $server = Start-Process -FilePath $env:ComSpec `
        -ArgumentList '/d /s /c "mvn spring-boot:run"' `
        -WorkingDirectory $PSScriptRoot `
        -NoNewWindow `
        -PassThru
}
finally {
    if ($passwordPointer -ne [IntPtr]::Zero) {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    }
    $password.Dispose()
    Remove-Item Env:DB_PASSWORD -ErrorAction SilentlyContinue
}

Write-Host "Iniciando Spring Boot y esperando a MySQL..."
$deadline = (Get-Date).AddMinutes(5)
$ready = $false
while ((Get-Date) -lt $deadline -and -not $server.HasExited) {
    $client = [System.Net.Sockets.TcpClient]::new()
    try {
        $connection = $client.BeginConnect("127.0.0.1", 8080, $null, $null)
        if ($connection.AsyncWaitHandle.WaitOne(500) -and $client.Connected) {
            $ready = $true
            break
        }
    }
    catch [System.Net.Sockets.SocketException] {
    }
    finally {
        $client.Dispose()
    }
    Start-Sleep -Seconds 1
}

if (-not $ready) {
    if ($server.HasExited) {
        $server.Refresh()
        throw "Spring Boot finalizo con codigo $($server.ExitCode). Revisa el error de la consola."
    }
    throw "La API no respondio en cinco minutos. Revisa la consola y el estado de MySQL."
}

Write-Host ""
Write-Host "La aplicacion esta lista:"
Write-Host "  En esta computadora: http://localhost:8080"
$addresses = [System.Net.Dns]::GetHostAddresses([System.Net.Dns]::GetHostName()) |
    Where-Object {
        $_.AddressFamily -eq [System.Net.Sockets.AddressFamily]::InterNetwork -and
        -not [System.Net.IPAddress]::IsLoopback($_)
    } |
    Select-Object -ExpandProperty IPAddressToString -Unique
foreach ($address in $addresses) {
    Write-Host "  En la red local:     http://${address}:8080"
}
Write-Host "La API y el panel quedan disponibles en la red local."
Write-Host "La consola sigue abierta mientras la API esta en ejecucion. Pulsa Ctrl+C para detenerla."
Start-Process "http://localhost:8080"

$server.WaitForExit()
exit $server.ExitCode
