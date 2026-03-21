package com.proyecto.web.dto;

import com.proyecto.web.enums.Permiso;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaXProcesoResponseDTO {
    private String nitEmpresa;
    private String nombreEmpresa;
    private Long idProceso;
    private String nombreProceso;
    private String nitOwner;
    private String nombreOwner;
    private Permiso permiso;
}