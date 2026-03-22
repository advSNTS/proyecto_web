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
public class ArcoRequestDTO {
    private Long idProceso;
    private Long nodoOrigenId;
    private Long nodoDestinoId;
}
 