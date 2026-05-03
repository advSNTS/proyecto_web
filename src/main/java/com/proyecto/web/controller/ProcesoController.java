package com.proyecto.web.controller;

import com.proyecto.web.dto.HistorialProcesoResponseDTO;
import com.proyecto.web.dto.ProcesoCompartidoRequestDTO;
import com.proyecto.web.dto.ProcesoCompartidoResponseDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.security.UsuarioPrincipal;
import com.proyecto.web.service.ProcesoCompartidoService;
import com.proyecto.web.service.ProcesoService;
import com.proyecto.web.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(procesoService.obtenerProcesos(requerirNit(nitEmpresa)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcesoResponseDTO> obtenerPorId(
            @PathVariable Long id,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(procesoService.obtenerProceso(id, requerirNit(nitEmpresa)));
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<ProcesoResponseDTO>> obtenerPorCategoria(
            @PathVariable String categoria,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(procesoService.obtenerPorCategoria(categoria, requerirNit(nitEmpresa)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcesoResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody ProcesoRequestDTO dto,
            @RequestParam(required = false) Long idEmpleado,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(
                procesoService.actualizarProceso(id, dto, idEmpleado, requerirNit(nitEmpresa)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @RequestParam(required = false) Long idEmpleado,
            @RequestParam(required = false) String nitEmpresa) {
        procesoService.eliminarProceso(id, idEmpleado, requerirNit(nitEmpresa));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialProcesoResponseDTO>> historial(
            @PathVariable Long id,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(
                procesoService.obtenerHistorialProcesoParaEmpresa(id, requerirNit(nitEmpresa)));
    }

    @PostMapping("/{id}/compartir")
    public ResponseEntity<ProcesoCompartidoResponseDTO> compartir(
            @PathVariable Long id,
            @RequestBody ProcesoCompartidoRequestDTO dto,
            @RequestParam(required = false) String nitEmpresa) {
        Long empleadoId = SecurityUtils.currentUser().map(UsuarioPrincipal::getEmpleadoId).orElse(null);
        return ResponseEntity.ok(
                procesoCompartidoService.compartir(id, dto, empleadoId, requerirNit(nitEmpresa)));
    }

    @GetMapping("/{id}/compartidos")
    public ResponseEntity<List<ProcesoCompartidoResponseDTO>> listarCompartidos(
            @PathVariable Long id,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(
                procesoCompartidoService.listarPorProceso(requerirNit(nitEmpresa), id));
    }

    private String requerirNit(String nitEmpresa) {
        String nit = SecurityUtils.resolverNitEmpresa(nitEmpresa);
        if (nit == null || nit.isBlank()) {
            throw new BusinessException(
                    "nitEmpresa es obligatorio (query param o token JWT)",
                    HttpStatus.BAD_REQUEST);
        }
        return nit;
    }
}
