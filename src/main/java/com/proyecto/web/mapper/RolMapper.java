package com.proyecto.web.mapper;
 
import com.proyecto.web.dto.RolRequestDTO;
import com.proyecto.web.dto.RolResponseDTO;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.entity.Rol;
 
public class RolMapper {
    private RolMapper() {}
 
    public static Rol toEntity(RolRequestDTO dto, Empresa empresa) {
        return Rol.builder()
                .empresa(empresa)
                .nombre(dto.getNombre())
                .permiso(dto.getPermiso())
                .build();
    }
 
    public static RolResponseDTO toResponse(Rol rol) {
        return RolResponseDTO.builder()
                .id(rol.getId())
                .nitEmpresa(rol.getEmpresa().getNit())
                .nombreEmpresa(rol.getEmpresa().getNombre())
                .nombre(rol.getNombre())
                .permiso(rol.getPermiso())
                .build();
    }
}