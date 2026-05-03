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
public class ProcesoRequestDTO {

    /** NIT de la empresa propietaria (requerido salvo uso interno con contexto de seguridad). */
    private String nitEmpresa;

    private String nombre;
    private String descripcion;
    private String categoria;

    /** Si se indica, debe pertenecer a la misma empresa; si no, se usa el pool por defecto. */
    private Long poolId;

    private EstadoProceso estado;

    /** Campos legados: se traducen a {@link #estado} si este es nulo. */
    private Boolean borrador;
    private Boolean activo;
}
