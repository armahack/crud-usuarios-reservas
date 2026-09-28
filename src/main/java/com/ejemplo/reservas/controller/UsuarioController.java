package com.ejemplo.reservas.controller;

import com.ejemplo.reservas.dto.ReservaResponse;
import com.ejemplo.reservas.dto.UsuarioRequest;
import com.ejemplo.reservas.dto.UsuarioResponse;
import com.ejemplo.reservas.service.ReservaService;
import com.ejemplo.reservas.service.UsuarioService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;
    private final ReservaService reservaService;

    public UsuarioController(UsuarioService service, ReservaService reservaService) {
        this.service = service;
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(usuario));
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/{id}/reservas")
    public List<ReservaResponse> listarReservas(@PathVariable Long id) {
        return reservaService.listarPorUsuario(id);
    }

    @PutMapping("/{id}")
    public UsuarioResponse modificar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest usuario) {
        return service.modificar(id, usuario);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
