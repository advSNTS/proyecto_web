package com.proyecto.web.repository;

import com.proyecto.web.entity.EmpleadoRolSistema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmpleadoRolSistemaRepository extends JpaRepository<EmpleadoRolSistema, Long> {

    List<EmpleadoRolSistema> findAllByEmpleado_IdAndEliminadoFalse(Long empleadoId);

    boolean existsByEmpleado_IdAndEmpresa_NitAndTipoRolAndEliminadoFalse(
            Long empleadoId, String nit, com.proyecto.web.enums.TipoRolSistema tipoRol);
}
