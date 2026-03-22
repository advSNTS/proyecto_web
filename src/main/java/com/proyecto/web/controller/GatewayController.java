package com.proyecto.web.controller;
import com.proyecto.web.dto.GatewayRequestDTO;
import com.proyecto.web.dto.GatewayResponseDTO;
import com.proyecto.web.enums.TipoGateway;
import com.proyecto.web.service.GatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
@RestController
@RequestMapping("/gateways")
@RequiredArgsConstructor
public class GatewayController {
 
    private final GatewayService gatewayService;
 
    @PostMapping
    public ResponseEntity<GatewayResponseDTO> crear(@RequestBody GatewayRequestDTO dto) {
        return ResponseEntity.ok(gatewayService.crearGateway(dto));
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<GatewayResponseDTO> porId(@PathVariable Long id) {
        return ResponseEntity.ok(gatewayService.obtenerGateway(id));
    }
 
    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<GatewayResponseDTO>> porProceso(@PathVariable Long procesoId) {
        return ResponseEntity.ok(gatewayService.obtenerPorProceso(procesoId));
    }
 
    // Filtrar por tipo — GET /gateways/proceso/1/tipo?tipo=XOR
    @GetMapping("/proceso/{procesoId}/tipo")
    public ResponseEntity<List<GatewayResponseDTO>> porProcesoYTipo(
            @PathVariable Long procesoId,
            @RequestParam TipoGateway tipo) {
        return ResponseEntity.ok(gatewayService.obtenerPorProcesoYTipo(procesoId, tipo));
    }
 
    @PutMapping("/{id}")
    public ResponseEntity<GatewayResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody GatewayRequestDTO dto) {
        return ResponseEntity.ok(gatewayService.actualizarGateway(id, dto));
    }
 
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        gatewayService.eliminarGateway(id);
        return ResponseEntity.noContent().build();
    }
}