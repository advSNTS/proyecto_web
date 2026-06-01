package com.proyecto.web.service;

import com.proyecto.web.dto.MensajeCatchRequestDTO;
import com.proyecto.web.dto.MensajeCatchResponseDTO;
import com.proyecto.web.entity.MensajeCatch;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.MensajeCatchRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MensajeCatchService {

    private final MensajeCatchRepository mensajeCatchRepository;
    private final ProcesoService procesoService;

    public MensajeCatchService(
            MensajeCatchRepository mensajeCatchRepository,
            ProcesoService procesoService) {
        this.mensajeCatchRepository = mensajeCatchRepository;
        this.procesoService = procesoService;
    }

    @Transactional
    public MensajeCatchResponseDTO crear(MensajeCatchRequestDTO dto) {
        Proceso p = procesoService.buscarVigente(dto.getProcesoId());
        boolean iniciar = Boolean.TRUE.equals(dto.getIniciarNuevaInstancia());
        MensajeCatch m = MensajeCatch.builder()
                .proceso(p)
                .nombreMensaje(dto.getNombreMensaje())
                .correlacionExpr(dto.getCorrelacionExpr())
                .iniciarNuevaInstancia(iniciar)
                .eliminado(false)
                .build();
        return toDto(mensajeCatchRepository.save(m));
    }


    @Transactional(readOnly = true)
    public List<MensajeCatchResponseDTO> listarPorProceso(Long procesoId) {
        procesoService.buscarVigente(procesoId);
        return mensajeCatchRepository.findAllByProceso_IdAndEliminadoFalse(procesoId).stream()
                .map(this::toDto)
                .toList();
    }


    @Transactional(readOnly = true)
    public MensajeCatchResponseDTO obtener(Long id) {
        MensajeCatch m = buscarActivo(id);
        procesoService.buscarVigente(m.getProceso().getId());
        return toDto(m);
    }


    @Transactional
    public MensajeCatchResponseDTO actualizar(Long id, MensajeCatchRequestDTO dto) {
        MensajeCatch m = buscarActivo(id);
        procesoService.buscarVigente(m.getProceso().getId());
        Proceso p = procesoService.buscarVigente(dto.getProcesoId());
        m.setProceso(p);
        m.setNombreMensaje(dto.getNombreMensaje());
        m.setCorrelacionExpr(dto.getCorrelacionExpr());
        m.setIniciarNuevaInstancia(Boolean.TRUE.equals(dto.getIniciarNuevaInstancia()));
        return toDto(mensajeCatchRepository.save(m));
    }


    @Transactional
    public void eliminar(Long id) {
        MensajeCatch m = buscarActivo(id);
        procesoService.buscarVigente(m.getProceso().getId());
        m.setEliminado(true);
        mensajeCatchRepository.save(m);
    }


    private MensajeCatch buscarActivo(Long id) {
        return mensajeCatchRepository.findById(id)
                .filter(x -> !x.isEliminado())
                .orElseThrow(() -> new BusinessException("Mensaje catch no encontrado", HttpStatus.NOT_FOUND));
    }

    private MensajeCatchResponseDTO toDto(MensajeCatch m) {
        return MensajeCatchResponseDTO.builder()
                .id(m.getId())
                .procesoId(m.getProceso().getId())
                .nombreMensaje(m.getNombreMensaje())
                .correlacionExpr(m.getCorrelacionExpr())
                .iniciarNuevaInstancia(m.isIniciarNuevaInstancia())
                .build();
    }
}
