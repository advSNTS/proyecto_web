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
public class RolRequestDTO {
    private String nitEmpresa;
    private String nombre;
    private Permiso permiso;
}
