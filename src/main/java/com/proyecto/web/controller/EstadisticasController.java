package com.proyecto.web.controller;

import com.proyecto.web.dto.DashboardMetricsDTO;
import com.proyecto.web.service.EstadisticasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
public class EstadisticasController {

    private final EstadisticasService estadisticasService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardMetricsDTO> dashboard() {
        return ResponseEntity.ok(estadisticasService.obtenerDashboard());
    }
}
