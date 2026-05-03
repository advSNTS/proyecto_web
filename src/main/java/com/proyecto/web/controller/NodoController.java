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
@RequestMapping("/nodos")
@RequiredArgsConstructor
public class NodoController {

    private final NodoService nodoService;

    @PostMapping
    public ResponseEntity<NodoResponseDTO> crear(@RequestBody NodoRequestDTO dto) {
        return ResponseEntity.ok(nodoService.crearNodo(dto));
    }

    @GetMapping("/proceso/{idProceso}")
    public ResponseEntity<List<NodoResponseDTO>> porProceso(@PathVariable Long idProceso) {
        return ResponseEntity.ok(nodoService.obtenerPorProceso(idProceso));
    }

    // Opcional: filtrar por tipo — GET /nodos/proceso/1?tipo=ACTIVIDAD
    @GetMapping("/proceso/{idProceso}/tipo")
    public ResponseEntity<List<NodoResponseDTO>> porProcesoYTipo(
            @PathVariable Long idProceso,
            @RequestParam TipoNodo tipo) {
        return ResponseEntity.ok(nodoService.obtenerPorProcesoYTipo(idProceso, tipo));
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
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        nodoService.eliminarNodo(id);
        return ResponseEntity.noContent().build();
    }
}