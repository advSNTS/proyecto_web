package com.proyecto.web.service;

import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.EmpresaResponseDTO;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.entity.Pool;
import com.proyecto.web.mapper.EmpresaMapper;
import com.proyecto.web.repository.EmpresaRepository;
import com.proyecto.web.repository.PoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmpresaService {

    private static final String EMPRESA_NO_ENCONTRADA = "Empresa no encontrada";

    private final EmpresaRepository empresaRepository;
    private final PoolRepository poolRepository;

    public EmpresaResponseDTO crearEmpresa(EmpresaRequestDTO dto) {
        Empresa empresa = EmpresaMapper.toEntity(dto);
        empresa = empresaRepository.save(empresa);
        Pool poolDefault = Pool.builder()
                .empresa(empresa)
                .nombre("Pool por defecto")
                .descripcion("Creado automáticamente al registrar la empresa")
                .esDefault(true)
                .eliminado(false)
                .build();
        poolRepository.save(poolDefault);
        log.info("Empresa y pool por defecto creados nit={}", empresa.getNit());
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
                .orElseThrow(() -> new RuntimeException(EMPRESA_NO_ENCONTRADA));
        return EmpresaMapper.toResponse(empresa);
    }

    public EmpresaResponseDTO actualizarEmpresa(String nit, EmpresaRequestDTO dto) {
        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(nit)
                .orElseThrow(() -> new RuntimeException(EMPRESA_NO_ENCONTRADA));

        empresa.setNombre(dto.getNombre());
        empresa.setCorreo(dto.getCorreo());

        return EmpresaMapper.toResponse(empresaRepository.save(empresa));
    }

    public void eliminarEmpresa(String nit) {
        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(nit)
                .orElseThrow(() -> new RuntimeException(EMPRESA_NO_ENCONTRADA));

        empresa.setDeleted(true);
        empresaRepository.save(empresa);
    }
}