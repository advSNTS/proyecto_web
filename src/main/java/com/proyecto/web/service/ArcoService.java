package com.proyecto.web.service;

import com.proyecto.web.dto.ArcoRequestDTO;
import com.proyecto.web.dto.ArcoResponseDTO;
import com.proyecto.web.entity.Arco;
import com.proyecto.web.entity.Nodo;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.mapper.ArcoMapper;
import com.proyecto.web.repository.ArcoRepository;
import com.proyecto.web.repository.NodoRepository;
import com.proyecto.web.util.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArcoService {

    private final ArcoRepository arcoRepository;
    private final ProcesoService procesoService;
    private final NodoRepository nodoRepository;

    public ArcoService(
            ArcoRepository arcoRepository,
            ProcesoService procesoService,
            NodoRepository nodoRepository) {
        this.arcoRepository = arcoRepository;
        this.procesoService = procesoService;
        this.nodoRepository = nodoRepository;
    }

    @Transactional
    public ArcoResponseDTO crearArco(ArcoRequestDTO dto) {
        Proceso proceso = procesoService.buscarVigente(dto.getIdProceso());
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();

        Nodo origen = buscarNodoDeProceso(dto.getNodoOrigenId(), dto.getIdProceso(), nitEmpresa);
        Nodo destino = buscarNodoDeProceso(dto.getNodoDestinoId(), dto.getIdProceso(), nitEmpresa);

        if (origen.getId().equals(destino.getId())) {
            throw new BusinessException("El nodo origen y destino no pueden ser el mismo");
        }

        if (arcoRepository.existsByProceso_IdAndNodoOrigen_IdAndNodoDestino_IdAndEliminadoFalse(
                dto.getIdProceso(), dto.getNodoOrigenId(), dto.getNodoDestinoId())) {
            throw new BusinessException("Ya existe un arco entre esos nodos en este proceso");
        }

        Arco arco = ArcoMapper.toEntity(proceso, origen, destino);
        return ArcoMapper.toResponse(arcoRepository.save(arco));
    }


    @Transactional(readOnly = true)
    public ArcoResponseDTO obtenerArco(Long id) {
        return ArcoMapper.toResponse(buscarPropio(id));
    }


    @Transactional(readOnly = true)
    public List<ArcoResponseDTO> obtenerPorProceso(Long idProceso) {
        procesoService.buscarVigente(idProceso);
        return arcoRepository.findAllByProceso_IdAndEliminadoFalse(idProceso).stream()
                .map(ArcoMapper::toResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<ArcoResponseDTO> obtenerSalientesDe(Long nodoOrigenId) {
        Nodo nodoOrigen = buscarNodoPropio(nodoOrigenId);
        return arcoRepository.findAllByNodoOrigen_Id(nodoOrigen.getId()).stream()
                .filter(a -> !a.isEliminado())
                .map(ArcoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ArcoResponseDTO> obtenerEntrantesA(Long nodoDestinoId) {
        Nodo nodoDestino = buscarNodoPropio(nodoDestinoId);
        return arcoRepository.findAllByNodoDestino_Id(nodoDestino.getId()).stream()
                .filter(a -> !a.isEliminado())
                .map(ArcoMapper::toResponse)
                .toList();
    }

    @Transactional
    public ArcoResponseDTO actualizarArco(Long id, ArcoRequestDTO dto) {
        Arco arco = buscarPropio(id);
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();

        Proceso proceso = procesoService.buscarVigente(dto.getIdProceso());

        Nodo origen = buscarNodoDeProceso(dto.getNodoOrigenId(), dto.getIdProceso(), nitEmpresa);
        Nodo destino = buscarNodoDeProceso(dto.getNodoDestinoId(), dto.getIdProceso(), nitEmpresa);

        if (origen.getId().equals(destino.getId())) {
            throw new BusinessException("El nodo origen y destino no pueden ser el mismo");
        }

        boolean duplicadoOtro = arcoRepository.existsByProceso_IdAndNodoOrigen_IdAndNodoDestino_IdAndEliminadoFalse(
                dto.getIdProceso(), dto.getNodoOrigenId(), dto.getNodoDestinoId());

        if (duplicadoOtro && !mismoArco(arco, dto.getIdProceso(), dto.getNodoOrigenId(), dto.getNodoDestinoId())) {
            throw new BusinessException("Ya existe un arco entre esos nodos en este proceso");
        }

        arco.setProceso(proceso);
        arco.setNodoOrigen(origen);
        arco.setNodoDestino(destino);

        return ArcoMapper.toResponse(arcoRepository.save(arco));
    }


    @Transactional
    public void eliminarArco(Long id) {
        Arco arco = buscarPropio(id);
        arco.setEliminado(true);
        arcoRepository.save(arco);
    }


    private boolean mismoArco(Arco arco, Long procesoId, Long origenId, Long destinoId) {
        return arco.getProceso().getId().equals(procesoId)
                && arco.getNodoOrigen().getId().equals(origenId)
                && arco.getNodoDestino().getId().equals(destinoId);
    }

    private Arco buscar(Long id) {
        return arcoRepository.findById(id)
                .filter(a -> !a.isEliminado())
                .orElseThrow(() -> new RuntimeException("Arco no encontrado"));
    }

    private Arco buscarPropio(Long id) {
        Arco arco = buscar(id);
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();
        if (arco.getProceso() == null
                || arco.getProceso().getEmpresa() == null
                || !nitEmpresa.equals(arco.getProceso().getEmpresa().getNit())) {
            throw new AccessDeniedException("El recurso no pertenece a la empresa autenticada.");
        }
        return arco;
    }

    private Nodo buscarNodoPropio(Long nodoId) {
        Nodo nodo = nodoRepository.findByIdAndEliminadoFalse(nodoId)
                .orElseThrow(() -> new RuntimeException("Nodo no encontrado: " + nodoId));
        String nitEmpresa = SecurityUtils.requireAuthenticatedNitEmpresa();
        if (nodo.getProceso() == null
                || nodo.getProceso().getEmpresa() == null
                || !nitEmpresa.equals(nodo.getProceso().getEmpresa().getNit())) {
            throw new AccessDeniedException("El recurso no pertenece a la empresa autenticada.");
        }
        return nodo;
    }

    private Nodo buscarNodoDeProceso(Long nodoId, Long idProceso, String nitEmpresa) {
        Nodo nodo = buscarNodoPropio(nodoId);
        if (!nodo.getProceso().getId().equals(idProceso)) {
            throw new BusinessException(
                    "El nodo " + nodoId + " no pertenece al proceso " + idProceso);
        }
        if (!nitEmpresa.equals(nodo.getProceso().getEmpresa().getNit())) {
            throw new AccessDeniedException("El recurso no pertenece a la empresa autenticada.");
        }
        return nodo;
    }
}
