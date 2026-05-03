package com.proyecto.web.controller;

import com.proyecto.web.dto.TareaIntegracionRequestDTO;
import com.proyecto.web.dto.TareaIntegracionResponseDTO;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.service.TareaIntegracionService;
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

import java.util.List;

@RestController
@RequestMapping("/api/tareas-integracion")
@RequiredArgsConstructor
public class TareaIntegracionController {

    private final TareaIntegracionService tareaIntegracionService;

    @PostMapping
    public ResponseEntity<TareaIntegracionResponseDTO> crear(
            @RequestBody TareaIntegracionRequestDTO dto,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(tareaIntegracionService.crear(requerirNit(nitEmpresa), dto));
    }

    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<TareaIntegracionResponseDTO>> porProceso(
            @PathVariable Long procesoId,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(tareaIntegracionService.listarPorProceso(requerirNit(nitEmpresa), procesoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TareaIntegracionResponseDTO> obtener(
            @PathVariable Long id,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(tareaIntegracionService.obtener(requerirNit(nitEmpresa), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TareaIntegracionResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody TareaIntegracionRequestDTO dto,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(tareaIntegracionService.actualizar(requerirNit(nitEmpresa), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @RequestParam(required = false) String nitEmpresa) {
        tareaIntegracionService.eliminar(requerirNit(nitEmpresa), id);
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
