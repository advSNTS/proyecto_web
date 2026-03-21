package com.proyecto.web.service;
 
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.entity.Empleado;
import com.proyecto.web.entity.HistorialProceso;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.mapper.ProcesoMapper;
import com.proyecto.web.repository.EmpleadoRepository;
import com.proyecto.web.repository.HistorialProcesoRepository;
import com.proyecto.web.repository.ProcesoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.time.LocalDateTime;
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class ProcesoService {
 
    private final ProcesoRepository procesoRepository;
    private final HistorialProcesoRepository historialProcesoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ObjectMapper objectMapper; // Bean de Jackson — ya viene con Spring Boot
 
    // ─── CRUD básico (sin historial) ────────────────────────────────────────────
 
    public ProcesoResponseDTO crearProceso(ProcesoRequestDTO dto) {
        Proceso proceso = ProcesoMapper.toEntity(dto);
        return ProcesoMapper.toResponse(procesoRepository.save(proceso));
    }
 
    public List<ProcesoResponseDTO> obtenerProcesos() {
        return procesoRepository.findAllByActivoTrue()
                .stream()
                .map(ProcesoMapper::toResponse)
                .toList();
    }
 
    public ProcesoResponseDTO obtenerProceso(Long id) {
        return ProcesoMapper.toResponse(buscarActivo(id));
    }
 
    public List<ProcesoResponseDTO> obtenerPorCategoria(String categoria) {
        return procesoRepository.findAllByCategoria(categoria)
                .stream()
                .map(ProcesoMapper::toResponse)
                .toList();
    }
 
    // ─── Operaciones con historial automático ───────────────────────────────────
 
    /**
     * Actualiza el proceso y guarda automáticamente el log del cambio.
     *
     * @param id          ID del proceso a actualizar
     * @param dto         Nuevos valores
     * @param idEmpleado  ID del empleado responsable — puede ser null si no se conoce
     */
    @Transactional
    public ProcesoResponseDTO actualizarProceso(Long id, ProcesoRequestDTO dto, Long idEmpleado) {
        Proceso proceso = buscarActivo(id);
 
        // Capturar estado ANTES del cambio
        String valorAnterior = serializar(ProcesoMapper.toResponse(proceso));
 
        // Aplicar cambios
        proceso.setNombre(dto.getNombre());
        proceso.setDescripcion(dto.getDescripcion());
        proceso.setCategoria(dto.getCategoria());
        proceso.setBorrador(dto.getBorrador());
        proceso.setActivo(dto.getActivo());
 
        Proceso guardado = procesoRepository.save(proceso);
 
        // Capturar estado DESPUÉS del cambio
        String valorNuevo = serializar(ProcesoMapper.toResponse(guardado));
 
        registrarHistorial(guardado, idEmpleado, valorAnterior, valorNuevo, "EDICION");
 
        return ProcesoMapper.toResponse(guardado);
    }
 
    /**
     * Soft delete del proceso — valor_nuevo queda null en el historial.
     *
     * @param id         ID del proceso
     * @param idEmpleado ID del empleado responsable — puede ser null
     */
    @Transactional
    public void eliminarProceso(Long id, Long idEmpleado) {
        Proceso proceso = buscarActivo(id);
 
        String valorAnterior = serializar(ProcesoMapper.toResponse(proceso));
 
        proceso.setActivo(false);
        Proceso guardado = procesoRepository.save(proceso);
 
        // valorNuevo = null porque el proceso ya no existe activo
        registrarHistorial(guardado, idEmpleado, valorAnterior, null, "ELIMINACION");
    }
 
    // ─── Historial (lectura) ─────────────────────────────────────────────────────
 
    public List<HistorialProceso> obtenerHistorialDeProceso(Long idProceso) {
        return historialProcesoRepository.findAllByProceso_IdOrderByFechaCambioDesc(idProceso);
    }
 
    // ─── Helpers privados ────────────────────────────────────────────────────────
 
    private Proceso buscarActivo(Long id) {
        return procesoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RuntimeException("Proceso no encontrado"));
    }
 
    private void registrarHistorial(Proceso proceso, Long idEmpleado,
                                    String valorAnterior, String valorNuevo,
                                    String tipoAccion) {
        Empleado empleado = null;
        if (idEmpleado != null) {
            // Si el empleado no existe simplemente no se asocia — no rompe el flujo
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
            throw new RuntimeException("Error al serializar objeto a JSON", e);
        }
    }
}