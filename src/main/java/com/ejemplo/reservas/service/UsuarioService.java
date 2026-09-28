package com.ejemplo.reservas.service;

import com.ejemplo.reservas.dto.UsuarioRequest;
import com.ejemplo.reservas.dto.UsuarioResponse;
import com.ejemplo.reservas.entity.Usuario;
import com.ejemplo.reservas.exception.ConflictoException;
import com.ejemplo.reservas.exception.RecursoNoEncontradoException;
import com.ejemplo.reservas.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository repo;

    public UsuarioService(UsuarioRepository repo) {
        this.repo = repo;
    }

    public UsuarioResponse guardar(UsuarioRequest datos) {
        if (repo.existsByCorreo(datos.correo())) {
            throw new ConflictoException("Ya existe un usuario con ese correo");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(datos.nombre());
        usuario.setCorreo(datos.correo());
        usuario.setEdad(datos.edad());
        return convertir(repo.save(usuario));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return repo.findAll().stream().map(UsuarioService::convertir).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return convertir(obtenerUsuario(id));
    }

    public UsuarioResponse modificar(Long id, UsuarioRequest datos) {
        Usuario actual = obtenerUsuario(id);
        if (repo.existsByCorreoAndIdNot(datos.correo(), id)) {
            throw new ConflictoException("Ya existe otro usuario con ese correo");
        }

        actual.setNombre(datos.nombre());
        actual.setCorreo(datos.correo());
        actual.setEdad(datos.edad());
        return convertir(repo.save(actual));
    }

    public void eliminar(Long id) {
        repo.delete(obtenerUsuario(id));
    }

    private Usuario obtenerUsuario(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + id));
    }

    private static UsuarioResponse convertir(Usuario usuario) {
        return new UsuarioResponse(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getCorreo(),
            usuario.getEdad()
        );
    }
}
