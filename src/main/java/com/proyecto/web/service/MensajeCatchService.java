package com.proyecto.web.service;

import com.proyecto.web.dto.MensajeCatchRequestDTO;
import com.proyecto.web.dto.MensajeCatchResponseDTO;
import com.proyecto.web.entity.MensajeCatch;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.MensajeCatchRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MensajeCatchService {

    private final MensajeCatchRepository mensajeCatchRepository;
    private final ProcesoService procesoService;
    private final MensajeCatchService self;

    public MensajeCatchService(
            MensajeCatchRepository mensajeCatchRepository,
            ProcesoService procesoService,
            @Lazy MensajeCatchService self) {
        this.mensajeCatchRepository = mensajeCatchRepository;
        this.procesoService = procesoService;
        this.self = self;
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

    /**
     * @deprecated Usa {@link #crear(MensajeCatchRequestDTO)}; el NIT se resuelve desde el contexto de seguridad.
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public MensajeCatchResponseDTO crear(String nitEmpresa, MensajeCatchRequestDTO dto) {
        return self.crear(dto);
    }

    @Transactional(readOnly = true)
    public List<MensajeCatchResponseDTO> listarPorProceso(Long procesoId) {
        procesoService.buscarVigente(procesoId);
        return mensajeCatchRepository.findAllByProceso_IdAndEliminadoFalse(procesoId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * @deprecated Usa {@link #listarPorProceso(Long)}; el NIT se resuelve desde el contexto de seguridad.
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public List<MensajeCatchResponseDTO> listarPorProceso(String nitEmpresa, Long procesoId) {
        return self.listarPorProceso(procesoId);
    }

    @Transactional(readOnly = true)
    public MensajeCatchResponseDTO obtener(Long id) {
        MensajeCatch m = buscarActivo(id);
        procesoService.buscarVigente(m.getProceso().getId());
        return toDto(m);
    }

    /**
     * @deprecated Usa {@link #obtener(Long)}; el NIT se resuelve desde el contexto de seguridad.
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public MensajeCatchResponseDTO obtener(String nitEmpresa, Long id) {
        return self.obtener(id);
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

    /**
     * @deprecated Usa {@link #actualizar(Long, MensajeCatchRequestDTO)}; el NIT se resuelve desde el contexto de seguridad.
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public MensajeCatchResponseDTO actualizar(String nitEmpresa, Long id, MensajeCatchRequestDTO dto) {
        return self.actualizar(id, dto);
    }

    @Transactional
    public void eliminar(Long id) {
        MensajeCatch m = buscarActivo(id);
        procesoService.buscarVigente(m.getProceso().getId());
        m.setEliminado(true);
        mensajeCatchRepository.save(m);
    }

    /**
     * @deprecated Usa {@link #eliminar(Long)}; el NIT se resuelve desde el contexto de seguridad.
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public void eliminar(String nitEmpresa, Long id) {
        self.eliminar(id);
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
