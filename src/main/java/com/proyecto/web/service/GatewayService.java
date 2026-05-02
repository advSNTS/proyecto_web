package com.proyecto.web.service;

import com.proyecto.web.dto.GatewayRequestDTO;
import com.proyecto.web.dto.GatewayResponseDTO;
import com.proyecto.web.entity.Arco;
import com.proyecto.web.entity.Gateway;
import com.proyecto.web.entity.Nodo;
import com.proyecto.web.enums.TipoGateway;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.mapper.GatewayMapper;
import com.proyecto.web.repository.ArcoRepository;
import com.proyecto.web.repository.GatewayRepository;
import com.proyecto.web.repository.NodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GatewayService {

    private final GatewayRepository gatewayRepository;
    private final NodoRepository nodoRepository;
    private final ArcoRepository arcoRepository;

    public GatewayResponseDTO crearGateway(GatewayRequestDTO dto) {
        Nodo nodo = buscarNodo(dto.getNodoId());

        if (gatewayRepository.existsByNodo_IdAndDeletedFalse(dto.getNodoId())) {
            throw new BusinessException("El nodo ya tiene un gateway asignado");
        }

        Gateway gateway = GatewayMapper.toEntity(dto, nodo);
        return GatewayMapper.toResponse(gatewayRepository.save(gateway));
    }

    public GatewayResponseDTO obtenerGateway(Long id) {
        return GatewayMapper.toResponse(buscarActivo(id));
    }

    public List<GatewayResponseDTO> obtenerPorProceso(Long procesoId) {
        return gatewayRepository.findAllByNodo_Proceso_IdAndDeletedFalse(procesoId)
                .stream()
                .map(GatewayMapper::toResponse)
                .toList();
    }

    public List<GatewayResponseDTO> obtenerPorProcesoYTipo(Long procesoId, TipoGateway tipo) {
        return gatewayRepository.findAllByNodo_Proceso_IdAndTipoGatewayAndDeletedFalse(procesoId, tipo)
                .stream()
                .map(GatewayMapper::toResponse)
                .toList();
    }

    public GatewayResponseDTO actualizarGateway(Long id, GatewayRequestDTO dto) {
        Gateway gateway = buscarActivo(id);

        if (!gateway.getNodo().getId().equals(dto.getNodoId())) {
            Nodo nodo = buscarNodo(dto.getNodoId());
            gateway.setNodo(nodo);
        }

        gateway.setTipoGateway(dto.getTipoGateway());
        return GatewayMapper.toResponse(gatewayRepository.save(gateway));
    }

    @Transactional
    public void eliminarGateway(Long id) {
        Gateway gateway = buscarActivo(id);
        Long nodoId = gateway.getNodo().getId();
        List<Arco> afectados = arcoRepository.findAllByNodoOrigen_IdOrNodoDestino_IdAndEliminadoFalse(nodoId, nodoId);
        afectados.forEach(a -> a.setEliminado(true));
        arcoRepository.saveAll(afectados);
        gateway.getNodo().setEliminado(true);
        nodoRepository.save(gateway.getNodo());
        gateway.setDeleted(true);
        gatewayRepository.save(gateway);
    }

    private Gateway buscarActivo(Long id) {
        return gatewayRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Gateway no encontrado"));
    }

    private Nodo buscarNodo(Long nodoId) {
        return nodoRepository.findByIdAndEliminadoFalse(nodoId)
                .orElseThrow(() -> new RuntimeException("Nodo no encontrado"));
    }
}
