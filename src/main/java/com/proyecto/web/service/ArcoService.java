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
import com.proyecto.web.repository.ProcesoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
 
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class ArcoService {
 
    private final ArcoRepository arcoRepository;
    private final ProcesoRepository procesoRepository;
    private final NodoRepository nodoRepository;
 
    public ArcoResponseDTO crearArco(ArcoRequestDTO dto) {
        Proceso proceso = procesoRepository.findByIdAndActivoTrue(dto.getIdProceso())
                .orElseThrow(() -> new RuntimeException("Proceso no encontrado"));
 
        Nodo origen = buscarNodoDeProceso(dto.getNodoOrigenId(), dto.getIdProceso());
        Nodo destino = buscarNodoDeProceso(dto.getNodoDestinoId(), dto.getIdProceso());
 
        // Un nodo no puede conectarse consigo mismo
        if (origen.getId().equals(destino.getId())) {
            throw new BusinessException("El nodo origen y destino no pueden ser el mismo");
        }

        // Evitar arcos duplicados en el mismo proceso
        if (arcoRepository.existsByProceso_IdAndNodoOrigen_IdAndNodoDestino_Id(
                dto.getIdProceso(), dto.getNodoOrigenId(), dto.getNodoDestinoId())) {
            throw new BusinessException("Ya existe un arco entre esos nodos en este proceso");
        }
 
        Arco arco = ArcoMapper.toEntity(proceso, origen, destino);
        return ArcoMapper.toResponse(arcoRepository.save(arco));
    }
 
    public ArcoResponseDTO obtenerArco(Long id) {
        return ArcoMapper.toResponse(buscar(id));
    }
 
    public List<ArcoResponseDTO> obtenerPorProceso(Long idProceso) {
        return arcoRepository.findAllByProceso_Id(idProceso)
                .stream()
                .map(ArcoMapper::toResponse)
                .toList();
    }
 
    public List<ArcoResponseDTO> obtenerSalientesDe(Long nodoOrigenId) {
        return arcoRepository.findAllByNodoOrigen_Id(nodoOrigenId)
                .stream()
                .map(ArcoMapper::toResponse)
                .toList();
    }
 
    public List<ArcoResponseDTO> obtenerEntrantesA(Long nodoDestinoId) {
        return arcoRepository.findAllByNodoDestino_Id(nodoDestinoId)
                .stream()
                .map(ArcoMapper::toResponse)
                .toList();
    }
 
    public ArcoResponseDTO actualizarArco(Long id, ArcoRequestDTO dto) {
        Arco arco = buscar(id);
 
        Proceso proceso = procesoRepository.findByIdAndActivoTrue(dto.getIdProceso())
                .orElseThrow(() -> new RuntimeException("Proceso no encontrado"));
 
        Nodo origen = buscarNodoDeProceso(dto.getNodoOrigenId(), dto.getIdProceso());
        Nodo destino = buscarNodoDeProceso(dto.getNodoDestinoId(), dto.getIdProceso());
 
        if (origen.getId().equals(destino.getId())) {
            throw new BusinessException("El nodo origen y destino no pueden ser el mismo");
        }

        // Validar duplicado excluyendo el arco actual
        boolean duplicado = arcoRepository.existsByProceso_IdAndNodoOrigen_IdAndNodoDestino_Id(
                dto.getIdProceso(), dto.getNodoOrigenId(), dto.getNodoDestinoId());

        if (duplicado && !arco.getProceso().getId().equals(dto.getIdProceso())) {
            throw new BusinessException("Ya existe un arco entre esos nodos en este proceso");
        }
 
        arco.setProceso(proceso);
        arco.setNodoOrigen(origen);
        arco.setNodoDestino(destino);
 
        return ArcoMapper.toResponse(arcoRepository.save(arco));
    }
 
    public void eliminarArco(Long id) {
        Arco arco = buscar(id);
        arcoRepository.delete(arco);
    }
 
    // ─── Helpers privados ─────────────────────────────────────────────────────
 
    private Arco buscar(Long id) {
        return arcoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Arco no encontrado"));
    }
 
    /**
     * Valida que el nodo exista y pertenezca al proceso indicado.
     * Evita conectar nodos de procesos distintos.
     */
    private Nodo buscarNodoDeProceso(Long nodoId, Long idProceso) {
        Nodo nodo = nodoRepository.findById(nodoId)
                .orElseThrow(() -> new RuntimeException("Nodo no encontrado: " + nodoId));
 
        if (!nodo.getProceso().getId().equals(idProceso)) {
            throw new BusinessException(
                    "El nodo " + nodoId + " no pertenece al proceso " + idProceso);
        }
        return nodo;
    }
}