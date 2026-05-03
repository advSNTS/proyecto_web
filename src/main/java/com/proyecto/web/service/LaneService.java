package com.proyecto.web.service;

import com.proyecto.web.dto.LaneRequestDTO;
import com.proyecto.web.dto.LaneResponseDTO;
import com.proyecto.web.entity.Lane;
import com.proyecto.web.entity.Pool;
import com.proyecto.web.entity.Rol;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.LaneRepository;
import com.proyecto.web.repository.PoolRepository;
import com.proyecto.web.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LaneService {

    private final LaneRepository laneRepository;
    private final PoolRepository poolRepository;
    private final RolRepository rolRepository;

    public LaneResponseDTO crear(String nitEmpresa, LaneRequestDTO dto) {
        Pool pool = poolRepository.findByIdAndEmpresa_NitAndEliminadoFalse(dto.getPoolId(), nitEmpresa)
                .orElseThrow(() -> new BusinessException("Pool no encontrado", HttpStatus.NOT_FOUND));
        Lane lane = Lane.builder()
                .pool(pool)
                .nombre(dto.getNombre())
                .rolProceso(resolverRol(nitEmpresa, dto.getRolProcesoId()))
                .eliminado(false)
                .build();
        return toDto(laneRepository.save(lane));
    }

    public List<LaneResponseDTO> listarPorEmpresa(String nitEmpresa) {
        return laneRepository.findAllByPool_Empresa_NitAndEliminadoFalse(nitEmpresa).stream()
                .map(this::toDto)
                .toList();
    }

    public List<LaneResponseDTO> listarPorPool(String nitEmpresa, Long poolId) {
        poolRepository.findByIdAndEmpresa_NitAndEliminadoFalse(poolId, nitEmpresa)
                .orElseThrow(() -> new BusinessException("Pool no encontrado", HttpStatus.NOT_FOUND));
        return laneRepository.findAllByPool_IdAndEliminadoFalse(poolId).stream()
                .map(this::toDto)
                .toList();
    }

    public LaneResponseDTO obtener(String nitEmpresa, Long id) {
        Lane lane = laneRepository.findByIdAndPool_Empresa_NitAndEliminadoFalse(id, nitEmpresa)
                .orElseThrow(() -> new BusinessException("Lane no encontrada", HttpStatus.NOT_FOUND));
        return toDto(lane);
    }

    @Transactional
    public LaneResponseDTO actualizar(String nitEmpresa, Long id, LaneRequestDTO dto) {
        Lane lane = laneRepository.findByIdAndPool_Empresa_NitAndEliminadoFalse(id, nitEmpresa)
                .orElseThrow(() -> new BusinessException("Lane no encontrada", HttpStatus.NOT_FOUND));
        Pool pool = poolRepository.findByIdAndEmpresa_NitAndEliminadoFalse(dto.getPoolId(), nitEmpresa)
                .orElseThrow(() -> new BusinessException("Pool no encontrado", HttpStatus.NOT_FOUND));
        lane.setPool(pool);
        lane.setNombre(dto.getNombre());
        lane.setRolProceso(resolverRol(nitEmpresa, dto.getRolProcesoId()));
        return toDto(laneRepository.save(lane));
    }

    @Transactional
    public void eliminar(String nitEmpresa, Long id) {
        Lane lane = laneRepository.findByIdAndPool_Empresa_NitAndEliminadoFalse(id, nitEmpresa)
                .orElseThrow(() -> new BusinessException("Lane no encontrada", HttpStatus.NOT_FOUND));
        lane.setEliminado(true);
        laneRepository.save(lane);
    }

    private Rol resolverRol(String nit, Long rolId) {
        if (rolId == null) {
            return null;
        }
        return rolRepository.findByIdAndDeletedFalse(rolId)
                .filter(r -> r.getEmpresa().getNit().equals(nit))
                .orElseThrow(() -> new BusinessException("Rol de proceso no válido", HttpStatus.BAD_REQUEST));
    }

    private LaneResponseDTO toDto(Lane l) {
        return LaneResponseDTO.builder()
                .id(l.getId())
                .poolId(l.getPool().getId())
                .nombre(l.getNombre())
                .rolProcesoId(l.getRolProceso() != null ? l.getRolProceso().getId() : null)
                .build();
    }
}
