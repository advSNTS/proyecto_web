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
public class EmpresaResponseDTO {
    private String nit;
    private String nombre;
    private String correo;
    /** Mensaje orientativo para el usuario (sin exponer contraseñas autogeneradas). */
    private String mensajeRegistro;
}
