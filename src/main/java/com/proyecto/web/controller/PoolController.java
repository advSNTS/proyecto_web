package com.proyecto.web.controller;

import com.proyecto.web.dto.PoolRequestDTO;
import com.proyecto.web.dto.PoolResponseDTO;
import com.proyecto.web.service.PoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public ResponseEntity<List<PoolResponseDTO>> listar() {
        return ResponseEntity.ok(poolService.listarPorEmpresa());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PoolResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(poolService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<PoolResponseDTO> crear(@RequestBody PoolRequestDTO dto) {
        return ResponseEntity.ok(poolService.crear(dto));
    }
}
