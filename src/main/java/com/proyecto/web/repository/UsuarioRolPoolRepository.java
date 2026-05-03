package com.proyecto.web.repository;

import com.proyecto.web.entity.UsuarioRolPool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRolPoolRepository extends JpaRepository<UsuarioRolPool, Long> {

    List<UsuarioRolPool> findAllByEmpleado_IdAndEliminadoFalse(Long empleadoId);
}
