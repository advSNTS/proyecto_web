package com.proyecto.web.dto;

import java.time.LocalDateTime;

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
public class HistorialProcesoResponseDTO {
    private Long id;
    private Long idProceso;
    private String nombreProceso;
    private Long idEmpleado;           // null si no se registró responsable
    private String nombreEmpleado;     // null si no se registró responsable
    private String valorAnterior;      // JSON como string
    private String valorNuevo;         // JSON como string — null en eliminación
    private LocalDateTime fechaCambio;
    private String tipoAccion;
}