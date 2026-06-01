package com.proyecto.web.controller;

import com.proyecto.web.dto.TareaIntegracionRequestDTO;
import com.proyecto.web.dto.TareaIntegracionResponseDTO;
import com.proyecto.web.service.TareaIntegracionService;
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

import java.util.List;

@RestController
@RequestMapping("/api/tareas-integracion")
@RequiredArgsConstructor
public class TareaIntegracionController {

    private final TareaIntegracionService tareaIntegracionService;

    @PostMapping
    public ResponseEntity<TareaIntegracionResponseDTO> crear(@RequestBody TareaIntegracionRequestDTO dto) {
        return ResponseEntity.ok(tareaIntegracionService.crear(dto));
    }

    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<TareaIntegracionResponseDTO>> porProceso(@PathVariable Long procesoId) {
        return ResponseEntity.ok(tareaIntegracionService.listarPorProceso(procesoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TareaIntegracionResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(tareaIntegracionService.obtener(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TareaIntegracionResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody TareaIntegracionRequestDTO dto) {
        return ResponseEntity.ok(tareaIntegracionService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tareaIntegracionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
