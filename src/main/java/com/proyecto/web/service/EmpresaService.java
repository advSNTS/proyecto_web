package com.proyecto.web.service;

import com.proyecto.web.entity.Empresa;
import com.proyecto.web.repository.EmpresaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpresaService {
    
    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    public List<Empresa> listar() {
        return empresaRepository.findAll();
    }

    public Empresa crear(Empresa empresa) {
        return empresaRepository.save(empresa);
    }

    public Empresa buscarPorNit(String nit) {
        return empresaRepository.findById(nit).orElse(null);
    }

    public void eliminar(String nit) {
        empresaRepository.deleteById(nit);
    }
}
