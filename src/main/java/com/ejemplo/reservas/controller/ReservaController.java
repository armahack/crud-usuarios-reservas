package com.ejemplo.reservas.controller;

import com.ejemplo.reservas.dto.ReservaRequest;
import com.ejemplo.reservas.dto.ReservaResponse;
import com.ejemplo.reservas.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService service;

    public ReservaController(ReservaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody ReservaRequest reserva) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(reserva));
    }

    @GetMapping
    public List<ReservaResponse> listar(@RequestParam(required = false) Long usuarioId) {
        return usuarioId == null ? service.listarTodas() : service.listarPorUsuario(usuarioId);
    }

    @GetMapping("/{id}")
    public ReservaResponse buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ReservaResponse modificar(@PathVariable Long id, @Valid @RequestBody ReservaRequest reserva) {
        return service.modificar(id, reserva);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
