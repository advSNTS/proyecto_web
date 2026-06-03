package com.proyecto.web.repository;

import com.proyecto.web.dto.HistorialProcesoResponseDTO;
import com.proyecto.web.entity.HistorialProceso;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistorialProcesoRepository extends JpaRepository<HistorialProceso, Long> {

    List<HistorialProceso> findAllByProceso_IdOrderByFechaCambioDesc(Long idProceso);

    // Cambios hechos por un empleado específico
    List<HistorialProceso> findAllByEmpleado_IdOrderByFechaCambioDesc(Long idEmpleado);

    List<HistorialProceso> findAllByProceso_IdAndTipoAccionOrderByFechaCambioDesc(Long idProceso, String tipoAccion);

    long countByProceso_Id(Long idProceso);

    @Query("""
            select new com.proyecto.web.dto.HistorialProcesoResponseDTO(
                h.id,
                p.id,
                p.nombre,
                e.id,
                e.nombre,
                h.valorAnterior,
                h.valorNuevo,
                h.fechaCambio,
                h.tipoAccion
            )
            from HistorialProceso h
            join h.proceso p
            left join h.empleado e
            where p.id = :idProceso
              and p.empresa.nit = :nitEmpresa
              and p.estado <> com.proyecto.web.enums.EstadoProceso.INACTIVO
            order by h.fechaCambio desc
            """)
    List<HistorialProcesoResponseDTO> findDetallePorProcesoYEmpresa(
            @Param("idProceso") Long idProceso,
            @Param("nitEmpresa") String nitEmpresa);

    @Query("""
            select new com.proyecto.web.dto.HistorialProcesoResponseDTO(
                h.id,
                p.id,
                p.nombre,
                e.id,
                e.nombre,
                h.valorAnterior,
                h.valorNuevo,
                h.fechaCambio,
                h.tipoAccion
            )
            from HistorialProceso h
            join h.proceso p
            left join h.empleado e
            where p.id = :idProceso
              and p.empresa.nit = :nitEmpresa
              and p.estado <> com.proyecto.web.enums.EstadoProceso.INACTIVO
            order by h.fechaCambio desc
            """)
    List<HistorialProcesoResponseDTO> findDetallePorProcesoYEmpresa(
            @Param("idProceso") Long idProceso,
            @Param("nitEmpresa") String nitEmpresa,
            Pageable pageable);

    @Query("""
            select h
            from HistorialProceso h
            join fetch h.proceso p
            left join fetch h.empleado e
            where p.empresa.nit = :nitEmpresa
            order by h.fechaCambio desc
            """)
    List<HistorialProceso> findRecientesPorEmpresa(
            @Param("nitEmpresa") String nitEmpresa,
            Pageable pageable);
}