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

    @PutMapping("/{id}")
    public ResponseEntity<ProcesoResponseDTO> actualizar(@PathVariable Long id, @RequestBody ProcesoRequestDTO dto) {
        return ResponseEntity.ok(procesoService.actualizarProceso(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        procesoService.eliminarProceso(id);
        return ResponseEntity.noContent().build();
    }
}