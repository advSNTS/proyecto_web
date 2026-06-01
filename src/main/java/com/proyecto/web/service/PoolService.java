package com.proyecto.web.service;

import com.proyecto.web.dto.PoolRequestDTO;
import com.proyecto.web.dto.PoolResponseDTO;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.entity.Pool;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.EmpresaRepository;
import com.proyecto.web.repository.PoolRepository;
import com.proyecto.web.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PoolService {

    private static final String MSG_POOL_NO_ENCONTRADO = "Pool no encontrado";

    private final PoolRepository poolRepository;
    private final EmpresaRepository empresaRepository;

    public PoolService(
            PoolRepository poolRepository,
            EmpresaRepository empresaRepository) {
        this.poolRepository = poolRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public List<PoolResponseDTO> listarPorEmpresa() {
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();
        return poolRepository.findAllByEmpresa_NitAndEliminadoFalse(nitEmpresa).stream()
                .map(this::toDto)
                .toList();
    }


    @Transactional(readOnly = true)
    public PoolResponseDTO obtener(Long id) {
        Pool pool = poolRepository.findById(id)
                .filter(p -> !p.isEliminado())
                .orElseThrow(() -> new BusinessException(MSG_POOL_NO_ENCONTRADO, HttpStatus.NOT_FOUND));
        validarPerteneceAEmpresa(pool, SecurityUtils.requireAuthenticatedNitEmpresa());
        return toDto(pool);
    }


    @Transactional
    public PoolResponseDTO crear(PoolRequestDTO dto) {
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();
        if (dto == null || dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new BusinessException("El nombre del pool es obligatorio", HttpStatus.BAD_REQUEST);
        }

        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(nitEmpresa)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada", HttpStatus.NOT_FOUND));

        String nombre = dto.getNombre().trim();
        if (poolRepository.findByEmpresa_NitAndNombreAndEliminadoFalse(nitEmpresa, nombre).isPresent()) {
            throw new BusinessException("Ya existe un pool con ese nombre en la empresa", HttpStatus.CONFLICT);
        }

        boolean esDefaultSolicitado = Boolean.TRUE.equals(dto.getEsDefault());
        if (esDefaultSolicitado) {
            poolRepository.findByEmpresa_NitAndEsDefaultTrueAndEliminadoFalse(nitEmpresa)
                    .ifPresent(actual -> {
                        actual.setEsDefault(false);
                        poolRepository.save(actual);
                    });
        }

        Pool pool = Pool.builder()
                .empresa(empresa)
                .nombre(nombre)
                .descripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null)
                .esDefault(esDefaultSolicitado)
                .eliminado(false)
                .build();

        return toDto(poolRepository.save(pool));
    }


    private void validarPerteneceAEmpresa(Pool pool, String nitEmpresa) {
        if (pool.getEmpresa() == null || !nitEmpresa.equals(pool.getEmpresa().getNit())) {
            throw new AccessDeniedException("El recurso no pertenece a la empresa autenticada.");
        }
    }

    private PoolResponseDTO toDto(Pool p) {
        return PoolResponseDTO.builder()
                .id(p.getId())
                .nitEmpresa(p.getEmpresa().getNit())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .esDefault(p.isEsDefault())
                .build();
    }
}
