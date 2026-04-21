package com.proyecto.web.mapper;

import com.proyecto.web.dto.NodoRequestDTO;
import com.proyecto.web.dto.NodoResponseDTO;
import com.proyecto.web.entity.Nodo;
import com.proyecto.web.entity.Proceso;
 
public class NodoMapper {
    private NodoMapper() {}
 
    public static Nodo toEntity(NodoRequestDTO dto, Proceso proceso) {
        return Nodo.builder()
                .proceso(proceso)
                .tipo(dto.getTipo())
                .nombre(dto.getNombre())
                .coordenadaX(dto.getCoordenadaX())
                .coordenadaY(dto.getCoordenadaY())
                .build();
    }
 
    public static NodoResponseDTO toResponse(Nodo nodo) {
        return NodoResponseDTO.builder()
                .id(nodo.getId())
                .idProceso(nodo.getProceso().getId())
                .nombreProceso(nodo.getProceso().getNombre())
                .tipo(nodo.getTipo())
                .nombre(nodo.getNombre())
                .coordenadaX(nodo.getCoordenadaX())
                .coordenadaY(nodo.getCoordenadaY())
                .build();
    }
}