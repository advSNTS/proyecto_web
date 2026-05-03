package com.proyecto.web.service;

import com.proyecto.web.dto.MensajeCatchRequestDTO;
import com.proyecto.web.dto.MensajeCatchResponseDTO;
import com.proyecto.web.entity.MensajeCatch;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.MensajeCatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MensajeCatchService {

    private final MensajeCatchRepository mensajeCatchRepository;
    private final ProcesoService procesoService;

    public MensajeCatchResponseDTO crear(String nitEmpresa, MensajeCatchRequestDTO dto) {
        Proceso p = procesoService.buscarVigente(dto.getProcesoId(), nitEmpresa);
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

    public List<MensajeCatchResponseDTO> listarPorProceso(String nitEmpresa, Long procesoId) {
        procesoService.buscarVigente(procesoId, nitEmpresa);
        return mensajeCatchRepository.findAllByProceso_IdAndEliminadoFalse(procesoId).stream()
                .map(this::toDto)
                .toList();
    }

    public MensajeCatchResponseDTO obtener(String nitEmpresa, Long id) {
        MensajeCatch m = buscarActivo(id);
        procesoService.buscarVigente(m.getProceso().getId(), nitEmpresa);
        return toDto(m);
    }

    @Transactional
    public MensajeCatchResponseDTO actualizar(String nitEmpresa, Long id, MensajeCatchRequestDTO dto) {
        MensajeCatch m = buscarActivo(id);
        procesoService.buscarVigente(m.getProceso().getId(), nitEmpresa);
        Proceso p = procesoService.buscarVigente(dto.getProcesoId(), nitEmpresa);
        m.setProceso(p);
        m.setNombreMensaje(dto.getNombreMensaje());
        m.setCorrelacionExpr(dto.getCorrelacionExpr());
        m.setIniciarNuevaInstancia(Boolean.TRUE.equals(dto.getIniciarNuevaInstancia()));
        return toDto(mensajeCatchRepository.save(m));
    }

    @Transactional
    public void eliminar(String nitEmpresa, Long id) {
        MensajeCatch m = buscarActivo(id);
        procesoService.buscarVigente(m.getProceso().getId(), nitEmpresa);
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
