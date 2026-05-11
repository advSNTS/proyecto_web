package com.proyecto.web.controller;

import com.proyecto.web.dto.LaneRequestDTO;
import com.proyecto.web.dto.LaneResponseDTO;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.service.LaneService;
import com.proyecto.web.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<LaneResponseDTO> crear(
            @RequestBody LaneRequestDTO dto,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(laneService.crear(requerirNit(nitEmpresa), dto));
    }

    @GetMapping
    public ResponseEntity<List<LaneResponseDTO>> listar(@RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(laneService.listarPorEmpresa(requerirNit(nitEmpresa)));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<List<LaneResponseDTO>> listarAdministrativo(@RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(laneService.listarTodasPorEmpresa(requerirNit(nitEmpresa)));
    }

    @GetMapping("/pool/{poolId}")
    public ResponseEntity<List<LaneResponseDTO>> listarPorPool(
            @PathVariable Long poolId,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(laneService.listarPorPool(requerirNit(nitEmpresa), poolId));
    }

    @GetMapping("/admin/pool/{poolId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<List<LaneResponseDTO>> listarPorPoolAdministrativo(
            @PathVariable Long poolId,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(laneService.listarTodasPorPool(requerirNit(nitEmpresa), poolId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaneResponseDTO> obtener(
            @PathVariable Long id,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(laneService.obtener(requerirNit(nitEmpresa), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LaneResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody LaneRequestDTO dto,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(laneService.actualizar(requerirNit(nitEmpresa), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @RequestParam(required = false) String nitEmpresa) {
        laneService.eliminar(requerirNit(nitEmpresa), id);
        return ResponseEntity.noContent().build();
    }

    private String requerirNit(String nitEmpresa) {
        String nit = SecurityUtils.resolverNitEmpresa(nitEmpresa);
        if (nit == null || nit.isBlank()) {
            throw new BusinessException("nitEmpresa es obligatorio", HttpStatus.BAD_REQUEST);
        }
        return nit;
    }
}
