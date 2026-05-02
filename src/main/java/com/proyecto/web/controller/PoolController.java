package com.proyecto.web.controller;

import com.proyecto.web.dto.PoolResponseDTO;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.service.PoolService;
import com.proyecto.web.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pools")
@RequiredArgsConstructor
public class PoolController {

    private final PoolService poolService;

    @GetMapping
    public ResponseEntity<List<PoolResponseDTO>> listar(@RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(poolService.listarPorEmpresa(requerirNit(nitEmpresa)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PoolResponseDTO> obtener(
            @PathVariable Long id,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(poolService.obtener(id, requerirNit(nitEmpresa)));
    }

    private String requerirNit(String nitEmpresa) {
        String nit = SecurityUtils.resolverNitEmpresa(nitEmpresa);
        if (nit == null || nit.isBlank()) {
            throw new BusinessException("nitEmpresa es obligatorio", HttpStatus.BAD_REQUEST);
        }
        return nit;
    }
}
