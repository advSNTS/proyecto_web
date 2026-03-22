package com.proyecto.web.mapper;

import com.proyecto.web.dto.ArcoRequestDTO;
import com.proyecto.web.dto.ArcoResponseDTO;
import com.proyecto.web.entity.Arco;
import com.proyecto.web.entity.Nodo;
import com.proyecto.web.entity.Proceso;
 
public class ArcoMapper {
 
    public static Arco toEntity(ArcoRequestDTO dto, Proceso proceso, Nodo origen, Nodo destino) {
        return Arco.builder()
                .proceso(proceso)
                .nodoOrigen(origen)
                .nodoDestino(destino)
                .build();
    }
 
    public static ArcoResponseDTO toResponse(Arco arco) {
        return ArcoResponseDTO.builder()
                .id(arco.getId())
                .idProceso(arco.getProceso().getId())
                .nombreProceso(arco.getProceso().getNombre())
                .nodoOrigenId(arco.getNodoOrigen().getId())
                .nombreNodoOrigen(arco.getNodoOrigen().getNombre())
                .nodoDestinoId(arco.getNodoDestino().getId())
                .nombreNodoDestino(arco.getNodoDestino().getNombre())
                .build();
    }
}