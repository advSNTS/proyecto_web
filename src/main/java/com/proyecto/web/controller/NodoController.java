package com.proyecto.web.controller;

import com.proyecto.web.dto.NodoRequestDTO;
import com.proyecto.web.dto.NodoResponseDTO;
import com.proyecto.web.enums.TipoNodo;
import com.proyecto.web.service.NodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nodos")
@RequiredArgsConstructor
public class NodoController {

    private final NodoService nodoService;

    @PostMapping
    public ResponseEntity<NodoResponseDTO> crear(@RequestBody NodoRequestDTO dto) {
        return ResponseEntity.ok(nodoService.crearNodo(dto));
    }

    @GetMapping("/proceso/{idProceso}")
    public ResponseEntity<List<NodoResponseDTO>> porProceso(
            @PathVariable Long idProceso,
            @RequestParam String nitEmpresa) {
        return ResponseEntity.ok(nodoService.obtenerPorProceso(idProceso, nitEmpresa));
    }

    @GetMapping("/proceso/{idProceso}/tipo")
    public ResponseEntity<List<NodoResponseDTO>> porProcesoYTipo(
            @PathVariable Long idProceso,
            @RequestParam TipoNodo tipo,
            @RequestParam String nitEmpresa) {
        return ResponseEntity.ok(nodoService.obtenerPorProcesoYTipo(idProceso, tipo, nitEmpresa));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NodoResponseDTO> porId(@PathVariable Long id) {
        return ResponseEntity.ok(nodoService.obtenerNodo(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NodoResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody NodoRequestDTO dto) {
        return ResponseEntity.ok(nodoService.actualizarNodo(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @RequestParam String nitEmpresa) {
        nodoService.eliminarNodo(id, nitEmpresa);
        return ResponseEntity.noContent().build();
    }
}