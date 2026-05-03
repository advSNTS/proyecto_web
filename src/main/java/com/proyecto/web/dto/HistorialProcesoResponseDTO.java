package com.proyecto.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialProcesoResponseDTO {

    private Long id;
    private Long idProceso;
    private String nombreProceso;
    private Long idEmpleado;
    private String nombreEmpleado;
    private String valorAnterior;
    private String valorNuevo;
    private LocalDateTime fechaCambio;
    private String tipoAccion;
}
