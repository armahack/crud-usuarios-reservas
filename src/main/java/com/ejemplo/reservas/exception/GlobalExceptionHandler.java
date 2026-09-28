package com.ejemplo.reservas.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<Map<String, Object>> manejarConflicto(ConflictoException ex) {
        return respuesta(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return respuesta(HttpStatus.BAD_REQUEST, "Error de validación", mensaje);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> manejarJsonInvalido(HttpMessageNotReadableException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "Solicitud inválida", "El cuerpo JSON no es válido");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> manejarIntegridad(DataIntegrityViolationException ex) {
        return respuesta(HttpStatus.CONFLICT, "Conflicto de datos",
            "La operación no se puede completar por una restricción de la base de datos");
    }

    private ResponseEntity<Map<String, Object>> respuesta(
        HttpStatus status,
        String error,
        String mensaje
    ) {
        return ResponseEntity.status(status).body(Map.of(
            "timestamp", LocalDateTime.now(),
            "status", status.value(),
            "error", error,
            "mensaje", mensaje
        ));
    }
}
