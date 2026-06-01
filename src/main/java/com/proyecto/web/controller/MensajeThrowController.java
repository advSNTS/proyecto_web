package com.proyecto.web.controller;

import com.proyecto.web.dto.MensajeThrowRequestDTO;
import com.proyecto.web.dto.MensajeThrowResponseDTO;
import com.proyecto.web.service.MensajeThrowService;
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
@RequestMapping("/api/mensajes-throw")
@RequiredArgsConstructor
public class MensajeThrowController {

    private final MensajeThrowService mensajeThrowService;

    @PostMapping
    public ResponseEntity<MensajeThrowResponseDTO> crear(@RequestBody MensajeThrowRequestDTO dto) {
        return ResponseEntity.ok(mensajeThrowService.crear(dto));
    }

    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<MensajeThrowResponseDTO>> porProceso(@PathVariable Long procesoId) {
        return ResponseEntity.ok(mensajeThrowService.listarPorProceso(procesoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MensajeThrowResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(mensajeThrowService.obtener(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MensajeThrowResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody MensajeThrowRequestDTO dto) {
        return ResponseEntity.ok(mensajeThrowService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mensajeThrowService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
