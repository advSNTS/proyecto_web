package com.proyecto.web.service;

import com.proyecto.web.dto.NodoRequestDTO;
import com.proyecto.web.dto.NodoResponseDTO;
import com.proyecto.web.entity.Nodo;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.enums.TipoNodo;
import com.proyecto.web.mapper.NodoMapper;
import com.proyecto.web.repository.NodoRepository;
import com.proyecto.web.repository.ProcesoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NodoService {

    private final NodoRepository nodoRepository;
    private final ProcesoRepository procesoRepository;

    public NodoResponseDTO crearNodo(NodoRequestDTO dto) {
        Proceso proceso = procesoRepository.findByIdAndActivoTrue(dto.getIdProceso())
                .orElseThrow(() -> new RuntimeException("Proceso no encontrado"));

        Nodo nodo = NodoMapper.toEntity(dto, proceso);
        return NodoMapper.toResponse(nodoRepository.save(nodo));
    }

    public List<NodoResponseDTO> obtenerPorProceso(Long idProceso) {
        return nodoRepository.findAllByProceso_Id(idProceso)
                .stream()
                .map(NodoMapper::toResponse)
                .toList();
    }

    public List<NodoResponseDTO> obtenerPorProcesoYTipo(Long idProceso, TipoNodo tipo) {
        return nodoRepository.findAllByProceso_IdAndTipo(idProceso, tipo)
                .stream()
                .map(NodoMapper::toResponse)
                .toList();
    }

    public NodoResponseDTO obtenerNodo(Long id) {
        return NodoMapper.toResponse(buscar(id));
    }

    public NodoResponseDTO actualizarNodo(Long id, NodoRequestDTO dto) {
        Nodo nodo = buscar(id);

        Proceso proceso = procesoRepository.findByIdAndActivoTrue(dto.getIdProceso())
                .orElseThrow(() -> new RuntimeException("Proceso no encontrado"));

        nodo.setProceso(proceso);
        nodo.setTipo(dto.getTipo());
        nodo.setNombre(dto.getNombre());

        return NodoMapper.toResponse(nodoRepository.save(nodo));
    }

    public void eliminarNodo(Long id) {
        Nodo nodo = buscar(id);
        nodoRepository.delete(nodo);
    }

    private Nodo buscar(Long id) {
        return nodoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nodo no encontrado"));
    }
}

//comentario