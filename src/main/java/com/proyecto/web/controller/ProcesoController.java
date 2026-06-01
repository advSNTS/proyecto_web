package com.proyecto.web.controller;

import com.proyecto.web.dto.HistorialProcesoResponseDTO;
import com.proyecto.web.dto.HistorialProcesoResumenDTO;
import com.proyecto.web.dto.ProcesoCompartidoRequestDTO;
import com.proyecto.web.dto.ProcesoCompartidoResponseDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.service.ProcesoCompartidoService;
import com.proyecto.web.service.ProcesoService;
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
@RequestMapping("/api/procesos")
@RequiredArgsConstructor
public class ProcesoController {

    private final ProcesoService procesoService;
    private final ProcesoCompartidoService procesoCompartidoService;

    @PostMapping
    public ResponseEntity<ProcesoResponseDTO> crear(@RequestBody ProcesoRequestDTO dto) {
        return ResponseEntity.ok(procesoService.crearProceso(dto));
    }

    @GetMapping
    public ResponseEntity<List<ProcesoResponseDTO>> obtenerTodos(
            @RequestParam(required = false) Long poolId) {
        return ResponseEntity.ok(procesoService.obtenerProcesos(poolId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcesoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(procesoService.obtenerProceso(id));
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<ProcesoResponseDTO> obtenerDetalleRapido(@PathVariable Long id) {
        return ResponseEntity.ok(procesoService.obtenerDetalleProcesoRapido(id));
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<ProcesoResponseDTO>> obtenerPorCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(procesoService.obtenerPorCategoria(categoria));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcesoResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody ProcesoRequestDTO dto) {
        return ResponseEntity.ok(procesoService.actualizarProceso(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        procesoService.eliminarProceso(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialProcesoResponseDTO>> historial(
            @PathVariable Long id,
            @RequestParam(required = false) Integer limite) {
        return ResponseEntity.ok(procesoService.obtenerHistorialProcesoParaEmpresa(id, limite));
    }

    @GetMapping("/{id}/historial/resumen")
    public ResponseEntity<HistorialProcesoResumenDTO> resumenHistorial(
            @PathVariable Long id,
            @RequestParam(required = false) Integer limite) {
        return ResponseEntity.ok(procesoService.obtenerResumenHistorialProceso(id, limite));
    }

    @PostMapping("/{id}/compartir")
    public ResponseEntity<ProcesoCompartidoResponseDTO> compartir(
            @PathVariable Long id,
            @RequestBody ProcesoCompartidoRequestDTO dto) {
        return ResponseEntity.ok(procesoCompartidoService.compartir(id, dto));
    }

    @GetMapping("/{id}/compartidos")
    public ResponseEntity<List<ProcesoCompartidoResponseDTO>> listarCompartidos(@PathVariable Long id) {
        return ResponseEntity.ok(procesoCompartidoService.listarPorProceso(id));
    }
}
