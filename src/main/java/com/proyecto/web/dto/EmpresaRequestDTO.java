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
public class EmpresaRequestDTO {
    private String nit;
    private String nombre;
    private String correo;
    /** Opcional. Si se omite, se usa la contraseña inicial por defecto del sistema (ver documentación). */
    private String contrasenaAdministrador;
}
