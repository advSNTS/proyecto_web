package com.proyecto.web.repository;

import com.proyecto.web.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    List<Empleado> findAllByDeletedFalse();

    Optional<Empleado> findByIdAndDeletedFalse(Long id);

    List<Empleado> findAllByEmpresa_NitAndDeletedFalse(String nit);
}