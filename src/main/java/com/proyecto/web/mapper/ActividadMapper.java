package com.proyecto.web.mapper;

 
import com.proyecto.web.dto.ActividadRequestDTO;
import com.proyecto.web.dto.ActividadResponseDTO;
import com.proyecto.web.entity.Actividad;
import com.proyecto.web.entity.Nodo;
 
public class ActividadMapper {
 
    public static Actividad toEntity(ActividadRequestDTO dto, Nodo nodo) {
        return Actividad.builder()
                .nodo(nodo)
                .descripcion(dto.getDescripcion())
                .build();
    }
 
    public static ActividadResponseDTO toResponse(Actividad actividad) {
        return ActividadResponseDTO.builder()
                .id(actividad.getId())
                .nodoId(actividad.getNodo().getId())
                .nombreNodo(actividad.getNodo().getNombre())
                .procesoId(actividad.getNodo().getProceso().getId())
                .nombreProceso(actividad.getNodo().getProceso().getNombre())
                .descripcion(actividad.getDescripcion())
                .build();
    }
}