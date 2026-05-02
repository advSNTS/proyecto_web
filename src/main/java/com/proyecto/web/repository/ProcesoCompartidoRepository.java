package com.proyecto.web.repository;

import com.proyecto.web.entity.ProcesoCompartido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcesoCompartidoRepository extends JpaRepository<ProcesoCompartido, Long> {

    boolean existsByProceso_IdAndPool_IdAndEliminadoFalse(Long procesoId, Long poolId);

    List<ProcesoCompartido> findAllByProceso_IdAndEliminadoFalse(Long procesoId);

    Optional<ProcesoCompartido> findByProceso_IdAndPool_IdAndEliminadoFalse(Long procesoId, Long poolId);
}
