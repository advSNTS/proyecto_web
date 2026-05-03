package com.proyecto.web.repository;

import com.proyecto.web.entity.Lane;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LaneRepository extends JpaRepository<Lane, Long> {

    List<Lane> findAllByPool_Empresa_NitAndEliminadoFalse(String nit);

    List<Lane> findAllByPool_IdAndEliminadoFalse(Long poolId);

    Optional<Lane> findByIdAndPool_Empresa_NitAndEliminadoFalse(Long id, String nit);
}
