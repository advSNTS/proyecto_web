package com.proyecto.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.ActividadRequestDTO;
import com.proyecto.web.dto.ActividadResponseDTO;
import com.proyecto.web.dto.HistorialActividadResponseDTO;
import com.proyecto.web.entity.Actividad;
import com.proyecto.web.entity.Arco;
import com.proyecto.web.entity.Empleado;
import com.proyecto.web.entity.HistorialActividad;
import com.proyecto.web.entity.Lane;
import com.proyecto.web.entity.Nodo;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.mapper.ActividadMapper;
import com.proyecto.web.mapper.HistorialActividadMapper;
import com.proyecto.web.repository.ActividadRepository;
import com.proyecto.web.repository.ArcoRepository;
import com.proyecto.web.repository.EmpleadoRepository;
import com.proyecto.web.repository.HistorialActividadRepository;
import com.proyecto.web.repository.LaneRepository;
import com.proyecto.web.repository.NodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActividadService {

    private final ActividadRepository actividadRepository;
    private final NodoRepository nodoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final HistorialActividadRepository historialActividadRepository;
    private final ObjectMapper objectMapper;
    private final ArcoRepository arcoRepository;
    private final LaneRepository laneRepository;

    public ActividadResponseDTO crearActividad(ActividadRequestDTO dto) {
        Nodo nodo = buscarNodo(dto.getNodoId());

        if (actividadRepository.existsByNodo_IdAndDeletedFalse(dto.getNodoId())) {
            throw new RuntimeException("El nodo ya tiene una actividad asignada");
        }

        Actividad actividad = ActividadMapper.toEntity(dto, nodo);
        actividad.setLane(resolverLaneParaProceso(nodo.getProceso(), dto.getLaneId()));
        return ActividadMapper.toResponse(actividadRepository.save(actividad));
    }

    public ActividadResponseDTO obtenerActividad(Long id) {
        return ActividadMapper.toResponse(buscarActiva(id));
    }

    public List<ActividadResponseDTO> obtenerPorProceso(Long procesoId) {
        return actividadRepository.findAllByNodo_Proceso_IdAndDeletedFalse(procesoId)
                .stream()
                .map(ActividadMapper::toResponse)
                .toList();
    }

    @Transactional
    public ActividadResponseDTO actualizarActividad(Long id, ActividadRequestDTO dto, Long idEmpleado) {
        Actividad actividad = buscarActiva(id);

        String valorAnterior = serializar(ActividadMapper.toResponse(actividad));

        actividad.setDescripcion(dto.getDescripcion());
        actividad.setTipoActividad(dto.getTipoActividad());
        actividad.setLane(resolverLaneParaProceso(actividad.getNodo().getProceso(), dto.getLaneId()));

        if (!actividad.getNodo().getId().equals(dto.getNodoId())) {
            Nodo nodo = buscarNodo(dto.getNodoId());
            actividad.setNodo(nodo);
        }

        Actividad guardada = actividadRepository.save(actividad);
        String valorNuevo = serializar(ActividadMapper.toResponse(guardada));

        registrarHistorial(guardada, idEmpleado, valorAnterior, valorNuevo, "EDICION");

        return ActividadMapper.toResponse(guardada);
    }

    @Transactional
    public void eliminarActividad(Long id, Long idEmpleado) {
        Actividad actividad = buscarActiva(id);

        String valorAnterior = serializar(ActividadMapper.toResponse(actividad));

        marcarArcosEliminadosPorNodo(actividad.getNodo().getId());
        actividad.getNodo().setEliminado(true);
        nodoRepository.save(actividad.getNodo());

        actividad.setDeleted(true);
        Actividad guardada = actividadRepository.save(actividad);

        registrarHistorial(guardada, idEmpleado, valorAnterior, null, "ELIMINACION");
    }

    public List<HistorialActividadResponseDTO> obtenerHistorial(Long idActividad) {
        return historialActividadRepository
                .findAllByActividad_IdOrderByFechaCambioDesc(idActividad)
                .stream()
                .map(HistorialActividadMapper::toResponse)
                .toList();
    }

    private Actividad buscarActiva(Long id) {
        return actividadRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
    }

    private Nodo buscarNodo(Long nodoId) {
        return nodoRepository.findByIdAndEliminadoFalse(nodoId)
                .orElseThrow(() -> new RuntimeException("Nodo no encontrado"));
    }

    private Lane resolverLaneParaProceso(Proceso proceso, Long laneId) {
        if (laneId == null) {
            return null;
        }
        Lane lane = laneRepository.findById(laneId)
                .orElseThrow(() -> new BusinessException("Lane no encontrada", HttpStatus.NOT_FOUND));
        if (lane.isEliminado() || !lane.getPool().getId().equals(proceso.getPool().getId())) {
            throw new BusinessException("Lane no válida para el proceso", HttpStatus.BAD_REQUEST);
        }
        return lane;
    }

    private void marcarArcosEliminadosPorNodo(Long nodoId) {
        List<Arco> afectados = arcoRepository.findAllByNodoOrigen_IdOrNodoDestino_IdAndEliminadoFalse(nodoId, nodoId);
        afectados.forEach(a -> a.setEliminado(true));
        arcoRepository.saveAll(afectados);
    }

    private void registrarHistorial(Actividad actividad, Long idEmpleado,
                                    String valorAnterior, String valorNuevo,
                                    String tipoAccion) {
        Empleado empleado = null;
        if (idEmpleado != null) {
            empleado = empleadoRepository.findByIdAndDeletedFalse(idEmpleado).orElse(null);
        }

        HistorialActividad historial = HistorialActividad.builder()
                .actividad(actividad)
                .empleado(empleado)
                .valorAnterior(valorAnterior)
                .valorNuevo(valorNuevo)
                .fechaCambio(LocalDateTime.now())
                .tipoAccion(tipoAccion)
                .build();

        historialActividadRepository.save(historial);
    }

    private String serializar(Object objeto) {
        try {
            return objectMapper.writeValueAsString(objeto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al serializar objeto a JSON", e);
        }
    }
}
