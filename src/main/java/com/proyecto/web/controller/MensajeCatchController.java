package com.proyecto.web.controller;

import com.proyecto.web.dto.MensajeCatchRequestDTO;
import com.proyecto.web.dto.MensajeCatchResponseDTO;
import com.proyecto.web.service.MensajeCatchService;
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
@RequestMapping("/api/mensajes-catch")
@RequiredArgsConstructor
public class MensajeCatchController {

    private final MensajeCatchService mensajeCatchService;

    @PostMapping
    public ResponseEntity<MensajeCatchResponseDTO> crear(@RequestBody MensajeCatchRequestDTO dto) {
        return ResponseEntity.ok(mensajeCatchService.crear(dto));
    }

    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<MensajeCatchResponseDTO>> porProceso(@PathVariable Long procesoId) {
        return ResponseEntity.ok(mensajeCatchService.listarPorProceso(procesoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MensajeCatchResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(mensajeCatchService.obtener(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MensajeCatchResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody MensajeCatchRequestDTO dto) {
        return ResponseEntity.ok(mensajeCatchService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mensajeCatchService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
