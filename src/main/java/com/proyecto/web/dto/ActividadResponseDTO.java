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
public class ActividadResponseDTO {
    private Long id;
    private Long nodoId;
    private String nombreNodo;
    private Long procesoId;
    private String nombreProceso;
    private String descripcion;
}