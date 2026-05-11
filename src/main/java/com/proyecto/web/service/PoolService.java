package com.proyecto.web.service;

import com.proyecto.web.dto.PoolRequestDTO;
import com.proyecto.web.dto.PoolResponseDTO;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.entity.Pool;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.EmpresaRepository;
import com.proyecto.web.repository.PoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PoolService {

    private final PoolRepository poolRepository;
    private final EmpresaRepository empresaRepository;

    public List<PoolResponseDTO> listarPorEmpresa(String nitEmpresa) {
        validarNit(nitEmpresa);
        return poolRepository.findAllByEmpresa_NitAndEliminadoFalse(nitEmpresa).stream()
                .map(this::toDto)
                .toList();
    }

    public PoolResponseDTO obtener(Long id, String nitEmpresa) {
        validarNit(nitEmpresa);
        Pool pool = poolRepository.findByIdAndEmpresa_NitAndEliminadoFalse(id, nitEmpresa)
                .orElseThrow(() -> new BusinessException("Pool no encontrado", HttpStatus.NOT_FOUND));
        return toDto(pool);
    }

    @Transactional
    public PoolResponseDTO crear(String nitEmpresa, PoolRequestDTO dto) {
        validarNit(nitEmpresa);
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

    private void validarNit(String nit) {
        if (nit == null || nit.isBlank()) {
            throw new BusinessException("nitEmpresa es obligatorio", HttpStatus.BAD_REQUEST);
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
