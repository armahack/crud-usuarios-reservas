package com.ejemplo.reservas.repository;

import com.ejemplo.reservas.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByCorreo(String correo);

    boolean existsByCorreoAndIdNot(String correo, Long id);
}
