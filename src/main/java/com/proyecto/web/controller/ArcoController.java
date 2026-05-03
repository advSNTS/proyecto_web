package com.proyecto.web.controller;

import com.proyecto.web.dto.ArcoRequestDTO;
import com.proyecto.web.dto.ArcoResponseDTO;
import com.proyecto.web.service.ArcoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/arcos")
@RequiredArgsConstructor
public class ArcoController {

    private final ArcoService arcoService;

    @PostMapping
    public ResponseEntity<ArcoResponseDTO> crear(@RequestBody ArcoRequestDTO dto) {
        return ResponseEntity.ok(arcoService.crearArco(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArcoResponseDTO> porId(@PathVariable Long id) {
        return ResponseEntity.ok(arcoService.obtenerArco(id));
    }

    // Todos los arcos de un proceso
    @GetMapping("/proceso/{idProceso}")
    public ResponseEntity<List<ArcoResponseDTO>> porProceso(@PathVariable Long idProceso) {
        return ResponseEntity.ok(arcoService.obtenerPorProceso(idProceso));
    }

    // Arcos que salen de un nodo
    @GetMapping("/origen/{nodoId}")
    public ResponseEntity<List<ArcoResponseDTO>> salientes(@PathVariable Long nodoId) {
        return ResponseEntity.ok(arcoService.obtenerSalientesDe(nodoId));
    }

    // Arcos que llegan a un nodo
    @GetMapping("/destino/{nodoId}")
    public ResponseEntity<List<ArcoResponseDTO>> entrantes(@PathVariable Long nodoId) {
        return ResponseEntity.ok(arcoService.obtenerEntrantesA(nodoId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArcoResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody ArcoRequestDTO dto) {
        return ResponseEntity.ok(arcoService.actualizarArco(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        arcoService.eliminarArco(id);
        return ResponseEntity.noContent().build();
    }
}