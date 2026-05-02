package com.proyecto.web.service;

import com.proyecto.web.dto.PoolResponseDTO;
import com.proyecto.web.entity.Pool;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.PoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PoolService {

    private final PoolRepository poolRepository;

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
