package com.proyecto.web.controller;

import com.proyecto.web.dto.MensajeThrowRequestDTO;
import com.proyecto.web.dto.MensajeThrowResponseDTO;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.service.MensajeThrowService;
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
@RequestMapping("/api/mensajes-throw")
@RequiredArgsConstructor
public class MensajeThrowController {

    private final MensajeThrowService mensajeThrowService;

    @PostMapping
    public ResponseEntity<MensajeThrowResponseDTO> crear(
            @RequestBody MensajeThrowRequestDTO dto,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(mensajeThrowService.crear(requerirNit(nitEmpresa), dto));
    }

    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<MensajeThrowResponseDTO>> porProceso(
            @PathVariable Long procesoId,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(mensajeThrowService.listarPorProceso(requerirNit(nitEmpresa), procesoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MensajeThrowResponseDTO> obtener(
            @PathVariable Long id,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(mensajeThrowService.obtener(requerirNit(nitEmpresa), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MensajeThrowResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody MensajeThrowRequestDTO dto,
            @RequestParam(required = false) String nitEmpresa) {
        return ResponseEntity.ok(mensajeThrowService.actualizar(requerirNit(nitEmpresa), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @RequestParam(required = false) String nitEmpresa) {
        mensajeThrowService.eliminar(requerirNit(nitEmpresa), id);
        return ResponseEntity.noContent().build();
    }

    private String requerirNit(String nitEmpresa) {
        String nit = SecurityUtils.resolverNitEmpresa(nitEmpresa);
        if (nit == null || nit.isBlank()) {
            throw new BusinessException("nitEmpresa es obligatorio", HttpStatus.BAD_REQUEST);
        }
        return nit;
    }
}
