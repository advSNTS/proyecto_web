package com.proyecto.web.dto;

import com.proyecto.web.enums.PermisoProcesoCompartido;

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
public class ProcesoCompartidoResponseDTO {
    private Long id;
    private Long procesoId;
    private Long poolId;
    private PermisoProcesoCompartido permiso;
}
