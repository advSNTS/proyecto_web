package com.proyecto.web.repository;

import com.proyecto.web.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, String>{
    
}
