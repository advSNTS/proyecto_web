package com.proyecto.web.dto;

import com.proyecto.web.enums.TipoDestinoMensajeExterno;

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
public class MensajeExternoRequestDTO {
    private TipoDestinoMensajeExterno destinoTipo;
    private String configuracion;
    private String credenciales;
}
