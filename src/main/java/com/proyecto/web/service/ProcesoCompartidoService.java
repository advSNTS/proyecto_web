package com.proyecto.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.ProcesoCompartidoRequestDTO;
import com.proyecto.web.dto.ProcesoCompartidoResponseDTO;
import com.proyecto.web.entity.Empleado;
import com.proyecto.web.entity.HistorialProceso;
import com.proyecto.web.entity.Pool;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.entity.ProcesoCompartido;
import com.proyecto.web.enums.TipoRolSistema;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.EmpleadoRepository;
import com.proyecto.web.repository.EmpleadoRolSistemaRepository;
import com.proyecto.web.repository.HistorialProcesoRepository;
import com.proyecto.web.repository.PoolRepository;
import com.proyecto.web.repository.ProcesoCompartidoRepository;
import com.proyecto.web.security.UsuarioPrincipal;
import com.proyecto.web.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ProcesoCompartidoService {

    private final ProcesoService procesoService;
    private final PoolRepository poolRepository;
    private final ProcesoCompartidoRepository procesoCompartidoRepository;
    private final EmpleadoRolSistemaRepository empleadoRolSistemaRepository;
    private final HistorialProcesoRepository historialProcesoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ObjectMapper objectMapper;

    public ProcesoCompartidoService(
            ProcesoService procesoService,
            PoolRepository poolRepository,
            ProcesoCompartidoRepository procesoCompartidoRepository,
            EmpleadoRolSistemaRepository empleadoRolSistemaRepository,
            HistorialProcesoRepository historialProcesoRepository,
            EmpleadoRepository empleadoRepository,
            ObjectMapper objectMapper) {
        this.procesoService = procesoService;
        this.poolRepository = poolRepository;
        this.procesoCompartidoRepository = procesoCompartidoRepository;
        this.empleadoRolSistemaRepository = empleadoRolSistemaRepository;
        this.historialProcesoRepository = historialProcesoRepository;
        this.empleadoRepository = empleadoRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProcesoCompartidoResponseDTO compartir(Long procesoId, ProcesoCompartidoRequestDTO dto) {
        UsuarioPrincipal currentUser = SecurityUtils.requireCurrentUser();
        Long empleadoId = currentUser.getEmpleadoId();
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();
        if (empleadoId == null) {
            throw new BusinessException("Se requiere empleado autenticado para compartir", HttpStatus.UNAUTHORIZED);
        }
        if (!empleadoRolSistemaRepository.existsByEmpleado_IdAndEmpresa_NitAndTipoRolAndEliminadoFalse(
                empleadoId, nitEmpresa, TipoRolSistema.ADMIN)) {
            throw new BusinessException("Solo administradores de sistema pueden compartir procesos", HttpStatus.FORBIDDEN);
        }

        Proceso proceso = procesoService.buscarVigente(procesoId);
        Pool pool = poolRepository.findById(dto.getPoolId())
                .filter(p -> !p.isEliminado())
                .orElseThrow(() -> new BusinessException("Pool no encontrado en la empresa", HttpStatus.NOT_FOUND));
        validarPerteneceAEmpresa(pool, nitEmpresa);

        if (procesoCompartidoRepository.existsByProceso_IdAndPool_IdAndEliminadoFalse(procesoId, pool.getId())) {
            throw new BusinessException("El proceso ya está compartido con ese pool", HttpStatus.CONFLICT);
        }

        ProcesoCompartido pc = ProcesoCompartido.builder()
                .proceso(proceso)
                .pool(pool)
                .permiso(dto.getPermiso())
                .eliminado(false)
                .build();
        pc = procesoCompartidoRepository.save(pc);

        registrarHistorial(proceso, empleadoId, Map.of("accion", "COMPARTIR", "poolId", pool.getId(), "permiso", dto.getPermiso().name()));

        log.info("Proceso {} compartido con pool {}", procesoId, pool.getId());
        return toDto(pc);
    }


    @Transactional(readOnly = true)
    public List<ProcesoCompartidoResponseDTO> listarPorProceso(Long procesoId) {
        procesoService.buscarVigente(procesoId);
        return procesoCompartidoRepository.findAllByProceso_IdAndEliminadoFalse(procesoId).stream()
                .map(this::toDto)
                .toList();
    }


    private void registrarHistorial(Proceso proceso, Long idEmpleado, Map<String, Object> cambios) {
        Empleado empleado = null;
        if (idEmpleado != null) {
            empleado = empleadoRepository.findByIdAndDeletedFalse(idEmpleado).orElse(null);
        }
        String json;
        try {
            json = objectMapper.writeValueAsString(cambios);
        } catch (JsonProcessingException e) {
            json = cambios.toString();
        }
        HistorialProceso historial = HistorialProceso.builder()
                .proceso(proceso)
                .empleado(empleado)
                .valorAnterior(null)
                .valorNuevo(json)
                .fechaCambio(LocalDateTime.now())
                .tipoAccion("COMPARTIR")
                .build();
        historialProcesoRepository.save(historial);
    }

    private ProcesoCompartidoResponseDTO toDto(ProcesoCompartido pc) {
        return ProcesoCompartidoResponseDTO.builder()
                .id(pc.getId())
                .procesoId(pc.getProceso().getId())
                .poolId(pc.getPool().getId())
                .permiso(pc.getPermiso())
                .build();
    }

    private void validarPerteneceAEmpresa(Pool pool, String nitEmpresa) {
        if (pool.getEmpresa() == null || !nitEmpresa.equals(pool.getEmpresa().getNit())) {
            throw new AccessDeniedException("El recurso no pertenece a la empresa autenticada.");
        }
    }
}
