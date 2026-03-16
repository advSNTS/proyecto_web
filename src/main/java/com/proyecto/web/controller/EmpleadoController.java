package com.proyecto.web.controller;

import com.proyecto.web.dto.EmpleadoRequestDTO;
import com.proyecto.web.dto.EmpleadoResponseDTO;
import com.proyecto.web.service.EmpleadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    @PostMapping
    public EmpleadoResponseDTO crearEmpleado(@RequestBody EmpleadoRequestDTO dto) {
        return empleadoService.crearEmpleado(dto);
    }

    @GetMapping
    public List<EmpleadoResponseDTO> listarEmpleados() {
        return empleadoService.obtenerEmpleados();
    }

    @GetMapping("/empresa/{nit}")
    public List<EmpleadoResponseDTO> listarPorEmpresa(@PathVariable String nit) {
        return empleadoService.obtenerEmpleadosPorEmpresa(nit);
    }

    @GetMapping("/{id}")
    public EmpleadoResponseDTO obtenerEmpleado(@PathVariable Long id) {
        return empleadoService.obtenerEmpleado(id);
    }

    @PutMapping("/{id}")
    public EmpleadoResponseDTO actualizarEmpleado(@PathVariable Long id,
                                                   @RequestBody EmpleadoRequestDTO dto) {
        return empleadoService.actualizarEmpleado(id, dto);
    }

    @DeleteMapping("/{id}")
    public void eliminarEmpleado(@PathVariable Long id) {
        empleadoService.eliminarEmpleado(id);
    }
}