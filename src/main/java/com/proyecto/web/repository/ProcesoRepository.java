package com.proyecto.web.repository;

import com.proyecto.web.entity.Proceso;
import com.proyecto.web.enums.EstadoProceso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcesoRepository extends JpaRepository<Proceso, Long> {

    List<Proceso> findAllByEstadoNot(EstadoProceso estado);

    Optional<Proceso> findByIdAndEstadoNot(Long id, EstadoProceso estado);

    List<Proceso> findAllByCategoriaAndEstadoNot(String categoria, EstadoProceso estado);

    List<Proceso> findAllByEmpresa_NitAndEstadoNot(String nit, EstadoProceso estado);

    List<Proceso> findAllByEmpresa_NitAndPool_IdAndEstadoNot(String nit, Long poolId, EstadoProceso estado);

    Optional<Proceso> findByIdAndEmpresa_NitAndEstadoNot(Long id, String nit, EstadoProceso estado);
}
 
