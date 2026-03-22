package com.proyecto.web.dto;

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
public class ArcoResponseDTO {
    private Long id;
    private Long idProceso;
    private String nombreProceso;
    private Long nodoOrigenId;
    private String nombreNodoOrigen;
    private Long nodoDestinoId;
    private String nombreNodoDestino;
}