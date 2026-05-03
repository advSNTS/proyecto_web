package com.proyecto.web.controller;

import com.proyecto.web.dto.EmpresaXProcesoRequestDTO;
import com.proyecto.web.dto.EmpresaXProcesoResponseDTO;
import com.proyecto.web.service.EmpresaXProcesoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/empresa-proceso")
@RequiredArgsConstructor
public class EmpresaXProcesoController {

    private final EmpresaXProcesoService empresaXProcesoService;

    // Asignar un proceso a una empresa
    @PostMapping
    public ResponseEntity<EmpresaXProcesoResponseDTO> asignar(@RequestBody EmpresaXProcesoRequestDTO dto) {
        return ResponseEntity.ok(empresaXProcesoService.asignarProceso(dto));
    }

    // Procesos activos de una empresa
    @GetMapping("/empresa/{nit}")
    public ResponseEntity<List<EmpresaXProcesoResponseDTO>> porEmpresa(@PathVariable String nit) {
        return ResponseEntity.ok(empresaXProcesoService.obtenerProcesosPorEmpresa(nit));
    }

    // Empresas que tienen un proceso
    @GetMapping("/proceso/{idProceso}")
    public ResponseEntity<List<EmpresaXProcesoResponseDTO>> porProceso(@PathVariable Long idProceso) {
        return ResponseEntity.ok(empresaXProcesoService.obtenerEmpresasPorProceso(idProceso));
    }

    // Procesos donde la empresa es owner
    @GetMapping("/owner/{nit}")
    public ResponseEntity<List<EmpresaXProcesoResponseDTO>> comoOwner(@PathVariable String nit) {
        return ResponseEntity.ok(empresaXProcesoService.obtenerProcesosComoOwner(nit));
    }

    // Actualizar solo el permiso de la relación
    @PatchMapping("/empresa/{nit}/proceso/{idProceso}")
    public ResponseEntity<EmpresaXProcesoResponseDTO> actualizarPermiso(
            @PathVariable String nit,
            @PathVariable Long idProceso,
            @RequestBody EmpresaXProcesoRequestDTO dto) {
        return ResponseEntity.ok(empresaXProcesoService.actualizarPermiso(nit, idProceso, dto));
    }

    // Soft delete de la relación
    @DeleteMapping("/empresa/{nit}/proceso/{idProceso}")
    public ResponseEntity<Void> quitar(
            @PathVariable String nit,
            @PathVariable Long idProceso) {
        empresaXProcesoService.quitarProceso(nit, idProceso);
        return ResponseEntity.noContent().build();
    }
}