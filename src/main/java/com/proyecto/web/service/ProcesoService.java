package com.proyecto.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.HistorialProcesoResponseDTO;
import com.proyecto.web.dto.HistorialProcesoResumenDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.entity.Empleado;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.entity.HistorialProceso;
import com.proyecto.web.entity.Pool;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.enums.EstadoProceso;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.mapper.ProcesoMapper;
import com.proyecto.web.repository.EmpleadoRepository;
import com.proyecto.web.repository.EmpresaRepository;
import com.proyecto.web.repository.HistorialProcesoRepository;
import com.proyecto.web.repository.PoolRepository;
import com.proyecto.web.repository.ProcesoRepository;
import com.proyecto.web.security.UsuarioPrincipal;
import com.proyecto.web.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ProcesoService {

    private static final int LIMITE_HISTORIAL_POR_DEFECTO = 50;
    private static final int LIMITE_HISTORIAL_MAXIMO = 200;

    private final ProcesoRepository procesoRepository;
    private final HistorialProcesoRepository historialProcesoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final EmpresaRepository empresaRepository;
    private final PoolRepository poolRepository;
    private final ObjectMapper objectMapper;

    public ProcesoService(
            ProcesoRepository procesoRepository,
            HistorialProcesoRepository historialProcesoRepository,
            EmpleadoRepository empleadoRepository,
            EmpresaRepository empresaRepository,
            PoolRepository poolRepository,
            ObjectMapper objectMapper) {
        this.procesoRepository = procesoRepository;
        this.historialProcesoRepository = historialProcesoRepository;
        this.empleadoRepository = empleadoRepository;
        this.empresaRepository = empresaRepository;
        this.poolRepository = poolRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProcesoResponseDTO crearProceso(ProcesoRequestDTO dto) {
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();

        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(nitEmpresa)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada", HttpStatus.NOT_FOUND));

        Pool pool = resolverPool(dto, empresa);

        Proceso proceso = ProcesoMapper.toEntity(dto, empresa, pool);
        Proceso guardado = procesoRepository.save(proceso);

        log.debug("Proceso creado id={} empresa={}", guardado.getId(), empresa.getNit());

        return ProcesoMapper.toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<ProcesoResponseDTO> obtenerProcesos(Long poolId) {
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();

        if (poolId != null) {
            Pool pool = poolRepository.findById(poolId)
                    .filter(p -> !p.isEliminado())
                    .orElseThrow(() -> new BusinessException("Pool no encontrado", HttpStatus.NOT_FOUND));
            validarPerteneceAEmpresa(pool, nitEmpresa);

            return procesoRepository
                    .findAllByEmpresa_NitAndPool_IdAndEstadoNotOrderByIdDesc(nitEmpresa, poolId, EstadoProceso.INACTIVO)
                    .stream()
                    .map(ProcesoMapper::toResponse)
                    .toList();
        }

        return procesoRepository
                .findAllByEmpresa_NitAndEstadoNotOrderByIdDesc(nitEmpresa, EstadoProceso.INACTIVO)
                .stream()
                .map(ProcesoMapper::toResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public ProcesoResponseDTO obtenerProceso(Long id) {
        return ProcesoMapper.toResponse(buscarVigente(id));
    }


    /**
     * Endpoint de soporte para pantallas de detalle: no consulta historial ni colecciones pesadas.
     * Así el encabezado/detalle del proceso puede pintarse de inmediato y el historial se carga aparte.
     */
    @Transactional(readOnly = true)
    public ProcesoResponseDTO obtenerDetalleProcesoRapido(Long id) {
        return obtenerProceso(id);
    }


    @Transactional(readOnly = true)
    public List<ProcesoResponseDTO> obtenerPorCategoria(String categoria) {
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();

        return procesoRepository
                .findAllByCategoriaAndEstadoNotOrderByIdDesc(categoria, EstadoProceso.INACTIVO)
                .stream()
                .filter(p -> p.getEmpresa() != null && nitEmpresa.equals(p.getEmpresa().getNit()))
                .map(ProcesoMapper::toResponse)
                .toList();
    }


    @Transactional
    public ProcesoResponseDTO actualizarProceso(Long id, ProcesoRequestDTO dto) {
        Proceso proceso = buscarVigente(id);
        Long idEmpleado = SecurityUtils.currentUser().map(UsuarioPrincipal::getEmpleadoId).orElse(null);

        String valorAnterior = serializar(ProcesoMapper.toResponse(proceso));

        proceso.setNombre(dto.getNombre());
        proceso.setDescripcion(dto.getDescripcion());
        proceso.setCategoria(dto.getCategoria());
        proceso.setEstado(ProcesoMapper.resolveEstadoDesdeDto(dto));

        if (dto.getPoolId() != null) {
            Pool pool = poolRepository.findById(dto.getPoolId())
                    .filter(p -> !p.isEliminado())
                    .orElseThrow(() -> new BusinessException("Pool no válido para la empresa", HttpStatus.BAD_REQUEST));
            validarPerteneceAEmpresa(pool, proceso.getEmpresa().getNit());
            proceso.setPool(pool);
        }

        Proceso guardado = procesoRepository.save(proceso);

        String valorNuevo = serializar(ProcesoMapper.toResponse(guardado));

        registrarHistorial(guardado, idEmpleado, valorAnterior, valorNuevo, "EDICION");

        return ProcesoMapper.toResponse(guardado);
    }


    @Transactional
    public void eliminarProceso(Long id) {
        Proceso proceso = buscarVigente(id);
        Long idEmpleado = SecurityUtils.currentUser().map(UsuarioPrincipal::getEmpleadoId).orElse(null);

        String valorAnterior = serializar(ProcesoMapper.toResponse(proceso));

        proceso.setEstado(EstadoProceso.INACTIVO);

        Proceso guardado = procesoRepository.save(proceso);

        registrarHistorial(guardado, idEmpleado, valorAnterior, null, "ELIMINACION");

        log.info("Proceso marcado INACTIVO id={}", id);
    }


    @Transactional(readOnly = true)
    public List<HistorialProceso> obtenerHistorialDeProceso(Long idProceso) {
        return historialProcesoRepository.findAllByProceso_IdOrderByFechaCambioDesc(idProceso);
    }

    @Transactional(readOnly = true)
    public List<HistorialProcesoResponseDTO> obtenerHistorialProcesoParaEmpresa(Long idProceso) {
        return obtenerHistorialProcesoParaEmpresa(idProceso, LIMITE_HISTORIAL_POR_DEFECTO);
    }


    @Transactional(readOnly = true)
    public List<HistorialProcesoResponseDTO> obtenerHistorialProcesoParaEmpresa(Long idProceso, Integer limite) {
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();
        buscarVigente(idProceso);

        int limiteSeguro = normalizarLimiteHistorial(limite);

        return historialProcesoRepository.findDetallePorProcesoYEmpresa(
                idProceso,
                nitEmpresa,
                PageRequest.of(0, limiteSeguro));
    }


    @Transactional(readOnly = true)
    public HistorialProcesoResumenDTO obtenerResumenHistorialProceso(Long idProceso, Integer limite) {
        buscarVigente(idProceso);

        int limiteSeguro = normalizarLimiteHistorial(limite);

        long total = historialProcesoRepository.countByProceso_Id(idProceso);

        return HistorialProcesoResumenDTO.builder()
                .idProceso(idProceso)
                .totalCambios(total)
                .limiteAplicado(limiteSeguro)
                .hayMas(total > limiteSeguro)
                .build();
    }


    @Transactional(readOnly = true)
    public Proceso buscarVigente(Long id) {
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();
        Proceso proceso = procesoRepository
                .findByIdAndEstadoNot(id, EstadoProceso.INACTIVO)
                .orElseThrow(() -> new BusinessException("Proceso no encontrado", HttpStatus.NOT_FOUND));
        validarPerteneceAEmpresa(proceso, nitEmpresa);
        return proceso;
    }


    /**
     * Uso restringido, por ejemplo administrador global:
     * proceso no inactivo sin filtrar empresa.
     */
    @Transactional(readOnly = true)
    public Proceso buscarVigenteGlobal(Long id) {
        return procesoRepository
                .findByIdAndEstadoNot(id, EstadoProceso.INACTIVO)
                .orElseThrow(() -> new BusinessException("Proceso no encontrado", HttpStatus.NOT_FOUND));
    }

    private Pool resolverPool(ProcesoRequestDTO dto, Empresa empresa) {
        if (dto.getPoolId() != null) {
            Pool pool = poolRepository.findById(dto.getPoolId())
                    .filter(p -> !p.isEliminado())
                    .orElseThrow(() -> new BusinessException("Pool no válido para la empresa", HttpStatus.BAD_REQUEST));
            validarPerteneceAEmpresa(pool, empresa.getNit());
            return pool;
        }

        return poolRepository
                .findByEmpresa_NitAndEsDefaultTrueAndEliminadoFalse(empresa.getNit())
                .orElseThrow(() -> new BusinessException(
                        "La empresa no tiene pool por defecto; cree la empresa con EmpresaService para generarlo",
                        HttpStatus.CONFLICT));
    }

    private int normalizarLimiteHistorial(Integer limite) {
        if (limite == null || limite <= 0) {
            return LIMITE_HISTORIAL_POR_DEFECTO;
        }

        return Math.min(limite, LIMITE_HISTORIAL_MAXIMO);
    }

    private void registrarHistorial(
            Proceso proceso,
            Long idEmpleado,
            String valorAnterior,
            String valorNuevo,
            String tipoAccion) {
        Empleado empleado = null;

        if (idEmpleado != null) {
            empleado = empleadoRepository.findByIdAndDeletedFalse(idEmpleado).orElse(null);
        }

        HistorialProceso historial = HistorialProceso.builder()
                .proceso(proceso)
                .empleado(empleado)
                .valorAnterior(valorAnterior)
                .valorNuevo(valorNuevo)
                .fechaCambio(LocalDateTime.now())
                .tipoAccion(tipoAccion)
                .build();

        historialProcesoRepository.save(historial);
    }

    private String serializar(Object objeto) {
        try {
            return objectMapper.writeValueAsString(objeto);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Error al serializar objeto a JSON", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validarPerteneceAEmpresa(Proceso proceso, String nitEmpresa) {
        if (proceso.getEmpresa() == null || !nitEmpresa.equals(proceso.getEmpresa().getNit())) {
            throw new AccessDeniedException("El recurso no pertenece a la empresa autenticada.");
        }
    }

    private void validarPerteneceAEmpresa(Pool pool, String nitEmpresa) {
        if (pool.getEmpresa() == null || !nitEmpresa.equals(pool.getEmpresa().getNit())) {
            throw new AccessDeniedException("El recurso no pertenece a la empresa autenticada.");
        }
    }
}
