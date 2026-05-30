package com.proyecto.web.mapper;

import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.enums.EstadoProceso;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcesoMapperTest {

    @Test
    void resolveEstadoDesdeDto_activoFalse_deberiaSerInactivo() {
        ProcesoRequestDTO dto = ProcesoRequestDTO.builder()
                .activo(false)
                .borrador(false)
                .build();

        assertEquals(EstadoProceso.INACTIVO, ProcesoMapper.resolveEstadoDesdeDto(dto));
    }

    @Test
    void resolveEstadoDesdeDto_activoTrueYBorradorFalse_deberiaSerPublicado() {
        ProcesoRequestDTO dto = ProcesoRequestDTO.builder()
                .activo(true)
                .borrador(false)
                .build();

        assertEquals(EstadoProceso.PUBLICADO, ProcesoMapper.resolveEstadoDesdeDto(dto));
    }

    @Test
    void resolveEstadoDesdeDto_sinActivoNiBorrador_deberiaSerBorrador() {
        ProcesoRequestDTO dto = ProcesoRequestDTO.builder()
                .activo(null)
                .borrador(null)
                .build();

        assertEquals(EstadoProceso.BORRADOR, ProcesoMapper.resolveEstadoDesdeDto(dto));
    }
}
