package com.ejemplo.reservas.dto;

import java.time.LocalDateTime;

public record ReservaResponse(
    Long id,
    LocalDateTime fechaHora,
    Integer cantidadPersonas,
    String observaciones,
    Long usuarioId
) {
}
