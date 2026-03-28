package com.proyecto.web.service;

import com.proyecto.web.dto.RequiereRequestDTO;
import com.proyecto.web.dto.RequiereResponseDTO;
import com.proyecto.web.entity.Actividad;
import com.proyecto.web.entity.Requiere;
import com.proyecto.web.entity.Rol;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.mapper.RequiereMapper;
import com.proyecto.web.repository.ActividadRepository;
import com.proyecto.web.repository.EmpresaXProcesoRepository;
import com.proyecto.web.repository.RequiereRepository;
import com.proyecto.web.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
 
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class RequiereService {
 
    private final RequiereRepository requiereRepository;
    private final ActividadRepository actividadRepository;
    private final RolRepository rolRepository;
    private final EmpresaXProcesoRepository empresaXProcesoRepository;
 
    public RequiereResponseDTO asignarRol(RequiereRequestDTO dto) {
        if (requiereRepository.existsByActividad_IdAndRol_IdAndDeletedFalse(dto.getActividadId(), dto.getRolId())) {
            throw new BusinessException("La actividad ya tiene asignado ese rol");
        }
 
        Actividad actividad = actividadRepository.findByIdAndDeletedFalse(dto.getActividadId())
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
 
        Rol rol = rolRepository.findByIdAndDeletedFalse(dto.getRolId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
 
        Long idProceso = actividad.getNodo().getProceso().getId();
        String nitEmpresaRol = rol.getEmpresa().getNit();
 
        boolean relacionExiste = empresaXProcesoRepository
                .existsByEmpresa_NitAndProceso_IdAndDeletedFalse(nitEmpresaRol, idProceso);
 
        if (!relacionExiste) {
            throw new BusinessException(
                    "El rol no pertenece a una empresa asociada al proceso de esta actividad"
            );
        }
 
        Requiere requiere = RequiereMapper.toEntity(actividad, rol);
        return RequiereMapper.toResponse(requiereRepository.save(requiere));
    }
 
    public List<RequiereResponseDTO> obtenerRolesPorActividad(Long actividadId) {
        return requiereRepository.findAllByActividad_IdAndDeletedFalse(actividadId)
                .stream()
                .map(RequiereMapper::toResponse)
                .toList();
    }
 
    public List<RequiereResponseDTO> obtenerActividadesPorRol(Long rolId) {
        return requiereRepository.findAllByRol_IdAndDeletedFalse(rolId)
                .stream()
                .map(RequiereMapper::toResponse)
                .toList();
    }
 
    public void quitarRol(Long actividadId, Long rolId) {
        Requiere requiere = requiereRepository
                .findByActividad_IdAndRol_IdAndDeletedFalse(actividadId, rolId)
                .orElseThrow(() -> new RuntimeException("Relación no encontrada"));
        requiere.setDeleted(true);
        requiereRepository.save(requiere);
    }
}