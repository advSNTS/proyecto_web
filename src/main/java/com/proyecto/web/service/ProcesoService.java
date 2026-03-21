package com.proyecto.web.service;

import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.mapper.ProcesoMapper;
import com.proyecto.web.repository.ProcesoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcesoService {

    private final ProcesoRepository procesoRepository;

    public ProcesoResponseDTO crearProceso(ProcesoRequestDTO dto) {
        Proceso proceso = ProcesoMapper.toEntity(dto);
        return ProcesoMapper.toResponse(procesoRepository.save(proceso));
    }

    public List<ProcesoResponseDTO> obtenerProcesos() {
        return procesoRepository.findAllByActivoTrue()
                .stream()
                .map(ProcesoMapper::toResponse)
                .toList();
    }

    public ProcesoResponseDTO obtenerProceso(Long id) {
        Proceso proceso = procesoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RuntimeException("Proceso no encontrado"));
        return ProcesoMapper.toResponse(proceso);
    }

    public List<ProcesoResponseDTO> obtenerPorCategoria(String categoria) {
        return procesoRepository.findAllByCategoria(categoria)
                .stream()
                .map(ProcesoMapper::toResponse)
                .toList();
    }

    public ProcesoResponseDTO actualizarProceso(Long id, ProcesoRequestDTO dto) {
        Proceso proceso = procesoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RuntimeException("Proceso no encontrado"));

        proceso.setNombre(dto.getNombre());
        proceso.setDescripcion(dto.getDescripcion());
        proceso.setCategoria(dto.getCategoria());
        proceso.setBorrador(dto.getBorrador());
        proceso.setActivo(dto.getActivo());

        return ProcesoMapper.toResponse(procesoRepository.save(proceso));
    }

    // Soft delete, marca como inactivo
    public void eliminarProceso(Long id) {
        Proceso proceso = procesoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RuntimeException("Proceso no encontrado"));
        proceso.setActivo(false);
        procesoRepository.save(proceso);
    }
}