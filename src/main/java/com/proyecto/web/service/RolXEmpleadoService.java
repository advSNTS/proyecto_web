package com.proyecto.web.service;

import com.proyecto.web.dto.RolXEmpleadoRequestDTO;
import com.proyecto.web.dto.RolXEmpleadoResponseDTO;
import com.proyecto.web.entity.Empleado;
import com.proyecto.web.entity.EmpleadoRolSistema;
import com.proyecto.web.entity.Rol;
import com.proyecto.web.entity.RolXEmpleado;
import com.proyecto.web.enums.Permiso;
import com.proyecto.web.enums.TipoRolSistema;
import com.proyecto.web.mapper.RolXEmpleadoMapper;
import com.proyecto.web.repository.EmpleadoRepository;
import com.proyecto.web.repository.EmpleadoRolSistemaRepository;
import com.proyecto.web.repository.RolRepository;
import com.proyecto.web.repository.RolXEmpleadoRepository;
import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolXEmpleadoService {

    private final RolXEmpleadoRepository rolXEmpleadoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final RolRepository rolRepository;
    private final EmpleadoRolSistemaRepository empleadoRolSistemaRepository;

    @Transactional
    public RolXEmpleadoResponseDTO asignarRol(RolXEmpleadoRequestDTO dto) {
        if (rolXEmpleadoRepository.existsByEmpleado_IdAndRol_IdAndDeletedFalse(dto.getEmpleadoId(), dto.getRolId())) {
            throw new RuntimeException("El empleado ya tiene asignado ese rol");
        }

        Empleado empleado = empleadoRepository.findByIdAndDeletedFalse(dto.getEmpleadoId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        Rol rol = rolRepository.findByIdAndDeletedFalse(dto.getRolId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        if (!rol.getEmpresa().getNit().equals(empleado.getEmpresa().getNit())) {
            throw new RuntimeException("El rol no pertenece a la empresa del empleado");
        }

        // ← NUEVO: sincronizar EmpleadoRolSistema
        TipoRolSistema tipoRol = mapearPermiso(rol.getPermiso());
        empleadoRolSistemaRepository.findAllByEmpleado_IdAndEliminadoFalse(empleado.getId())
                .forEach(r -> {
                    r.setEliminado(true);
                    empleadoRolSistemaRepository.save(r);
                });
        empleadoRolSistemaRepository.save(EmpleadoRolSistema.builder()
                .empleado(empleado)
                .empresa(empleado.getEmpresa())
                .tipoRol(tipoRol)
                .eliminado(false)
                .build());

        RolXEmpleado rxe = RolXEmpleadoMapper.toEntity(empleado, rol);
        return RolXEmpleadoMapper.toResponse(rolXEmpleadoRepository.save(rxe));
    }

    private TipoRolSistema mapearPermiso(Permiso permiso) {
        if (permiso == null) return TipoRolSistema.READER;
        return switch (permiso) {
            case ADMINISTRAR -> TipoRolSistema.ADMIN;
            case EDITAR      -> TipoRolSistema.EDITOR;
            default          -> TipoRolSistema.READER;
        };
    }

    public List<RolXEmpleadoResponseDTO> obtenerRolesPorEmpleado(Long empleadoId) {
        return rolXEmpleadoRepository.findAllByEmpleado_IdAndDeletedFalse(empleadoId)
                .stream()
                .map(RolXEmpleadoMapper::toResponse)
                .toList();
    }

    public List<RolXEmpleadoResponseDTO> obtenerEmpleadosPorRol(Long rolId) {
        return rolXEmpleadoRepository.findAllByRol_IdAndDeletedFalse(rolId)
                .stream()
                .map(RolXEmpleadoMapper::toResponse)
                .toList();
    }

    public void quitarRol(Long id) {
        RolXEmpleado rxe = rolXEmpleadoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada"));
        rxe.setDeleted(true);
        rolXEmpleadoRepository.save(rxe);
    }
}