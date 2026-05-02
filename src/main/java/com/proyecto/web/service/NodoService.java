package com.proyecto.web.service;

import com.proyecto.web.dto.NodoRequestDTO;
import com.proyecto.web.dto.NodoResponseDTO;
import com.proyecto.web.entity.Arco;
import com.proyecto.web.entity.Nodo;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.enums.TipoNodo;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.mapper.NodoMapper;
import com.proyecto.web.repository.ArcoRepository;
import com.proyecto.web.repository.NodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NodoService {

    private final NodoRepository nodoRepository;
    private final ProcesoService procesoService;
    private final ArcoRepository arcoRepository;

    public NodoResponseDTO crearNodo(NodoRequestDTO dto) {
        validarNit(dto.getNitEmpresa());
        Proceso proceso = procesoService.buscarVigente(dto.getIdProceso(), dto.getNitEmpresa());
        Nodo nodo = NodoMapper.toEntity(dto, proceso);
        return NodoMapper.toResponse(nodoRepository.save(nodo));
    }

    public List<NodoResponseDTO> obtenerPorProceso(Long idProceso, String nitEmpresa) {
        procesoService.buscarVigente(idProceso, nitEmpresa);
        return nodoRepository.findAllByProceso_IdAndEliminadoFalse(idProceso).stream()
                .map(NodoMapper::toResponse)
                .toList();
    }

    public List<NodoResponseDTO> obtenerPorProcesoYTipo(Long idProceso, TipoNodo tipo, String nitEmpresa) {
        procesoService.buscarVigente(idProceso, nitEmpresa);
        return nodoRepository.findAllByProceso_IdAndTipoAndEliminadoFalse(idProceso, tipo).stream()
                .map(NodoMapper::toResponse)
                .toList();
    }

    public NodoResponseDTO obtenerNodo(Long id) {
        return NodoMapper.toResponse(buscar(id));
    }

    public NodoResponseDTO actualizarNodo(Long id, NodoRequestDTO dto) {
        validarNit(dto.getNitEmpresa());
        Nodo nodo = buscar(id);
        Proceso proceso = procesoService.buscarVigente(dto.getIdProceso(), dto.getNitEmpresa());
        nodo.setProceso(proceso);
        nodo.setTipo(dto.getTipo());
        nodo.setNombre(dto.getNombre());
        nodo.setCoordenadaX(dto.getCoordenadaX());
        nodo.setCoordenadaY(dto.getCoordenadaY());
        return NodoMapper.toResponse(nodoRepository.save(nodo));
    }

    @Transactional
    public void eliminarNodo(Long id, String nitEmpresa) {
        validarNit(nitEmpresa);
        Nodo nodo = buscar(id);
        procesoService.buscarVigente(nodo.getProceso().getId(), nitEmpresa);
        marcarArcosEliminadosPorNodo(nodo.getId());
        nodo.setEliminado(true);
        nodoRepository.save(nodo);
    }

    private void validarNit(String nit) {
        if (nit == null || nit.isBlank()) {
            throw new BusinessException("nitEmpresa es obligatorio", HttpStatus.BAD_REQUEST);
        }
    }

    private Nodo buscar(Long id) {
        return nodoRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RuntimeException("Nodo no encontrado"));
    }

    private void marcarArcosEliminadosPorNodo(Long nodoId) {
        List<Arco> afectados = arcoRepository.findAllByNodoOrigen_IdOrNodoDestino_IdAndEliminadoFalse(nodoId, nodoId);
        afectados.forEach(a -> a.setEliminado(true));
        arcoRepository.saveAll(afectados);
    }
}
