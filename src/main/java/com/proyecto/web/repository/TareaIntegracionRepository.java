package com.proyecto.web.repository;

import com.proyecto.web.entity.TareaIntegracion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TareaIntegracionRepository extends JpaRepository<TareaIntegracion, Long> {

    List<TareaIntegracion> findAllByProceso_IdAndEliminadoFalse(Long procesoId);
}
