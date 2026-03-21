package com.proyecto.web.controller;

import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.service.ProcesoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/procesos")
@RequiredArgsConstructor
public class ProcesoController {

    private final ProcesoService procesoService;

    @PostMapping
    public ResponseEntity<ProcesoResponseDTO> crear(@RequestBody ProcesoRequestDTO dto) {
        return ResponseEntity.ok(procesoService.crearProceso(dto));
    }

    @GetMapping
    public ResponseEntity<List<ProcesoResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(procesoService.obtenerProcesos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcesoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(procesoService.obtenerProceso(id));
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<ProcesoResponseDTO>> obtenerPorCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(procesoService.obtenerPorCategoria(categoria));
    }

    // idEmpleado es opcional — si no se manda, el historial queda sin responsable
    @PutMapping("/{id}")
    public ResponseEntity<ProcesoResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody ProcesoRequestDTO dto,
            @RequestParam(required = false) Long idEmpleado) {
        return ResponseEntity.ok(procesoService.actualizarProceso(id, dto, idEmpleado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @RequestParam(required = false) Long idEmpleado) {
        procesoService.eliminarProceso(id, idEmpleado);
        return ResponseEntity.noContent().build();
    }

    // Consultar el historial de cambios de un proceso
    @GetMapping("/{id}/historial")
    public ResponseEntity<?> historial(@PathVariable Long id) {
        return ResponseEntity.ok(procesoService.obtenerHistorialDeProceso(id));
    }
}