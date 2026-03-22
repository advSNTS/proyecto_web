package com.proyecto.web.dto;

import com.proyecto.web.enums.TipoNodo;

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
public class NodoResponseDTO {
    private Long id;
    private Long idProceso;
    private String nombreProceso;
    private TipoNodo tipo;
    private String nombre;
}
 