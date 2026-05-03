package com.proyecto.web.service;

import com.proyecto.web.dto.MensajeExternoRequestDTO;
import com.proyecto.web.dto.MensajeExternoResponseDTO;
import com.proyecto.web.entity.MensajeExterno;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.MensajeExternoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MensajeExternoService {

    private final MensajeExternoRepository mensajeExternoRepository;

    public MensajeExternoResponseDTO crear(MensajeExternoRequestDTO dto) {
        MensajeExterno m = MensajeExterno.builder()
                .destinoTipo(dto.getDestinoTipo())
                .configuracion(dto.getConfiguracion())
                .credenciales(dto.getCredenciales())
                .eliminado(false)
                .build();
        return toDto(mensajeExternoRepository.save(m));
    }

    public List<MensajeExternoResponseDTO> listar() {
        return mensajeExternoRepository.findAllByEliminadoFalse().stream()
                .map(this::toDto)
                .toList();
    }

    public MensajeExternoResponseDTO obtener(Long id) {
        return toDto(buscarActivo(id));
    }

    @Transactional
    public MensajeExternoResponseDTO actualizar(Long id, MensajeExternoRequestDTO dto) {
        MensajeExterno m = buscarActivo(id);
        m.setDestinoTipo(dto.getDestinoTipo());
        m.setConfiguracion(dto.getConfiguracion());
        m.setCredenciales(dto.getCredenciales());
        return toDto(mensajeExternoRepository.save(m));
    }

    @Transactional
    public void eliminar(Long id) {
        MensajeExterno m = buscarActivo(id);
        m.setEliminado(true);
        mensajeExternoRepository.save(m);
    }

    public MensajeExterno buscarActivoEntidad(Long id) {
        return buscarActivo(id);
    }

    private MensajeExterno buscarActivo(Long id) {
        return mensajeExternoRepository.findById(id)
                .filter(x -> !x.isEliminado())
                .orElseThrow(() -> new BusinessException("Mensaje externo no encontrado", HttpStatus.NOT_FOUND));
    }

    private MensajeExternoResponseDTO toDto(MensajeExterno m) {
        return MensajeExternoResponseDTO.builder()
                .id(m.getId())
                .destinoTipo(m.getDestinoTipo())
                .configuracion(m.getConfiguracion())
                .credenciales(m.getCredenciales())
                .build();
    }
}
