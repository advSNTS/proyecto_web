package com.proyecto.web.service;

import com.proyecto.web.dto.TareaIntegracionRequestDTO;
import com.proyecto.web.dto.TareaIntegracionResponseDTO;
import com.proyecto.web.entity.MensajeExterno;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.entity.TareaIntegracion;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.TareaIntegracionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TareaIntegracionService {

    private final TareaIntegracionRepository tareaIntegracionRepository;
    private final ProcesoService procesoService;
    private final MensajeExternoService mensajeExternoService;

    public TareaIntegracionService(
            TareaIntegracionRepository tareaIntegracionRepository,
            ProcesoService procesoService,
            MensajeExternoService mensajeExternoService) {
        this.tareaIntegracionRepository = tareaIntegracionRepository;
        this.procesoService = procesoService;
        this.mensajeExternoService = mensajeExternoService;
    }

    @Transactional
    public TareaIntegracionResponseDTO crear(TareaIntegracionRequestDTO dto) {
        Proceso p = procesoService.buscarVigente(dto.getProcesoId());
        MensajeExterno ext = mensajeExternoService.buscarActivoEntidad(dto.getMensajeExternoId());
        TareaIntegracion t = TareaIntegracion.builder()
                .proceso(p)
                .mensajeExterno(ext)
                .payloadMapping(dto.getPayloadMapping())
                .eliminado(false)
                .build();
        return toDto(tareaIntegracionRepository.save(t));
    }


    @Transactional(readOnly = true)
    public List<TareaIntegracionResponseDTO> listarPorProceso(Long procesoId) {
        procesoService.buscarVigente(procesoId);
        return tareaIntegracionRepository.findAllByProceso_IdAndEliminadoFalse(procesoId).stream()
                .map(this::toDto)
                .toList();
    }


    @Transactional(readOnly = true)
    public TareaIntegracionResponseDTO obtener(Long id) {
        TareaIntegracion t = buscarActivo(id);
        procesoService.buscarVigente(t.getProceso().getId());
        return toDto(t);
    }


    @Transactional
    public TareaIntegracionResponseDTO actualizar(Long id, TareaIntegracionRequestDTO dto) {
        TareaIntegracion t = buscarActivo(id);
        procesoService.buscarVigente(t.getProceso().getId());
        Proceso p = procesoService.buscarVigente(dto.getProcesoId());
        MensajeExterno ext = mensajeExternoService.buscarActivoEntidad(dto.getMensajeExternoId());
        t.setProceso(p);
        t.setMensajeExterno(ext);
        t.setPayloadMapping(dto.getPayloadMapping());
        return toDto(tareaIntegracionRepository.save(t));
    }


    @Transactional
    public void eliminar(Long id) {
        TareaIntegracion t = buscarActivo(id);
        procesoService.buscarVigente(t.getProceso().getId());
        t.setEliminado(true);
        tareaIntegracionRepository.save(t);
    }


    private TareaIntegracion buscarActivo(Long id) {
        return tareaIntegracionRepository.findById(id)
                .filter(x -> !x.isEliminado())
                .orElseThrow(() -> new BusinessException("Tarea de integración no encontrada", HttpStatus.NOT_FOUND));
    }

    private TareaIntegracionResponseDTO toDto(TareaIntegracion t) {
        return TareaIntegracionResponseDTO.builder()
                .id(t.getId())
                .procesoId(t.getProceso().getId())
                .mensajeExternoId(t.getMensajeExterno().getId())
                .payloadMapping(t.getPayloadMapping())
                .build();
    }
}
