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
public class HistorialActividadResponseDTO {
    private Long id;
    private Long idActividad;
    private String nombreNodo;
    private Long idEmpleado;       // null si no se registró responsable
    private String nombreEmpleado; // null si no se registró responsable
    private String valorAnterior;
    private String valorNuevo;     // null en eliminación
    private LocalDateTime fechaCambio;
    private String tipoAccion;
}
 