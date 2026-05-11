package com.proyecto.web.service;

import com.proyecto.web.dto.LaneRequestDTO;
import com.proyecto.web.dto.LaneResponseDTO;
import com.proyecto.web.entity.Lane;
import com.proyecto.web.entity.Pool;
import com.proyecto.web.entity.Rol;
import com.proyecto.web.entity.RolXEmpleado;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.LaneRepository;
import com.proyecto.web.repository.PoolRepository;
import com.proyecto.web.repository.RolRepository;
import com.proyecto.web.repository.RolXEmpleadoRepository;
import com.proyecto.web.security.UsuarioPrincipal;
import com.proyecto.web.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LaneService {

    private final LaneRepository laneRepository;
    private final PoolRepository poolRepository;
    private final RolRepository rolRepository;
    private final RolXEmpleadoRepository rolXEmpleadoRepository;

    @Transactional
    public LaneResponseDTO crear(String nitEmpresa, LaneRequestDTO dto) {
        Pool pool = poolRepository.findByIdAndEmpresa_NitAndEliminadoFalse(dto.getPoolId(), nitEmpresa)
                .orElseThrow(() -> new BusinessException("Pool no encontrado", HttpStatus.NOT_FOUND));
        Lane lane = Lane.builder()
                .pool(pool)
                .nombre(dto.getNombre())
                .rolProceso(resolverRolObligatorio(nitEmpresa, dto.getRolProcesoId()))
                .eliminado(false)
                .build();
        return toDto(laneRepository.save(lane));
    }

    @Transactional(readOnly = true)
    public List<LaneResponseDTO> listarPorEmpresa(String nitEmpresa) {
        if (debeMostrarTodasLasLanes()) {
            return laneRepository.findAllByPool_Empresa_NitAndEliminadoFalse(nitEmpresa).stream()
                    .map(this::toDto)
                    .toList();
        }

        List<Long> rolIds = resolverRolIdsVisibles(nitEmpresa);
        if (rolIds.isEmpty()) {
            return List.of();
        }

        return laneRepository.findAllByPool_Empresa_NitAndEliminadoFalseAndRolProceso_IdIn(nitEmpresa, rolIds).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LaneResponseDTO> listarTodasPorEmpresa(String nitEmpresa) {
        poolRepository.findAllByEmpresa_NitAndEliminadoFalse(nitEmpresa);
        return laneRepository.findAllByPool_Empresa_NitAndEliminadoFalse(nitEmpresa).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LaneResponseDTO> listarPorPool(String nitEmpresa, Long poolId) {
        poolRepository.findByIdAndEmpresa_NitAndEliminadoFalse(poolId, nitEmpresa)
                .orElseThrow(() -> new BusinessException("Pool no encontrado", HttpStatus.NOT_FOUND));

        if (debeMostrarTodasLasLanes()) {
            return laneRepository.findAllByPool_IdAndEliminadoFalse(poolId).stream()
                    .map(this::toDto)
                    .toList();
        }

        List<Long> rolIds = resolverRolIdsVisibles(nitEmpresa);
        if (rolIds.isEmpty()) {
            return List.of();
        }

        return laneRepository.findAllByPool_IdAndEliminadoFalseAndRolProceso_IdIn(poolId, rolIds).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LaneResponseDTO> listarTodasPorPool(String nitEmpresa, Long poolId) {
        poolRepository.findByIdAndEmpresa_NitAndEliminadoFalse(poolId, nitEmpresa)
                .orElseThrow(() -> new BusinessException("Pool no encontrado", HttpStatus.NOT_FOUND));
        return laneRepository.findAllByPool_IdAndEliminadoFalse(poolId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public LaneResponseDTO obtener(String nitEmpresa, Long id) {
        Lane lane;
        if (debeMostrarTodasLasLanes()) {
            lane = laneRepository.findByIdAndPool_Empresa_NitAndEliminadoFalse(id, nitEmpresa)
                    .orElseThrow(() -> new BusinessException("Lane no encontrada", HttpStatus.NOT_FOUND));
        } else {
            List<Long> rolIds = resolverRolIdsVisibles(nitEmpresa);
            if (rolIds.isEmpty()) {
                throw new BusinessException("Lane no encontrada", HttpStatus.NOT_FOUND);
            }
            lane = laneRepository.findByIdAndPool_Empresa_NitAndEliminadoFalseAndRolProceso_IdIn(id, nitEmpresa, rolIds)
                    .orElseThrow(() -> new BusinessException("Lane no encontrada", HttpStatus.NOT_FOUND));
        }
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
        lane.setRolProceso(resolverRolObligatorio(nitEmpresa, dto.getRolProcesoId()));
        return toDto(laneRepository.save(lane));
    }

    @Transactional
    public void eliminar(String nitEmpresa, Long id) {
        Lane lane = laneRepository.findByIdAndPool_Empresa_NitAndEliminadoFalse(id, nitEmpresa)
                .orElseThrow(() -> new BusinessException("Lane no encontrada", HttpStatus.NOT_FOUND));
        lane.setEliminado(true);
        laneRepository.save(lane);
    }

    private Rol resolverRolObligatorio(String nit, Long rolId) {
        if (rolId == null) {
            throw new BusinessException("rolProcesoId es obligatorio", HttpStatus.BAD_REQUEST);
        }
        return rolRepository.findByIdAndDeletedFalse(rolId)
                .filter(r -> r.getEmpresa().getNit().equals(nit))
                .orElseThrow(() -> new BusinessException("Rol de proceso no valido", HttpStatus.BAD_REQUEST));
    }

    private boolean debeMostrarTodasLasLanes() {
        Optional<UsuarioPrincipal> currentUser = SecurityUtils.currentUser();
        return currentUser.isEmpty()
                || currentUser.map(UsuarioPrincipal::isAdminGlobal).orElse(false)
                || currentUser.map(this::tieneRolSistemaGestion).orElse(false);
    }

    private boolean tieneRolSistemaGestion(UsuarioPrincipal principal) {
        for (GrantedAuthority authority : principal.getAuthorities()) {
            String value = authority.getAuthority();
            if ("ROLE_ADMIN".equals(value) || "ROLE_EDITOR".equals(value)) {
                return true;
            }
        }
        return false;
    }

    private List<Long> resolverRolIdsVisibles(String nitEmpresa) {
        Optional<UsuarioPrincipal> currentUser = SecurityUtils.currentUser();
        if (currentUser.isEmpty()
                || currentUser.get().isAdminGlobal()
                || tieneRolSistemaGestion(currentUser.get())) {
            return List.of();
        }

        Long empleadoId = currentUser.get().getEmpleadoId();
        if (empleadoId == null) {
            return List.of();
        }

        return rolXEmpleadoRepository.findAllByEmpleado_IdAndDeletedFalse(empleadoId).stream()
                .map(RolXEmpleado::getRol)
                .filter(rol -> rol != null && rol.getEmpresa() != null && nitEmpresa.equals(rol.getEmpresa().getNit()))
                .map(Rol::getId)
                .distinct()
                .toList();
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
