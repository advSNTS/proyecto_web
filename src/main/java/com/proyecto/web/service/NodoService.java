package com.proyecto.web.service;

import com.proyecto.web.dto.NodoRequestDTO;
import com.proyecto.web.dto.NodoResponseDTO;
import com.proyecto.web.entity.Arco;
import com.proyecto.web.entity.Nodo;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.enums.TipoNodo;
import com.proyecto.web.mapper.NodoMapper;
import com.proyecto.web.repository.ArcoRepository;
import com.proyecto.web.repository.NodoRepository;
import com.proyecto.web.util.SecurityUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NodoService {

    private final NodoRepository nodoRepository;
    private final ProcesoService procesoService;
    private final ArcoRepository arcoRepository;
    private final NodoService self;

    public NodoService(
            NodoRepository nodoRepository,
            ProcesoService procesoService,
            ArcoRepository arcoRepository,
            @Lazy NodoService self) {
        this.nodoRepository = nodoRepository;
        this.procesoService = procesoService;
        this.arcoRepository = arcoRepository;
        this.self = self;
    }

    @Transactional
    public NodoResponseDTO crearNodo(NodoRequestDTO dto) {
        Proceso proceso = procesoService.buscarVigente(dto.getIdProceso());
        Nodo nodo = NodoMapper.toEntity(dto, proceso);
        return NodoMapper.toResponse(nodoRepository.save(nodo));
    }

    @Transactional(readOnly = true)
    public List<NodoResponseDTO> obtenerPorProceso(Long idProceso) {
        procesoService.buscarVigente(idProceso);
        return nodoRepository.findAllByProceso_IdAndEliminadoFalse(idProceso).stream()
                .map(NodoMapper::toResponse)
                .toList();
    }

    /**
     * @deprecated Usa {@link #obtenerPorProceso(Long)}; el NIT se resuelve desde el contexto de seguridad.
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public List<NodoResponseDTO> obtenerPorProceso(Long idProceso, String nitEmpresa) {
        return self.obtenerPorProceso(idProceso);
    }

    @Transactional(readOnly = true)
    public List<NodoResponseDTO> obtenerPorProcesoYTipo(Long idProceso, TipoNodo tipo) {
        procesoService.buscarVigente(idProceso);
        return nodoRepository.findAllByProceso_IdAndTipoAndEliminadoFalse(idProceso, tipo).stream()
                .map(NodoMapper::toResponse)
                .toList();
    }

    /**
     * @deprecated Usa {@link #obtenerPorProcesoYTipo(Long, TipoNodo)}; el NIT se resuelve desde el contexto de seguridad.
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public List<NodoResponseDTO> obtenerPorProcesoYTipo(Long idProceso, TipoNodo tipo, String nitEmpresa) {
        return self.obtenerPorProcesoYTipo(idProceso, tipo);
    }

    @Transactional(readOnly = true)
    public NodoResponseDTO obtenerNodo(Long id) {
        return NodoMapper.toResponse(buscarPropio(id));
    }

    @Transactional
    public NodoResponseDTO actualizarNodo(Long id, NodoRequestDTO dto) {
        Nodo nodo = buscarPropio(id);
        Proceso proceso = procesoService.buscarVigente(dto.getIdProceso());
        nodo.setProceso(proceso);
        nodo.setTipo(dto.getTipo());
        nodo.setNombre(dto.getNombre());
        nodo.setCoordenadaX(dto.getCoordenadaX());
        nodo.setCoordenadaY(dto.getCoordenadaY());
        return NodoMapper.toResponse(nodoRepository.save(nodo));
    }

    @Transactional
    public void eliminarNodo(Long id) {
        Nodo nodo = buscarPropio(id);
        procesoService.buscarVigente(nodo.getProceso().getId());
        marcarArcosEliminadosPorNodo(nodo.getId());
        nodo.setEliminado(true);
        nodoRepository.save(nodo);
    }

    /**
     * @deprecated Usa {@link #eliminarNodo(Long)}; el NIT se resuelve desde el contexto de seguridad.
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public void eliminarNodo(Long id, String nitEmpresa) {
        self.eliminarNodo(id);
    }

    private Nodo buscar(Long id) {
        return nodoRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RuntimeException("Nodo no encontrado"));
    }

    private Nodo buscarPropio(Long id) {
        Nodo nodo = buscar(id);
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();
        if (nodo.getProceso() == null
                || nodo.getProceso().getEmpresa() == null
                || !nitEmpresa.equals(nodo.getProceso().getEmpresa().getNit())) {
            throw new AccessDeniedException("El recurso no pertenece a la empresa autenticada.");
        }
        return nodo;
    }

    private void marcarArcosEliminadosPorNodo(Long nodoId) {
        List<Arco> afectados = arcoRepository.findAllByNodoOrigen_IdOrNodoDestino_IdAndEliminadoFalse(nodoId, nodoId);
        afectados.forEach(a -> a.setEliminado(true));
        arcoRepository.saveAll(afectados);
    }
}
