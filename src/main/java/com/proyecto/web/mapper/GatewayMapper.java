package com.proyecto.web.mapper;

import com.proyecto.web.dto.GatewayRequestDTO;
import com.proyecto.web.dto.GatewayResponseDTO;
import com.proyecto.web.entity.Gateway;
import com.proyecto.web.entity.Nodo;
 
public class GatewayMapper {
    private GatewayMapper() {}
 
    public static Gateway toEntity(GatewayRequestDTO dto, Nodo nodo) {
        return Gateway.builder()
                .nodo(nodo)
                .tipoGateway(dto.getTipoGateway())
                .build();
    }
 
    public static GatewayResponseDTO toResponse(Gateway gateway) {
        return GatewayResponseDTO.builder()
                .id(gateway.getId())
                .nodoId(gateway.getNodo().getId())
                .nombreNodo(gateway.getNodo().getNombre())
                .procesoId(gateway.getNodo().getProceso().getId())
                .nombreProceso(gateway.getNodo().getProceso().getNombre())
                .tipoGateway(gateway.getTipoGateway())
                .build();
    }
}