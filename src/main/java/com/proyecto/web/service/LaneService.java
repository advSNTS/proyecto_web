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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LaneService {

    private static final String MSG_POOL_NO_ENCONTRADO = "Pool no encontrado";
    private static final String MSG_LANE_NO_ENCONTRADA = "Lane no encontrada";
    private static final String AUTHORITY_ROLE_ADMIN = "ROLE_ADMIN";
    private static final String AUTHORITY_ROLE_EDITOR = "ROLE_EDITOR";

    private final LaneRepository laneRepository;
    private final PoolRepository poolRepository;
    private final RolRepository rolRepository;
    private final RolXEmpleadoRepository rolXEmpleadoRepository;

    @Transactional
    public LaneResponseDTO crear(LaneRequestDTO dto) {
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();
        Pool pool = poolRepository.findById(dto.getPoolId())
                .filter(p -> !p.isEliminado())
                .orElseThrow(() -> new BusinessException(MSG_POOL_NO_ENCONTRADO, HttpStatus.NOT_FOUND));
        validarPerteneceAEmpresa(pool, nitEmpresa);
        Lane lane = Lane.builder()
                .pool(pool)
                .nombre(dto.getNombre())
                .rolProceso(resolverRolObligatorio(nitEmpresa, dto.getRolProcesoId()))
                .eliminado(false)
                .build();
        return toDto(laneRepository.save(lane));
    }

    @Deprecated
    public LaneResponseDTO crear(String nitEmpresa, LaneRequestDTO dto) {
        return crear(dto);
    }

    @Transactional(readOnly = true)
    public List<LaneResponseDTO> listarPorEmpresa() {
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();
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

    @Deprecated
    public List<LaneResponseDTO> listarPorEmpresa(String nitEmpresa) {
        return listarPorEmpresa();
    }

    @Transactional(readOnly = true)
    public List<LaneResponseDTO> listarTodasPorEmpresa() {
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();
        poolRepository.findAllByEmpresa_NitAndEliminadoFalse(nitEmpresa);
        return laneRepository.findAllByPool_Empresa_NitAndEliminadoFalse(nitEmpresa).stream()
                .map(this::toDto)
                .toList();
    }

    @Deprecated
    public List<LaneResponseDTO> listarTodasPorEmpresa(String nitEmpresa) {
        return listarTodasPorEmpresa();
    }

    @Transactional(readOnly = true)
    public List<LaneResponseDTO> listarPorPool(Long poolId) {
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();
        Pool pool = poolRepository.findById(poolId)
                .filter(p -> !p.isEliminado())
                .orElseThrow(() -> new BusinessException(MSG_POOL_NO_ENCONTRADO, HttpStatus.NOT_FOUND));
        validarPerteneceAEmpresa(pool, nitEmpresa);

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

    @Deprecated
    public List<LaneResponseDTO> listarPorPool(String nitEmpresa, Long poolId) {
        return listarPorPool(poolId);
    }

    @Transactional(readOnly = true)
    public List<LaneResponseDTO> listarTodasPorPool(Long poolId) {
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();
        Pool pool = poolRepository.findById(poolId)
                .filter(p -> !p.isEliminado())
                .orElseThrow(() -> new BusinessException(MSG_POOL_NO_ENCONTRADO, HttpStatus.NOT_FOUND));
        validarPerteneceAEmpresa(pool, nitEmpresa);
        return laneRepository.findAllByPool_IdAndEliminadoFalse(poolId).stream()
                .map(this::toDto)
                .toList();
    }

    @Deprecated
    public List<LaneResponseDTO> listarTodasPorPool(String nitEmpresa, Long poolId) {
        return listarTodasPorPool(poolId);
    }

    @Transactional(readOnly = true)
    public LaneResponseDTO obtener(Long id) {
        return toDto(buscarPropia(id));
    }

    @Deprecated
    public LaneResponseDTO obtener(String nitEmpresa, Long id) {
        return obtener(id);
    }

    @Transactional
    public LaneResponseDTO actualizar(Long id, LaneRequestDTO dto) {
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();
        Lane lane = buscarPropia(id);
        Pool pool = poolRepository.findById(dto.getPoolId())
                .filter(p -> !p.isEliminado())
                .orElseThrow(() -> new BusinessException(MSG_POOL_NO_ENCONTRADO, HttpStatus.NOT_FOUND));
        validarPerteneceAEmpresa(pool, nitEmpresa);
        lane.setPool(pool);
        lane.setNombre(dto.getNombre());
        lane.setRolProceso(resolverRolObligatorio(nitEmpresa, dto.getRolProcesoId()));
        return toDto(laneRepository.save(lane));
    }

    @Deprecated
    public LaneResponseDTO actualizar(String nitEmpresa, Long id, LaneRequestDTO dto) {
        return actualizar(id, dto);
    }

    @Transactional
    public void eliminar(Long id) {
        Lane lane = buscarPropia(id);
        lane.setEliminado(true);
        laneRepository.save(lane);
    }

    @Deprecated
    public void eliminar(String nitEmpresa, Long id) {
        eliminar(id);
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
            if (AUTHORITY_ROLE_ADMIN.equals(value) || AUTHORITY_ROLE_EDITOR.equals(value)) {
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

    private Lane buscarPropia(Long id) {
        Lane lane = laneRepository.findById(id)
                .filter(l -> !l.isEliminado())
                .orElseThrow(() -> new BusinessException(MSG_LANE_NO_ENCONTRADA, HttpStatus.NOT_FOUND));
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();
        if (lane.getPool() == null
                || lane.getPool().getEmpresa() == null
                || !nitEmpresa.equals(lane.getPool().getEmpresa().getNit())) {
            throw new AccessDeniedException("El recurso no pertenece a la empresa autenticada.");
        }
        return lane;
    }

    private void validarPerteneceAEmpresa(Pool pool, String nitEmpresa) {
        if (pool.getEmpresa() == null || !nitEmpresa.equals(pool.getEmpresa().getNit())) {
            throw new AccessDeniedException("El recurso no pertenece a la empresa autenticada.");
        }
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
