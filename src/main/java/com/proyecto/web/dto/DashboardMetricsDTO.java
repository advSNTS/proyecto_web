package com.proyecto.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardMetricsDTO {

    private long totalProcesos;
    private long procesosBorrador;
    private long procesosPublicado;
    private long procesosInactivo;
    private long totalEmpleados;
    private long totalActividades;
    private long totalGateways;
    private long totalPools;
    private long totalLanes;
    private long totalArcos;
    private List<CambioRecienteDTO> cambiosRecientes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CambioRecienteDTO {
        private String tipoAccion;
        private String fechaCambio;
        private String nombreProceso;
        private String nombreEmpleado;
    }
}
