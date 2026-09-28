package com.ejemplo.reservas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
    @NotBlank @Size(max = 100) String nombre,
    @NotBlank @Email @Size(max = 120) String correo,
    @NotNull @PositiveOrZero Integer edad
) {
}
