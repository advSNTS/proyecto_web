package com.proyecto.web.service;

import com.proyecto.web.dto.MensajeThrowRequestDTO;
import com.proyecto.web.dto.MensajeThrowResponseDTO;
import com.proyecto.web.entity.MensajeThrow;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.MensajeThrowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MensajeThrowService {

    private final MensajeThrowRepository mensajeThrowRepository;
    private final ProcesoService procesoService;

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

    @Deprecated
    public MensajeThrowResponseDTO crear(String nitEmpresa, MensajeThrowRequestDTO dto) {
        return crear(dto);
    }

    public List<MensajeThrowResponseDTO> listarPorProceso(Long procesoId) {
        procesoService.buscarVigente(procesoId);
        return mensajeThrowRepository.findAllByProceso_IdAndEliminadoFalse(procesoId).stream()
                .map(this::toDto)
                .toList();
    }

    @Deprecated
    public List<MensajeThrowResponseDTO> listarPorProceso(String nitEmpresa, Long procesoId) {
        return listarPorProceso(procesoId);
    }

    public MensajeThrowResponseDTO obtener(Long id) {
        MensajeThrow m = buscarActivo(id);
        procesoService.buscarVigente(m.getProceso().getId());
        return toDto(m);
    }

    @Deprecated
    public MensajeThrowResponseDTO obtener(String nitEmpresa, Long id) {
        return obtener(id);
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

    @Deprecated
    public MensajeThrowResponseDTO actualizar(String nitEmpresa, Long id, MensajeThrowRequestDTO dto) {
        return actualizar(id, dto);
    }

    @Transactional
    public void eliminar(Long id) {
        MensajeThrow m = buscarActivo(id);
        procesoService.buscarVigente(m.getProceso().getId());
        m.setEliminado(true);
        mensajeThrowRepository.save(m);
    }

    @Deprecated
    public void eliminar(String nitEmpresa, Long id) {
        eliminar(id);
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
