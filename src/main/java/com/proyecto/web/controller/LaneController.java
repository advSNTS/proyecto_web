package com.proyecto.web.controller;

import com.proyecto.web.dto.LaneRequestDTO;
import com.proyecto.web.dto.LaneResponseDTO;
import com.proyecto.web.service.LaneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/lanes")
@RequiredArgsConstructor
public class LaneController {

    private final LaneService laneService;

    @PostMapping
    public ResponseEntity<LaneResponseDTO> crear(@RequestBody LaneRequestDTO dto) {
        return ResponseEntity.ok(laneService.crear(dto));
    }

    @GetMapping
    public ResponseEntity<List<LaneResponseDTO>> listar() {
        return ResponseEntity.ok(laneService.listarPorEmpresa());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<List<LaneResponseDTO>> listarAdministrativo() {
        return ResponseEntity.ok(laneService.listarTodasPorEmpresa());
    }

    @GetMapping("/pool/{poolId}")
    public ResponseEntity<List<LaneResponseDTO>> listarPorPool(@PathVariable Long poolId) {
        return ResponseEntity.ok(laneService.listarPorPool(poolId));
    }

    @GetMapping("/admin/pool/{poolId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<List<LaneResponseDTO>> listarPorPoolAdministrativo(@PathVariable Long poolId) {
        return ResponseEntity.ok(laneService.listarTodasPorPool(poolId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaneResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(laneService.obtener(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LaneResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody LaneRequestDTO dto) {
        return ResponseEntity.ok(laneService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        laneService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
