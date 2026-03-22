package com.proyecto.web.mapper;

import com.proyecto.web.dto.RequiereResponseDTO;
import com.proyecto.web.entity.Actividad;
import com.proyecto.web.entity.Requiere;
import com.proyecto.web.entity.Rol;
 
public class RequiereMapper {
 
    public static Requiere toEntity(Actividad actividad, Rol rol) {
        return Requiere.builder()
                .actividad(actividad)
                .rol(rol)
                .build();
    }
 
    public static RequiereResponseDTO toResponse(Requiere requiere) {
        return RequiereResponseDTO.builder()
                .actividadId(requiere.getActividad().getId())
                .nombreActividad(requiere.getActividad().getNodo().getNombre())
                .rolId(requiere.getRol().getId())
                .nombreRol(requiere.getRol().getNombre())
                .nitEmpresa(requiere.getRol().getEmpresa().getNit())
                .build();
    }
}