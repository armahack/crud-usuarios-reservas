# API CRUD de usuarios y reservas

API REST con Spring Boot, Spring Data JPA y MySQL. Los datos se conservan al reiniciar la aplicación; Hibernate crea o actualiza las tablas existentes.

## Requisitos

- Java 17 o superior
- Maven 3.6 o superior
- MySQL en ejecución

La configuración predeterminada se conecta a `localhost:3306`, con usuario `root`, contraseña vacía y crea la base `reservas_db` si no existe. Se puede configurar por entorno:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/reservas_db?createDatabaseIfNotExist=true&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "tu_password"
```

Iniciar la aplicación desde la raíz del proyecto:

```powershell
mvn spring-boot:run
```

Ejecutar pruebas:

```powershell
mvn test
```

## Endpoints

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | `/api/usuarios` | Crear usuario |
| GET | `/api/usuarios` | Listar usuarios |
| GET | `/api/usuarios/{id}` | Buscar usuario |
| PUT | `/api/usuarios/{id}` | Modificar usuario |
| DELETE | `/api/usuarios/{id}` | Eliminar usuario y sus reservas |
| GET | `/api/usuarios/{id}/reservas` | Listar las reservas de un usuario |
| POST | `/api/reservas` | Crear reserva |
| GET | `/api/reservas` | Listar reservas |
| GET | `/api/reservas?usuarioId={id}` | Filtrar reservas por usuario |
| GET | `/api/reservas/{id}` | Buscar reserva |
| PUT | `/api/reservas/{id}` | Modificar reserva |
| DELETE | `/api/reservas/{id}` | Eliminar reserva |

### Ejemplos de cuerpos JSON

Crear o modificar usuario:

```json
{
  "nombre": "Ana Pérez",
  "correo": "ana@example.com",
  "edad": 30
}
```

Crear o modificar reserva:

```json
{
  "fechaHora": "2026-10-12T19:30:00",
  "cantidadPersonas": 4,
  "observaciones": "Mesa junto a la ventana",
  "usuarioId": 1
}
```

Las reservas reciben y devuelven `usuarioId`, sin exponer la entidad asociada ni crear ciclos en el JSON. Los errores de validación responden con HTTP 400, los recursos inexistentes con HTTP 404 y los conflictos de correo con HTTP 409.

## Verificación de datos y capturas

Importar `postman/Reservas.postman_collection.json` en Postman. La colección guarda automáticamente los ID al crear recursos; ejecutar primero **Crear usuario** y luego **Crear reserva**. Para comprobar la persistencia, detener y volver a iniciar la aplicación y consultar los mismos recursos. En MySQL, las tablas `usuarios` y `reservas` se pueden revisar con:

```sql
USE reservas_db;
SELECT * FROM usuarios;
SELECT * FROM reservas;
```

Las capturas de Postman y de las tablas se deben tomar en el entorno donde se ejecute la aplicación y MySQL.
