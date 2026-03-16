package com.proyecto.web.service;

import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.EmpresaResponseDTO;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.mapper.EmpresaMapper;
import com.proyecto.web.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaResponseDTO crearEmpresa(EmpresaRequestDTO dto) {

        Empresa empresa = EmpresaMapper.toEntity(dto);

        empresa = empresaRepository.save(empresa);

        return EmpresaMapper.toResponse(empresa);
    }

    public List<EmpresaResponseDTO> obtenerEmpresas() {

        List<Empresa> empresas = empresaRepository.findAll();
        List<EmpresaResponseDTO> resultado = new ArrayList<>();

        for (Empresa empresa : empresas) {
            resultado.add(EmpresaMapper.toResponse(empresa));
        }
        return resultado;
    }

    public EmpresaResponseDTO obtenerEmpresa(String nit) {

        Empresa empresa = empresaRepository.findById(nit)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        return EmpresaMapper.toResponse(empresa);
    }
    
}
