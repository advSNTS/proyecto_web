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
public class RequiereResponseDTO {
    private Long actividadId;
    private String nombreActividad;  // nombre del nodo asociado
    private Long rolId;
    private String nombreRol;
    private String nitEmpresa;
}
 