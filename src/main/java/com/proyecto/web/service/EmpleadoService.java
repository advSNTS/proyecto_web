package com.proyecto.web.service;

import com.proyecto.web.dto.EmpleadoRequestDTO;
import com.proyecto.web.dto.EmpleadoResponseDTO;
import com.proyecto.web.entity.Credencial;
import com.proyecto.web.entity.Empleado;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.mapper.EmpleadoMapper;
import com.proyecto.web.repository.EmpleadoRepository;
import com.proyecto.web.repository.EmpresaRepository;

import jakarta.transaction.Transactional;

import com.proyecto.web.repository.CredencialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpleadoService {

    private static final String EMPLEADO_NO_ENCONTRADO = "Empleado no encontrado";
    private static final String EMPRESA_NO_ENCONTRADA = "Empresa no encontrada";

    private final EmpleadoRepository empleadoRepository;
    private final EmpresaRepository empresaRepository;
    private final CredencialRepository credencialRepository;

    @Transactional
    public EmpleadoResponseDTO crearEmpleado(EmpleadoRequestDTO dto) {
        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(dto.getNitEmpresa())
                .orElseThrow(() -> new RuntimeException(EMPRESA_NO_ENCONTRADA));

        Empleado empleado = EmpleadoMapper.toEntity(dto, empresa);
        empleado = empleadoRepository.save(empleado); // necesita ID antes de crear credencial

        Credencial credencial = EmpleadoMapper.toCredencial(dto, empleado);
        credencialRepository.save(credencial);

        empleado.setCredencial(credencial);
        return EmpleadoMapper.toResponse(empleado);
    }

    public List<EmpleadoResponseDTO> obtenerEmpleados() {
        return empleadoRepository.findAllByDeletedFalse()
                .stream()
                .map(EmpleadoMapper::toResponse)
                .toList();
    }

    public List<EmpleadoResponseDTO> obtenerEmpleadosPorEmpresa(String nit) {
        return empleadoRepository.findAllByEmpresa_NitAndDeletedFalse(nit)
                .stream()
                .map(EmpleadoMapper::toResponse)
                .toList();
    }

    public EmpleadoResponseDTO obtenerEmpleado(Long id) {
        Empleado empleado = empleadoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException(EMPLEADO_NO_ENCONTRADO));
        return EmpleadoMapper.toResponse(empleado);
    }

    @Transactional
    public EmpleadoResponseDTO actualizarEmpleado(Long id, EmpleadoRequestDTO dto) {
        Empleado empleado = empleadoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException(EMPLEADO_NO_ENCONTRADO));

        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(dto.getNitEmpresa())
                .orElseThrow(() -> new RuntimeException(EMPRESA_NO_ENCONTRADA));

        empleado.setEmpresa(empresa);
        empleado.setNombre(dto.getNombre());
        empleado.setTipoDocumento(dto.getTipoDocumento());
        empleado.setNumeroDocumento(dto.getNumeroDocumento());

        // actualizar correo si viene en el request
        if (dto.getCredencial() != null && empleado.getCredencial() != null) {
            empleado.getCredencial().setCorreo(dto.getCredencial().getCorreo());
            credencialRepository.save(empleado.getCredencial());
        }

        return EmpleadoMapper.toResponse(empleadoRepository.save(empleado));
    }

    public void eliminarEmpleado(Long id) {
        Empleado empleado = empleadoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException(EMPLEADO_NO_ENCONTRADO));
        empleado.setDeleted(true);
        empleadoRepository.save(empleado);
    }
}