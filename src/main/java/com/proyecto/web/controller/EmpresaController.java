package com.proyecto.web.controller;

import com.proyecto.web.entity.Empresa;
import com.proyecto.web.service.EmpresaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/empresas")
public class EmpresaController {
    
    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    public List<Empresa> listar() {
        return empresaService.listar();
    }

    @PostMapping
    public Empresa crear(@RequestBody Empresa empresa) {
        return empresaService.crear(empresa);
    }

    @GetMapping("/{nit}")
    public Empresa buscar(@PathVariable String nit) {
        return empresaService.buscarPorNit(nit);
    }

    @DeleteMapping("/{nit}")
    public void eliminar(@PathVariable String nit) {
        empresaService.eliminar(nit);
    }
}
