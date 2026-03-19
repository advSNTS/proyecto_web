package com.proyecto.web.mapper;
 
import com.proyecto.web.dto.RolXEmpleadoResponseDTO;
import com.proyecto.web.entity.Empleado;
import com.proyecto.web.entity.Rol;
import com.proyecto.web.entity.RolXEmpleado;
 
public class RolXEmpleadoMapper {
 
    public static RolXEmpleado toEntity(Empleado empleado, Rol rol) {
        return RolXEmpleado.builder()
                .empleado(empleado)
                .rol(rol)
                .build();
    }
 
    public static RolXEmpleadoResponseDTO toResponse(RolXEmpleado rxe) {
        return RolXEmpleadoResponseDTO.builder()
                .id(rxe.getId())
                .empleadoId(rxe.getEmpleado().getId())
                .nombreEmpleado(rxe.getEmpleado().getNombre())
                .rolId(rxe.getRol().getId())
                .nombreRol(rxe.getRol().getNombre())
                .permiso(rxe.getRol().getPermiso())
                .build();
    }
}