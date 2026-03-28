package com.proyecto.web.mapper;

import com.proyecto.web.dto.HistorialActividadResponseDTO;
import com.proyecto.web.entity.HistorialActividad;
 
public class HistorialActividadMapper {
    private HistorialActividadMapper() {}
 
    public static HistorialActividadResponseDTO toResponse(HistorialActividad h) {
        return HistorialActividadResponseDTO.builder()
                .id(h.getId())
                .idActividad(h.getActividad().getId())
                .nombreNodo(h.getActividad().getNodo().getNombre())
                .idEmpleado(h.getEmpleado() != null ? h.getEmpleado().getId() : null)
                .nombreEmpleado(h.getEmpleado() != null ? h.getEmpleado().getNombre() : null)
                .valorAnterior(h.getValorAnterior())
                .valorNuevo(h.getValorNuevo())
                .fechaCambio(h.getFechaCambio())
                .tipoAccion(h.getTipoAccion())
                .build();
    }
}