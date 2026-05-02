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
public class TareaIntegracionResponseDTO {
    private Long id;
    private Long procesoId;
    private Long mensajeExternoId;
    private String payloadMapping;
}
