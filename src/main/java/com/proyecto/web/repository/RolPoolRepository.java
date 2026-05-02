package com.proyecto.web.repository;

import com.proyecto.web.entity.RolPool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolPoolRepository extends JpaRepository<RolPool, Long> {

    List<RolPool> findAllByPool_Empresa_NitAndEliminadoFalse(String nit);

    Optional<RolPool> findByIdAndPool_Empresa_NitAndEliminadoFalse(Long id, String nit);
}
