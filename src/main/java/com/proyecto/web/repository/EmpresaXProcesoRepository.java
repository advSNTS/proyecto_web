package com.proyecto.web.repository;

import com.proyecto.web.entity.EmpresaProcesoId;
import com.proyecto.web.entity.EmpresaXProceso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpresaXProcesoRepository extends JpaRepository<EmpresaXProceso, EmpresaProcesoId> {

    // Todos los procesos activos de una empresa
    List<EmpresaXProceso> findAllByEmpresa_NitAndDeletedFalse(String nit);

    // Todas las empresas activas que tienen un proceso
    List<EmpresaXProceso> findAllByProceso_IdAndDeletedFalse(Long idProceso);

    // Todos los procesos donde una empresa es owner
    List<EmpresaXProceso> findAllByEmpresaOwner_NitAndDeletedFalse(String nitOwner);

    // Buscar relación puntual activa
    Optional<EmpresaXProceso> findByEmpresa_NitAndProceso_IdAndDeletedFalse(String nit, Long idProceso);

    // Validar duplicado antes de asignar
    boolean existsByEmpresa_NitAndProceso_IdAndDeletedFalse(String nit, Long idProceso);
}