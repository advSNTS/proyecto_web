package com.proyecto.web.repository;

import com.proyecto.web.entity.Credencial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredencialRepository extends JpaRepository<Credencial, Long> {

    Optional<Credencial> findByCorreoAndContrasena(String correo, String contrasena);

    Optional<Credencial> findByCorreo(String correo);

    Optional<Credencial> findByCorreoIgnoreCase(String correo);

    Optional<Credencial> findByTokenVerificacion(String tokenVerificacion);

    boolean existsByCorreo(String correo);

    boolean existsByCorreoIgnoreCase(String correo);
}