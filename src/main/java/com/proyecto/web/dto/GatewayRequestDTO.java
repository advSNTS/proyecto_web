package com.proyecto.web.dto;

import com.proyecto.web.enums.TipoGateway;

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
public class GatewayRequestDTO {
    private Long nodoId;
    private TipoGateway tipoGateway;
}
 