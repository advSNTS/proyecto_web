package com.proyecto.web.service;

import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.EmpresaResponseDTO;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.mapper.EmpresaMapper;
import com.proyecto.web.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaResponseDTO crearEmpresa(EmpresaRequestDTO dto) {
        Empresa empresa = EmpresaMapper.toEntity(dto);
        empresa = empresaRepository.save(empresa);
        return EmpresaMapper.toResponse(empresa);
    }

    public List<EmpresaResponseDTO> obtenerEmpresas() {
        return empresaRepository.findAllByDeletedFalse()
                .stream()
                .map(EmpresaMapper::toResponse)
                .toList();
    }

    public EmpresaResponseDTO obtenerEmpresa(String nit) {
        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(nit)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        return EmpresaMapper.toResponse(empresa);
    }

    public EmpresaResponseDTO actualizarEmpresa(String nit, EmpresaRequestDTO dto) {
        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(nit)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        empresa.setNombre(dto.getNombre());
        empresa.setCorreo(dto.getCorreo());

        return EmpresaMapper.toResponse(empresaRepository.save(empresa));
    }

    public void eliminarEmpresa(String nit) {
        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(nit)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        empresa.setDeleted(true);
        empresaRepository.save(empresa);
    }
}