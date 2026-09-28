package com.ejemplo.reservas.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ReservaRequest(
    @NotNull LocalDateTime fechaHora,
    @NotNull @Positive Integer cantidadPersonas,
    @Size(max = 255) String observaciones,
    @NotNull @Positive Long usuarioId
) {
}
