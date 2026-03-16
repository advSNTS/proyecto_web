package com.proyecto.web.controller;

import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.EmpresaResponseDTO;
import com.proyecto.web.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @PostMapping
    public EmpresaResponseDTO crearEmpresa(@RequestBody EmpresaRequestDTO dto) {
        return empresaService.crearEmpresa(dto);
    }

    @GetMapping
    public List<EmpresaResponseDTO> listarEmpresas() {
        return empresaService.obtenerEmpresas();
    }

    @GetMapping("/{nit}")
    public EmpresaResponseDTO obtenerEmpresa(@PathVariable String nit) {
        return empresaService.obtenerEmpresa(nit);
    }
}