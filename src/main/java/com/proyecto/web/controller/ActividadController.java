package com.proyecto.web.controller;

import com.proyecto.web.dto.ActividadRequestDTO;
import com.proyecto.web.dto.ActividadResponseDTO;
import com.proyecto.web.dto.HistorialActividadResponseDTO;
import com.proyecto.web.service.ActividadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
@RestController
@RequestMapping("/actividades")
@RequiredArgsConstructor
public class ActividadController {
 
    private final ActividadService actividadService;
 
    @PostMapping
    public ResponseEntity<ActividadResponseDTO> crear(@RequestBody ActividadRequestDTO dto) {
        return ResponseEntity.ok(actividadService.crearActividad(dto));
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<ActividadResponseDTO> porId(@PathVariable Long id) {
        return ResponseEntity.ok(actividadService.obtenerActividad(id));
    }
 
    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<ActividadResponseDTO>> porProceso(@PathVariable Long procesoId) {
        return ResponseEntity.ok(actividadService.obtenerPorProceso(procesoId));
    }
 
    // idEmpleado opcional — si no se manda, el historial queda sin responsable
    @PutMapping("/{id}")
    public ResponseEntity<ActividadResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody ActividadRequestDTO dto,
            @RequestParam(required = false) Long idEmpleado) {
        return ResponseEntity.ok(actividadService.actualizarActividad(id, dto, idEmpleado));
    }
 
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @RequestParam(required = false) Long idEmpleado) {
        actividadService.eliminarActividad(id, idEmpleado);
        return ResponseEntity.noContent().build();
    }
 
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialActividadResponseDTO>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(actividadService.obtenerHistorial(id));
    }
}