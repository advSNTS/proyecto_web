package com.proyecto.web.repository;

import com.proyecto.web.entity.MensajeExterno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeExternoRepository extends JpaRepository<MensajeExterno, Long> {

    List<MensajeExterno> findAllByEliminadoFalse();
}
