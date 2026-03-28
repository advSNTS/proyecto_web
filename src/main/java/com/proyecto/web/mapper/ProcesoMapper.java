package com.proyecto.web.mapper;
 
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.entity.Proceso;
 
public class ProcesoMapper {
    private ProcesoMapper() {}
 
    public static Proceso toEntity(ProcesoRequestDTO dto) {
        return Proceso.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .categoria(dto.getCategoria())
                .borrador(dto.getBorrador())
                .activo(dto.getActivo())
                .build();
    }
 
    public static ProcesoResponseDTO toResponse(Proceso proceso) {
        return ProcesoResponseDTO.builder()
                .id(proceso.getId())
                .nombre(proceso.getNombre())
                .descripcion(proceso.getDescripcion())
                .categoria(proceso.getCategoria())
                .borrador(proceso.getBorrador())
                .activo(proceso.getActivo())
                .build();
    }
}
 