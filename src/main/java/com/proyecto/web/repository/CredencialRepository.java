package com.proyecto.web.repository;

import com.proyecto.web.entity.Credencial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredencialRepository extends JpaRepository<Credencial, Long> {

    Optional<Credencial> findByCorreo(String correo);

    Optional<Credencial> findByVerificationToken(String verificationToken);
}
