package com.proyecto.web.service;

import com.proyecto.web.dto.EmpleadoRequestDTO;
import com.proyecto.web.dto.EmpleadoResponseDTO;
import com.proyecto.web.entity.Empleado;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.mapper.EmpleadoMapper;
import com.proyecto.web.repository.EmpleadoRepository;
import com.proyecto.web.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final EmpresaRepository empresaRepository;

    public EmpleadoResponseDTO crearEmpleado(EmpleadoRequestDTO dto) {
        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(dto.getNitEmpresa())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        Empleado empleado = EmpleadoMapper.toEntity(dto, empresa);
        return EmpleadoMapper.toResponse(empleadoRepository.save(empleado));
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
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        return EmpleadoMapper.toResponse(empleado);
    }

    public EmpleadoResponseDTO actualizarEmpleado(Long id, EmpleadoRequestDTO dto) {
        Empleado empleado = empleadoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(dto.getNitEmpresa())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        empleado.setEmpresa(empresa);
        empleado.setNombre(dto.getNombre());
        empleado.setTipoDocumento(dto.getTipoDocumento());
        empleado.setNumeroDocumento(dto.getNumeroDocumento());

        return EmpleadoMapper.toResponse(empleadoRepository.save(empleado));
    }

    public void eliminarEmpleado(Long id) {
        Empleado empleado = empleadoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        empleado.setDeleted(true);
        empleadoRepository.save(empleado);
    }
}