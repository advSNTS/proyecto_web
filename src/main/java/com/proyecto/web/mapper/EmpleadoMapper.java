package com.proyecto.web.mapper;

import com.proyecto.web.dto.EmpleadoRequestDTO;
import com.proyecto.web.dto.EmpleadoResponseDTO;
import com.proyecto.web.entity.Empleado;
import com.proyecto.web.entity.Empresa;

public class EmpleadoMapper {

    public static Empleado toEntity(EmpleadoRequestDTO dto, Empresa empresa) {
        return Empleado.builder()
                .empresa(empresa)
                .nombre(dto.getNombre())
                .tipoDocumento(dto.getTipoDocumento())
                .numeroDocumento(dto.getNumeroDocumento())
                .build();
    }

    public static EmpleadoResponseDTO toResponse(Empleado empleado) {
        return EmpleadoResponseDTO.builder()
                .id(empleado.getId())
                .nitEmpresa(empleado.getEmpresa().getNit())
                .nombreEmpresa(empleado.getEmpresa().getNombre())
                .nombre(empleado.getNombre())
                .tipoDocumento(empleado.getTipoDocumento())
                .numeroDocumento(empleado.getNumeroDocumento())
                .build();
    }
}