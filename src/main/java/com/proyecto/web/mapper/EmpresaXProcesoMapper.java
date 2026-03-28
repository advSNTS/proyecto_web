package com.proyecto.web.mapper;

import com.proyecto.web.dto.EmpresaXProcesoResponseDTO;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.entity.EmpresaXProceso;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.enums.Permiso;

public class EmpresaXProcesoMapper {
    private EmpresaXProcesoMapper() {}

    public static EmpresaXProceso toEntity(Empresa empresa, Proceso proceso, Empresa owner, Permiso permiso) {
        return EmpresaXProceso.builder()
                .empresa(empresa)
                .proceso(proceso)
                .empresaOwner(owner)
                .permiso(permiso)
                .build();
    }

    public static EmpresaXProcesoResponseDTO toResponse(EmpresaXProceso ep) {
        return EmpresaXProcesoResponseDTO.builder()
                .nitEmpresa(ep.getEmpresa().getNit())
                .nombreEmpresa(ep.getEmpresa().getNombre())
                .idProceso(ep.getProceso().getId())
                .nombreProceso(ep.getProceso().getNombre())
                .nitOwner(ep.getEmpresaOwner().getNit())
                .nombreOwner(ep.getEmpresaOwner().getNombre())
                .permiso(ep.getPermiso())
                .build();
    }
}