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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArcoService {

    private final ArcoRepository arcoRepository;
    private final ProcesoService procesoService;
    private final NodoRepository nodoRepository;

    public ArcoResponseDTO crearArco(ArcoRequestDTO dto) {
        validarNit(dto.getNitEmpresa());
        Proceso proceso = procesoService.buscarVigente(dto.getIdProceso(), dto.getNitEmpresa());

        Nodo origen = buscarNodoDeProceso(dto.getNodoOrigenId(), dto.getIdProceso());
        Nodo destino = buscarNodoDeProceso(dto.getNodoDestinoId(), dto.getIdProceso());

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

    public ArcoResponseDTO obtenerArco(Long id) {
        return ArcoMapper.toResponse(buscar(id));
    }

    public List<ArcoResponseDTO> obtenerPorProceso(Long idProceso, String nitEmpresa) {
        procesoService.buscarVigente(idProceso, nitEmpresa);
        return arcoRepository.findAllByProceso_IdAndEliminadoFalse(idProceso).stream()
                .map(ArcoMapper::toResponse)
                .toList();
    }

    public List<ArcoResponseDTO> obtenerSalientesDe(Long nodoOrigenId) {
        return arcoRepository.findAllByNodoOrigen_Id(nodoOrigenId).stream()
                .filter(a -> !a.isEliminado())
                .map(ArcoMapper::toResponse)
                .toList();
    }

    public List<ArcoResponseDTO> obtenerEntrantesA(Long nodoDestinoId) {
        return arcoRepository.findAllByNodoDestino_Id(nodoDestinoId).stream()
                .filter(a -> !a.isEliminado())
                .map(ArcoMapper::toResponse)
                .toList();
    }

    public ArcoResponseDTO actualizarArco(Long id, ArcoRequestDTO dto) {
        validarNit(dto.getNitEmpresa());
        Arco arco = buscar(id);

        Proceso proceso = procesoService.buscarVigente(dto.getIdProceso(), dto.getNitEmpresa());

        Nodo origen = buscarNodoDeProceso(dto.getNodoOrigenId(), dto.getIdProceso());
        Nodo destino = buscarNodoDeProceso(dto.getNodoDestinoId(), dto.getIdProceso());

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
        Arco arco = buscar(id);
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

    private void validarNit(String nit) {
        if (nit == null || nit.isBlank()) {
            throw new BusinessException("nitEmpresa es obligatorio", HttpStatus.BAD_REQUEST);
        }
    }

    private Nodo buscarNodoDeProceso(Long nodoId, Long idProceso) {
        Nodo nodo = nodoRepository.findByIdAndEliminadoFalse(nodoId)
                .orElseThrow(() -> new RuntimeException("Nodo no encontrado: " + nodoId));

        if (!nodo.getProceso().getId().equals(idProceso)) {
            throw new BusinessException(
                    "El nodo " + nodoId + " no pertenece al proceso " + idProceso);
        }
        return nodo;
    }
}
