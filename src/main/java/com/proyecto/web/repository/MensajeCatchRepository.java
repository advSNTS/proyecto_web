package com.proyecto.web.repository;

import com.proyecto.web.entity.MensajeCatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeCatchRepository extends JpaRepository<MensajeCatch, Long> {

    List<MensajeCatch> findAllByProceso_IdAndEliminadoFalse(Long procesoId);
}
