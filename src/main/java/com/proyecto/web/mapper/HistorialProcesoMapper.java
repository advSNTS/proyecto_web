package com.proyecto.web.mapper;
import com.proyecto.web.dto.HistorialProcesoResponseDTO;
import com.proyecto.web.entity.HistorialProceso;
 
public class HistorialProcesoMapper {
    private HistorialProcesoMapper() {}
 
    public static HistorialProcesoResponseDTO toResponse(HistorialProceso h) {
        return HistorialProcesoResponseDTO.builder()
                .id(h.getId())
                .idProceso(h.getProceso().getId())
                .nombreProceso(h.getProceso().getNombre())
                .idEmpleado(h.getEmpleado() != null ? h.getEmpleado().getId() : null)
                .nombreEmpleado(h.getEmpleado() != null ? h.getEmpleado().getNombre() : null)
                .valorAnterior(h.getValorAnterior())
                .valorNuevo(h.getValorNuevo())
                .fechaCambio(h.getFechaCambio())
                .tipoAccion(h.getTipoAccion())
                .build();
    }
}