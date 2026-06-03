package com.proyecto.web.repository;

import com.proyecto.web.entity.Proceso;
import com.proyecto.web.enums.EstadoProceso;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcesoRepository extends JpaRepository<Proceso, Long> {

    @EntityGraph(attributePaths = {"empresa", "pool"})
    List<Proceso> findAllByEstadoNot(EstadoProceso estado);

    @EntityGraph(attributePaths = {"empresa", "pool"})
    Optional<Proceso> findByIdAndEstadoNot(Long id, EstadoProceso estado);

    @EntityGraph(attributePaths = {"empresa", "pool"})
    List<Proceso> findAllByCategoriaAndEstadoNot(String categoria, EstadoProceso estado);

    @EntityGraph(attributePaths = {"empresa", "pool"})
    List<Proceso> findAllByCategoriaAndEstadoNotOrderByIdDesc(String categoria, EstadoProceso estado);

    @EntityGraph(attributePaths = {"empresa", "pool"})
    List<Proceso> findAllByEmpresa_NitAndEstadoNot(String nit, EstadoProceso estado);

    @EntityGraph(attributePaths = {"empresa", "pool"})
    List<Proceso> findAllByEmpresa_NitAndEstadoNotOrderByIdDesc(String nit, EstadoProceso estado);

    @EntityGraph(attributePaths = {"empresa", "pool"})
    List<Proceso> findAllByEmpresa_NitAndPool_IdAndEstadoNot(String nit, Long poolId, EstadoProceso estado);

    @EntityGraph(attributePaths = {"empresa", "pool"})
    List<Proceso> findAllByEmpresa_NitAndPool_IdAndEstadoNotOrderByIdDesc(String nit, Long poolId, EstadoProceso estado);

    @EntityGraph(attributePaths = {"empresa", "pool"})
    Optional<Proceso> findByIdAndEmpresa_NitAndEstadoNot(Long id, String nit, EstadoProceso estado);

    long countByEmpresa_Nit(String nit);

    long countByEmpresa_NitAndEstado(String nit, EstadoProceso estado);
}