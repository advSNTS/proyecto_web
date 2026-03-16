package com.proyecto.web.repository;

import com.proyecto.web.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, String> {
    List<Empresa> findAllByDeletedFalse();

    Optional<Empresa> findByNitAndDeletedFalse(String nit);
}