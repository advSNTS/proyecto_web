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
public class ProcesoRequestDTO {
    private String nombre;
    private String descripcion;
    private String categoria;
    private Boolean borrador;
    private Boolean activo;
}
 