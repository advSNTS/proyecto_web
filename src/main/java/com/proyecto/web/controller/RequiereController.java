package com.proyecto.web.controller;

import com.proyecto.web.dto.RequiereRequestDTO;
import com.proyecto.web.dto.RequiereResponseDTO;
import com.proyecto.web.service.RequiereService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
@RestController
@RequestMapping("/requiere")
@RequiredArgsConstructor
public class RequiereController {
 
    private final RequiereService requiereService;
 
    @PostMapping
    public ResponseEntity<RequiereResponseDTO> asignar(@RequestBody RequiereRequestDTO dto) {
        return ResponseEntity.ok(requiereService.asignarRol(dto));
    }
 

    @GetMapping("/actividad/{actividadId}")
    public ResponseEntity<List<RequiereResponseDTO>> porActividad(@PathVariable Long actividadId) {
        return ResponseEntity.ok(requiereService.obtenerRolesPorActividad(actividadId));
    }
 
    // Actividades que requieren un rol
    @GetMapping("/rol/{rolId}")
    public ResponseEntity<List<RequiereResponseDTO>> porRol(@PathVariable Long rolId) {
        return ResponseEntity.ok(requiereService.obtenerActividadesPorRol(rolId));
    }
 
    // Quitar un rol de una actividad (soft delete)
    @DeleteMapping("/actividad/{actividadId}/rol/{rolId}")
    public ResponseEntity<Void> quitar(
            @PathVariable Long actividadId,
            @PathVariable Long rolId) {
        requiereService.quitarRol(actividadId, rolId);
        return ResponseEntity.noContent().build();
    }
}
