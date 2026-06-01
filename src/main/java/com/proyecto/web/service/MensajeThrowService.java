package com.proyecto.web.service;

import com.proyecto.web.dto.MensajeThrowRequestDTO;
import com.proyecto.web.dto.MensajeThrowResponseDTO;
import com.proyecto.web.entity.MensajeThrow;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.MensajeThrowRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MensajeThrowService {

    private final MensajeThrowRepository mensajeThrowRepository;
    private final ProcesoService procesoService;
    private final MensajeThrowService self;

    public MensajeThrowService(
            MensajeThrowRepository mensajeThrowRepository,
            ProcesoService procesoService,
            @Lazy MensajeThrowService self) {
        this.mensajeThrowRepository = mensajeThrowRepository;
        this.procesoService = procesoService;
        this.self = self;
    }

    @Transactional
    public MensajeThrowResponseDTO crear(MensajeThrowRequestDTO dto) {
        Proceso p = procesoService.buscarVigente(dto.getProcesoId());
        MensajeThrow m = MensajeThrow.builder()
                .proceso(p)
                .nombreMensaje(dto.getNombreMensaje())
                .payloadTemplate(dto.getPayloadTemplate())
                .eliminado(false)
                .build();
        return toDto(mensajeThrowRepository.save(m));
    }

    /**
     * @deprecated Usa {@link #crear(MensajeThrowRequestDTO)}; el NIT se resuelve desde el contexto de seguridad.
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public MensajeThrowResponseDTO crear(String nitEmpresa, MensajeThrowRequestDTO dto) {
        return self.crear(dto);
    }

    @Transactional(readOnly = true)
    public List<MensajeThrowResponseDTO> listarPorProceso(Long procesoId) {
        procesoService.buscarVigente(procesoId);
        return mensajeThrowRepository.findAllByProceso_IdAndEliminadoFalse(procesoId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * @deprecated Usa {@link #listarPorProceso(Long)}; el NIT se resuelve desde el contexto de seguridad.
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public List<MensajeThrowResponseDTO> listarPorProceso(String nitEmpresa, Long procesoId) {
        return self.listarPorProceso(procesoId);
    }

    @Transactional(readOnly = true)
    public MensajeThrowResponseDTO obtener(Long id) {
        MensajeThrow m = buscarActivo(id);
        procesoService.buscarVigente(m.getProceso().getId());
        return toDto(m);
    }

    /**
     * @deprecated Usa {@link #obtener(Long)}; el NIT se resuelve desde el contexto de seguridad.
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public MensajeThrowResponseDTO obtener(String nitEmpresa, Long id) {
        return self.obtener(id);
    }

    @Transactional
    public MensajeThrowResponseDTO actualizar(Long id, MensajeThrowRequestDTO dto) {
        MensajeThrow m = buscarActivo(id);
        procesoService.buscarVigente(m.getProceso().getId());
        Proceso p = procesoService.buscarVigente(dto.getProcesoId());
        m.setProceso(p);
        m.setNombreMensaje(dto.getNombreMensaje());
        m.setPayloadTemplate(dto.getPayloadTemplate());
        return toDto(mensajeThrowRepository.save(m));
    }

    /**
     * @deprecated Usa {@link #actualizar(Long, MensajeThrowRequestDTO)}; el NIT se resuelve desde el contexto de seguridad.
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public MensajeThrowResponseDTO actualizar(String nitEmpresa, Long id, MensajeThrowRequestDTO dto) {
        return self.actualizar(id, dto);
    }

    @Transactional
    public void eliminar(Long id) {
        MensajeThrow m = buscarActivo(id);
        procesoService.buscarVigente(m.getProceso().getId());
        m.setEliminado(true);
        mensajeThrowRepository.save(m);
    }

    /**
     * @deprecated Usa {@link #eliminar(Long)}; el NIT se resuelve desde el contexto de seguridad.
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public void eliminar(String nitEmpresa, Long id) {
        self.eliminar(id);
    }

    private MensajeThrow buscarActivo(Long id) {
        return mensajeThrowRepository.findById(id)
                .filter(x -> !x.isEliminado())
                .orElseThrow(() -> new BusinessException("Mensaje throw no encontrado", HttpStatus.NOT_FOUND));
    }

    private MensajeThrowResponseDTO toDto(MensajeThrow m) {
        return MensajeThrowResponseDTO.builder()
                .id(m.getId())
                .procesoId(m.getProceso().getId())
                .nombreMensaje(m.getNombreMensaje())
                .payloadTemplate(m.getPayloadTemplate())
                .build();
    }
}
