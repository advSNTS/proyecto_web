package com.proyecto.web.service;

import com.proyecto.web.dto.RolRequestDTO;
import com.proyecto.web.dto.RolResponseDTO;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.entity.Rol;
import com.proyecto.web.mapper.RolMapper;
import com.proyecto.web.repository.EmpresaRepository;
import com.proyecto.web.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;
    private final EmpresaRepository empresaRepository;

    public RolResponseDTO crearRol(RolRequestDTO dto) {
        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(dto.getNitEmpresa())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

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
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        return RolMapper.toResponse(rol);
    }

    public RolResponseDTO actualizarRol(Long id, RolRequestDTO dto) {
        Rol rol = rolRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(dto.getNitEmpresa())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        rol.setEmpresa(empresa);
        rol.setNombre(dto.getNombre());
        rol.setPermiso(dto.getPermiso());

        return RolMapper.toResponse(rolRepository.save(rol));
    }

    public void eliminarRol(Long id) {
        Rol rol = rolRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        rol.setDeleted(true);
        rolRepository.save(rol);
    }
}