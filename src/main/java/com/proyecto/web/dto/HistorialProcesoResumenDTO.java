package com.proyecto.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialProcesoResumenDTO {

    private Long idProceso;
    private long totalCambios;
    private int limiteAplicado;
    private boolean hayMas;
}