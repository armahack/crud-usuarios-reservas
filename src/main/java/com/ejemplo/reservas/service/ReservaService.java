package com.ejemplo.reservas.service;

import com.ejemplo.reservas.dto.ReservaRequest;
import com.ejemplo.reservas.dto.ReservaResponse;
import com.ejemplo.reservas.entity.Reserva;
import com.ejemplo.reservas.entity.Usuario;
import com.ejemplo.reservas.exception.RecursoNoEncontradoException;
import com.ejemplo.reservas.repository.ReservaRepository;
import com.ejemplo.reservas.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReservaService {

    private final ReservaRepository repo;
    private final UsuarioRepository usuarioRepo;

    public ReservaService(ReservaRepository repo, UsuarioRepository usuarioRepo) {
        this.repo = repo;
        this.usuarioRepo = usuarioRepo;
    }

    public ReservaResponse guardar(ReservaRequest datos) {
        Usuario usuario = obtenerUsuario(datos.usuarioId());
        Reserva reserva = new Reserva();
        aplicarDatos(reserva, datos);
        usuario.agregarReserva(reserva);
        return convertir(repo.save(reserva));
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarTodas() {
        return repo.findAll().stream().map(ReservaService::convertir).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarPorUsuario(Long usuarioId) {
        if (!usuarioRepo.existsById(usuarioId)) {
            throw new RecursoNoEncontradoException("No existe el usuario con id " + usuarioId);
        }
        return repo.findByUsuarioId(usuarioId).stream().map(ReservaService::convertir).toList();
    }

    @Transactional(readOnly = true)
    public ReservaResponse buscarPorId(Long id) {
        return convertir(obtenerReserva(id));
    }

    public ReservaResponse modificar(Long id, ReservaRequest datos) {
        Reserva actual = obtenerReserva(id);
        Usuario nuevoUsuario = obtenerUsuario(datos.usuarioId());
        Usuario usuarioAnterior = actual.getUsuario();
        if (!usuarioAnterior.getId().equals(nuevoUsuario.getId())) {
            usuarioAnterior.quitarReserva(actual);
            nuevoUsuario.agregarReserva(actual);
        }
        aplicarDatos(actual, datos);
        return convertir(repo.save(actual));
    }

    public void eliminar(Long id) {
        Reserva reserva = obtenerReserva(id);
        reserva.getUsuario().quitarReserva(reserva);
        repo.delete(reserva);
    }

    private Usuario obtenerUsuario(Long id) {
        return usuarioRepo.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("El usuario indicado no existe"));
    }

    private Reserva obtenerReserva(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe la reserva con id " + id));
    }

    private static void aplicarDatos(Reserva reserva, ReservaRequest datos) {
        reserva.setFechaHora(datos.fechaHora());
        reserva.setCantidadPersonas(datos.cantidadPersonas());
        reserva.setObservaciones(datos.observaciones());
    }

    private static ReservaResponse convertir(Reserva reserva) {
        return new ReservaResponse(
            reserva.getId(),
            reserva.getFechaHora(),
            reserva.getCantidadPersonas(),
            reserva.getObservaciones(),
            reserva.getUsuario().getId()
        );
    }
}
