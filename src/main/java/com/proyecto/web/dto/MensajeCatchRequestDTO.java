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
public class MensajeCatchRequestDTO {
    private Long procesoId;
    private String nombreMensaje;
    private String correlacionExpr;
    private Boolean iniciarNuevaInstancia;
}
