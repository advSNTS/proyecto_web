package com.proyecto.web.repository;

import com.proyecto.web.entity.MensajeThrow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeThrowRepository extends JpaRepository<MensajeThrow, Long> {

    List<MensajeThrow> findAllByProceso_IdAndEliminadoFalse(Long procesoId);
}
