package com.proyecto.web.controller;

import com.proyecto.web.dto.RolXEmpleadoRequestDTO;
import com.proyecto.web.dto.RolXEmpleadoResponseDTO;
import com.proyecto.web.service.RolXEmpleadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rol-empleado")
@RequiredArgsConstructor
public class RolXEmpleadoController {

    private final RolXEmpleadoService rolXEmpleadoService;

    // Asignar un rol a un empleado
    @PostMapping
    public ResponseEntity<RolXEmpleadoResponseDTO> asignar(@RequestBody RolXEmpleadoRequestDTO dto) {
        return ResponseEntity.ok(rolXEmpleadoService.asignarRol(dto));
    }

    // Ver todos los roles de un empleado
    @GetMapping("/empleado/{empleadoId}")
    public ResponseEntity<List<RolXEmpleadoResponseDTO>> porEmpleado(@PathVariable Long empleadoId) {
        return ResponseEntity.ok(rolXEmpleadoService.obtenerRolesPorEmpleado(empleadoId));
    }

    // Ver todos los empleados con un rol
    @GetMapping("/rol/{rolId}")
    public ResponseEntity<List<RolXEmpleadoResponseDTO>> porRol(@PathVariable Long rolId) {
        return ResponseEntity.ok(rolXEmpleadoService.obtenerEmpleadosPorRol(rolId));
    }

    // Quitar (soft delete) un rol a un empleado
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> quitar(@PathVariable Long id) {
        rolXEmpleadoService.quitarRol(id);
        return ResponseEntity.noContent().build();
    }
}