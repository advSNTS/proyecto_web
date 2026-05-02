package com.proyecto.web.mapper;

import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.entity.Pool;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.enums.EstadoProceso;

public class ProcesoMapper {
    private ProcesoMapper() {}

    public static EstadoProceso resolveEstadoDesdeDto(ProcesoRequestDTO dto) {
        if (dto.getEstado() != null) {
            return dto.getEstado();
        }
        if (Boolean.FALSE.equals(dto.getActivo())) {
            return EstadoProceso.INACTIVO;
        }
        if (Boolean.TRUE.equals(dto.getBorrador())) {
            return EstadoProceso.BORRADOR;
        }
        if (Boolean.TRUE.equals(dto.getActivo()) && Boolean.FALSE.equals(dto.getBorrador())) {
            return EstadoProceso.PUBLICADO;
        }
        if (dto.getBorrador() == null && dto.getActivo() == null) {
            return EstadoProceso.BORRADOR;
        }
        return EstadoProceso.BORRADOR;
    }

    public static Proceso toEntity(ProcesoRequestDTO dto, Empresa empresa, Pool pool) {
        return Proceso.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .categoria(dto.getCategoria())
                .estado(resolveEstadoDesdeDto(dto))
                .empresa(empresa)
                .pool(pool)
                .build();
    }

    public static ProcesoResponseDTO toResponse(Proceso proceso) {
        EstadoProceso e = proceso.getEstado();
        return ProcesoResponseDTO.builder()
                .id(proceso.getId())
                .nombre(proceso.getNombre())
                .descripcion(proceso.getDescripcion())
                .categoria(proceso.getCategoria())
                .estado(e)
                .poolId(proceso.getPool() != null ? proceso.getPool().getId() : null)
                .nitEmpresa(proceso.getEmpresa() != null ? proceso.getEmpresa().getNit() : null)
                .borrador(e == EstadoProceso.BORRADOR)
                .activo(e != EstadoProceso.INACTIVO)
                .build();
    }
}
