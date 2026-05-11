package com.proyecto.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.HistorialProcesoResponseDTO;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcesoService {

    private final ProcesoRepository procesoRepository;
    private final HistorialProcesoRepository historialProcesoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final EmpresaRepository empresaRepository;
    private final PoolRepository poolRepository;
    private final ObjectMapper objectMapper;

    public ProcesoResponseDTO crearProceso(ProcesoRequestDTO dto) {
        if (dto.getNitEmpresa() == null || dto.getNitEmpresa().isBlank()) {
            throw new BusinessException("nitEmpresa es obligatorio al crear un proceso", HttpStatus.BAD_REQUEST);
        }
        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(dto.getNitEmpresa())
                .orElseThrow(() -> new BusinessException("Empresa no encontrada", HttpStatus.NOT_FOUND));
        Pool pool = resolverPool(dto, empresa);
        Proceso proceso = ProcesoMapper.toEntity(dto, empresa, pool);
        Proceso guardado = procesoRepository.save(proceso);
        log.debug("Proceso creado id={} empresa={}", guardado.getId(), empresa.getNit());
        return ProcesoMapper.toResponse(guardado);
    }

    public List<ProcesoResponseDTO> obtenerProcesos(String nitEmpresa, Long poolId) {
        if (nitEmpresa == null || nitEmpresa.isBlank()) {
            throw new BusinessException("nitEmpresa es obligatorio", HttpStatus.BAD_REQUEST);
        }
        if (poolId != null) {
            poolRepository.findByIdAndEmpresa_NitAndEliminadoFalse(poolId, nitEmpresa)
                    .orElseThrow(() -> new BusinessException("Pool no encontrado", HttpStatus.NOT_FOUND));
            return procesoRepository
                    .findAllByEmpresa_NitAndPool_IdAndEstadoNot(nitEmpresa, poolId, EstadoProceso.INACTIVO)
                    .stream()
                    .map(ProcesoMapper::toResponse)
                    .toList();
        }
        return procesoRepository
                .findAllByEmpresa_NitAndEstadoNot(nitEmpresa, EstadoProceso.INACTIVO)
                .stream()
                .map(ProcesoMapper::toResponse)
                .toList();
    }

    public ProcesoResponseDTO obtenerProceso(Long id, String nitEmpresa) {
        return ProcesoMapper.toResponse(buscarVigente(id, nitEmpresa));
    }

    public List<ProcesoResponseDTO> obtenerPorCategoria(String categoria, String nitEmpresa) {
        if (nitEmpresa == null || nitEmpresa.isBlank()) {
            throw new BusinessException("nitEmpresa es obligatorio", HttpStatus.BAD_REQUEST);
        }
        return procesoRepository
                .findAllByCategoriaAndEstadoNot(categoria, EstadoProceso.INACTIVO)
                .stream()
                .filter(p -> p.getEmpresa().getNit().equals(nitEmpresa))
                .map(ProcesoMapper::toResponse)
                .toList();
    }

    @Transactional
    public ProcesoResponseDTO actualizarProceso(Long id, ProcesoRequestDTO dto, Long idEmpleado, String nitEmpresa) {
        Proceso proceso = buscarVigente(id, nitEmpresa);

        String valorAnterior = serializar(ProcesoMapper.toResponse(proceso));

        proceso.setNombre(dto.getNombre());
        proceso.setDescripcion(dto.getDescripcion());
        proceso.setCategoria(dto.getCategoria());
        proceso.setEstado(ProcesoMapper.resolveEstadoDesdeDto(dto));
        if (dto.getPoolId() != null) {
            Pool pool = poolRepository.findByIdAndEmpresa_NitAndEliminadoFalse(dto.getPoolId(), proceso.getEmpresa().getNit())
                    .orElseThrow(() -> new BusinessException("Pool no válido para la empresa", HttpStatus.BAD_REQUEST));
            proceso.setPool(pool);
        }

        Proceso guardado = procesoRepository.save(proceso);
        String valorNuevo = serializar(ProcesoMapper.toResponse(guardado));
        registrarHistorial(guardado, idEmpleado, valorAnterior, valorNuevo, "EDICION");
        return ProcesoMapper.toResponse(guardado);
    }

    @Transactional
    public void eliminarProceso(Long id, Long idEmpleado, String nitEmpresa) {
        Proceso proceso = buscarVigente(id, nitEmpresa);
        String valorAnterior = serializar(ProcesoMapper.toResponse(proceso));
        proceso.setEstado(EstadoProceso.INACTIVO);
        Proceso guardado = procesoRepository.save(proceso);
        registrarHistorial(guardado, idEmpleado, valorAnterior, null, "ELIMINACION");
        log.info("Proceso marcado INACTIVO id={}", id);
    }

    public List<HistorialProceso> obtenerHistorialDeProceso(Long idProceso) {
        return historialProcesoRepository.findAllByProceso_IdOrderByFechaCambioDesc(idProceso);
    }

    @Transactional(readOnly = true)
    public List<HistorialProcesoResponseDTO> obtenerHistorialProcesoParaEmpresa(Long idProceso, String nitEmpresa) {
        buscarVigente(idProceso, nitEmpresa);
        return historialProcesoRepository.findDetallePorProcesoYEmpresa(idProceso, nitEmpresa);
    }

    public Proceso buscarVigente(Long id, String nitEmpresa) {
        if (nitEmpresa == null || nitEmpresa.isBlank()) {
            throw new BusinessException("nitEmpresa es obligatorio", HttpStatus.BAD_REQUEST);
        }
        return procesoRepository
                .findByIdAndEmpresa_NitAndEstadoNot(id, nitEmpresa, EstadoProceso.INACTIVO)
                .orElseThrow(() -> new BusinessException("Proceso no encontrado", HttpStatus.NOT_FOUND));
    }

    /** Uso restringido (p. ej. administrador global): proceso no inactivo sin filtrar empresa. */
    public Proceso buscarVigenteGlobal(Long id) {
        return procesoRepository
                .findByIdAndEstadoNot(id, EstadoProceso.INACTIVO)
                .orElseThrow(() -> new BusinessException("Proceso no encontrado", HttpStatus.NOT_FOUND));
    }

    private Pool resolverPool(ProcesoRequestDTO dto, Empresa empresa) {
        if (dto.getPoolId() != null) {
            return poolRepository.findByIdAndEmpresa_NitAndEliminadoFalse(dto.getPoolId(), empresa.getNit())
                    .orElseThrow(() -> new BusinessException("Pool no válido para la empresa", HttpStatus.BAD_REQUEST));
        }
        return poolRepository
                .findByEmpresa_NitAndEsDefaultTrueAndEliminadoFalse(empresa.getNit())
                .orElseThrow(() -> new BusinessException(
                        "La empresa no tiene pool por defecto; cree la empresa con EmpresaService para generarlo",
                        HttpStatus.CONFLICT));
    }

    private void registrarHistorial(Proceso proceso, Long idEmpleado,
                                    String valorAnterior, String valorNuevo,
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
}
