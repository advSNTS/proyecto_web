package com.proyecto.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.ActividadRequestDTO;
import com.proyecto.web.dto.ActividadResponseDTO;
import com.proyecto.web.dto.HistorialActividadResponseDTO;
import com.proyecto.web.entity.Actividad;
import com.proyecto.web.entity.Empleado;
import com.proyecto.web.entity.HistorialActividad;
import com.proyecto.web.entity.Nodo;
import com.proyecto.web.mapper.ActividadMapper;
import com.proyecto.web.mapper.HistorialActividadMapper;
import com.proyecto.web.repository.ActividadRepository;
import com.proyecto.web.repository.EmpleadoRepository;
import com.proyecto.web.repository.HistorialActividadRepository;
import com.proyecto.web.repository.NodoRepository;
import lombok.RequiredArgsConstructor;
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
 
    // ─── CRUD ────────────────────────────────────────────────────────────────────
 
    public ActividadResponseDTO crearActividad(ActividadRequestDTO dto) {
        Nodo nodo = buscarNodo(dto.getNodoId());
 
        if (actividadRepository.existsByNodo_IdAndDeletedFalse(dto.getNodoId())) {
            throw new RuntimeException("El nodo ya tiene una actividad asignada");
        }
 
        Actividad actividad = ActividadMapper.toEntity(dto, nodo);
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
 
    // ─── Con historial automático ─────────────────────────────────────────────
 
    /**
     * @param id         ID de la actividad (= ID del nodo)
     * @param dto        Nuevos valores
     * @param idEmpleado Responsable del cambio — puede ser null
     */
    @Transactional
    public ActividadResponseDTO actualizarActividad(Long id, ActividadRequestDTO dto, Long idEmpleado) {
        Actividad actividad = buscarActiva(id);
 
        String valorAnterior = serializar(ActividadMapper.toResponse(actividad));
 
        actividad.setDescripcion(dto.getDescripcion());
 
        // Permite reasignar el nodo si se manda uno diferente
        if (!actividad.getNodo().getId().equals(dto.getNodoId())) {
            Nodo nodo = buscarNodo(dto.getNodoId());
            actividad.setNodo(nodo);
        }
 
        Actividad guardada = actividadRepository.save(actividad);
        String valorNuevo = serializar(ActividadMapper.toResponse(guardada));
 
        registrarHistorial(guardada, idEmpleado, valorAnterior, valorNuevo, "EDICION");
 
        return ActividadMapper.toResponse(guardada);
    }
 
    /**
     * @param id         ID de la actividad
     * @param idEmpleado Responsable — puede ser null
     */
    @Transactional
    public void eliminarActividad(Long id, Long idEmpleado) {
        Actividad actividad = buscarActiva(id);
 
        String valorAnterior = serializar(ActividadMapper.toResponse(actividad));
 
        actividad.setDeleted(true);
        Actividad guardada = actividadRepository.save(actividad);
 
        // valorNuevo = null — actividad eliminada
        registrarHistorial(guardada, idEmpleado, valorAnterior, null, "ELIMINACION");
    }
 
    // ─── Historial (lectura) ──────────────────────────────────────────────────
 
    public List<HistorialActividadResponseDTO> obtenerHistorial(Long idActividad) {
        return historialActividadRepository
                .findAllByActividad_IdOrderByFechaCambioDesc(idActividad)
                .stream()
                .map(HistorialActividadMapper::toResponse)
                .toList();
    }
 
    // ─── Helpers privados ─────────────────────────────────────────────────────
 
    private Actividad buscarActiva(Long id) {
        return actividadRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
    }
 
    private Nodo buscarNodo(Long nodoId) {
        return nodoRepository.findById(nodoId)
                .orElseThrow(() -> new RuntimeException("Nodo no encontrado"));
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