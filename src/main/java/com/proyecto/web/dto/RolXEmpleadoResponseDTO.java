package com.proyecto.web.dto;

import com.proyecto.web.enums.Permiso;

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
public class RolXEmpleadoResponseDTO {
    private Long id;
    private Long empleadoId;
    private String nombreEmpleado;
    private Long rolId;
    private String nombreRol;
    private Permiso permiso;
}
 