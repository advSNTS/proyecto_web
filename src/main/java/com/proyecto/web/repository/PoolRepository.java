package com.proyecto.web.repository;

import com.proyecto.web.entity.Pool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PoolRepository extends JpaRepository<Pool, Long> {

    Optional<Pool> findByEmpresa_NitAndEsDefaultTrueAndEliminadoFalse(String nit);

    List<Pool> findAllByEmpresa_NitAndEliminadoFalse(String nit);

    Optional<Pool> findByIdAndEmpresa_NitAndEliminadoFalse(Long id, String nit);
}
