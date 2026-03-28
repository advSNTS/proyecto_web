package com.proyecto.web.mapper;

import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.EmpresaResponseDTO;
import com.proyecto.web.entity.Empresa;

public class EmpresaMapper {
    private EmpresaMapper() {}

    public static Empresa toEntity(EmpresaRequestDTO dto) {
        return Empresa.builder()
                .nit(dto.getNit())
                .nombre(dto.getNombre())
                .correo(dto.getCorreo())
                .build();
    }

    public static EmpresaResponseDTO toResponse(Empresa empresa) {
        return EmpresaResponseDTO.builder()
                .nit(empresa.getNit())
                .nombre(empresa.getNombre())
                .correo(empresa.getCorreo())
                .build();
    }
}