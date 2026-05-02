package com.proyecto.web.controller;

import com.proyecto.web.dto.MensajeExternoRequestDTO;
import com.proyecto.web.dto.MensajeExternoResponseDTO;
import com.proyecto.web.service.MensajeExternoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mensajes-externos")
@RequiredArgsConstructor
public class MensajeExternoController {

    private final MensajeExternoService mensajeExternoService;

    @PostMapping
    public ResponseEntity<MensajeExternoResponseDTO> crear(@RequestBody MensajeExternoRequestDTO dto) {
        return ResponseEntity.ok(mensajeExternoService.crear(dto));
    }

    @GetMapping
    public ResponseEntity<List<MensajeExternoResponseDTO>> listar() {
        return ResponseEntity.ok(mensajeExternoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MensajeExternoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(mensajeExternoService.obtener(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MensajeExternoResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody MensajeExternoRequestDTO dto) {
        return ResponseEntity.ok(mensajeExternoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mensajeExternoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
