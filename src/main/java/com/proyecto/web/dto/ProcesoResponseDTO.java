package com.proyecto.web.dto;

import com.proyecto.web.enums.EstadoProceso;

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
public class ProcesoResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private EstadoProceso estado;
    private Long poolId;
    private String nitEmpresa;

    /** Derivado de {@link #estado} para compatibilidad con clientes antiguos. */
    private Boolean borrador;
    /** Derivado de {@link #estado} para compatibilidad con clientes antiguos. */
    private Boolean activo;
}
