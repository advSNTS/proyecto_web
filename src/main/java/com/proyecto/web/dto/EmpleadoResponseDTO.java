package com.proyecto.web.dto;

import com.proyecto.web.enums.TipoDocumento;

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
public class EmpleadoResponseDTO {
    private Long id;
    private String nitEmpresa;
    private String nombreEmpresa;
    private String nombre;
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private String correo;
}