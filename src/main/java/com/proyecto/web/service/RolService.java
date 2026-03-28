package com.proyecto.web.service;

import com.proyecto.web.dto.RolRequestDTO;
import com.proyecto.web.dto.RolResponseDTO;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.entity.Requiere;
import com.proyecto.web.entity.Rol;
import com.proyecto.web.entity.RolXEmpleado;
import com.proyecto.web.mapper.RolMapper;
import com.proyecto.web.repository.EmpresaRepository;
import com.proyecto.web.repository.RequiereRepository;
import com.proyecto.web.repository.RolRepository;
import com.proyecto.web.repository.RolXEmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class RolService {
 
    private static final String ROL_NO_ENCONTRADO = "Rol no encontrado";
    private static final String EMPRESA_NO_ENCONTRADA = "Empresa no encontrada";

    private final RolRepository rolRepository;
    private final EmpresaRepository empresaRepository;
    private final RolXEmpleadoRepository rolXEmpleadoRepository;
    private final RequiereRepository requiereRepository;
 
    public RolResponseDTO crearRol(RolRequestDTO dto) {
        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(dto.getNitEmpresa())
                .orElseThrow(() -> new RuntimeException(EMPRESA_NO_ENCONTRADA));
 
        Rol rol = RolMapper.toEntity(dto, empresa);
        return RolMapper.toResponse(rolRepository.save(rol));
    }
 
    public List<RolResponseDTO> obtenerRoles() {
        return rolRepository.findAllByDeletedFalse()
                .stream()
                .map(RolMapper::toResponse)
                .toList();
    }
 
    public List<RolResponseDTO> obtenerRolesPorEmpresa(String nit) {
        return rolRepository.findAllByEmpresa_NitAndDeletedFalse(nit)
                .stream()
                .map(RolMapper::toResponse)
                .toList();
    }
 
    public RolResponseDTO obtenerRol(Long id) {
        Rol rol = rolRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException(ROL_NO_ENCONTRADO));
        return RolMapper.toResponse(rol);
    }
 
    public RolResponseDTO actualizarRol(Long id, RolRequestDTO dto) {
        Rol rol = rolRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException(ROL_NO_ENCONTRADO));
 
        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(dto.getNitEmpresa())
                .orElseThrow(() -> new RuntimeException(EMPRESA_NO_ENCONTRADA));
 
        rol.setEmpresa(empresa);
        rol.setNombre(dto.getNombre());
        rol.setPermiso(dto.getPermiso());
 
        return RolMapper.toResponse(rolRepository.save(rol));
    }
 
    /**
     * Soft delete del rol.
     * Cascada: desactiva todos los RolXEmpleado y Requiere que usen este rol,
     * ya que esas relaciones dejan de ser válidas.
     */
    @Transactional
    public void eliminarRol(Long id) {
        Rol rol = rolRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException(ROL_NO_ENCONTRADO));
 
        rol.setDeleted(true);
        rolRepository.save(rol);
 
        // Cascada 1: desactivar asignaciones de este rol a empleados
        List<RolXEmpleado> asignaciones = rolXEmpleadoRepository.findAllByRol_IdAndDeletedFalse(id);
        asignaciones.forEach(a -> a.setDeleted(true));
        rolXEmpleadoRepository.saveAll(asignaciones);
 
        // Cascada 2: desactivar actividades que requerían este rol
        List<Requiere> requeridos = requiereRepository.findAllByRol_IdAndDeletedFalse(id);
        requeridos.forEach(r -> r.setDeleted(true));
        requiereRepository.saveAll(requeridos);
    }
}