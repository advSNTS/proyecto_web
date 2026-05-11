package com.proyecto.web.repository;

import com.proyecto.web.entity.Lane;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LaneRepository extends JpaRepository<Lane, Long> {

    List<Lane> findAllByPool_Empresa_NitAndEliminadoFalse(String nit);

    List<Lane> findAllByPool_Empresa_NitAndEliminadoFalseAndRolProceso_IdIn(String nit, Collection<Long> rolIds);

    List<Lane> findAllByPool_IdAndEliminadoFalse(Long poolId);

    List<Lane> findAllByPool_IdAndEliminadoFalseAndRolProceso_IdIn(Long poolId, Collection<Long> rolIds);

    Optional<Lane> findByIdAndPool_Empresa_NitAndEliminadoFalse(Long id, String nit);

    Optional<Lane> findByIdAndPool_Empresa_NitAndEliminadoFalseAndRolProceso_IdIn(
            Long id, String nit, Collection<Long> rolIds);
}
