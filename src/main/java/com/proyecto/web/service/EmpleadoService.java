package com.proyecto.web.service;

import com.proyecto.web.dto.EmpleadoRequestDTO;
import com.proyecto.web.dto.EmpleadoLoginRequestDTO;
import com.proyecto.web.dto.EmpleadoLoginResponseDTO;
import com.proyecto.web.dto.EmpleadoResponseDTO;
import com.proyecto.web.entity.Credencial;
import com.proyecto.web.entity.Empleado;
import com.proyecto.web.entity.EmpleadoRolSistema;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.enums.TipoRolSistema;
import com.proyecto.web.exception.AuthenticationException;
import com.proyecto.web.mapper.EmpleadoMapper;
import com.proyecto.web.repository.CredencialRepository;
import com.proyecto.web.repository.EmpleadoRepository;
import com.proyecto.web.repository.EmpleadoRolSistemaRepository;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.EmpresaRepository;
import com.proyecto.web.security.JwtService;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpleadoService {

    private static final String EMPLEADO_NO_ENCONTRADO = "Empleado no encontrado";
    private static final String EMPRESA_NO_ENCONTRADA = "Empresa no encontrada";
    private static final String CREDENCIALES_INVALIDAS = "Credenciales invalidas";

    private final EmpleadoRepository empleadoRepository;
private final EmpresaRepository empresaRepository;
private final CredencialRepository credencialRepository;
private final EmpleadoRolSistemaRepository empleadoRolSistemaRepository;
private final JwtService jwtService;
private final PasswordEncoder passwordEncoder;
private final VerificacionCorreoService verificacionCorreoService;

    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

    @Transactional
    public EmpleadoResponseDTO crearEmpleado(EmpleadoRequestDTO dto) {
        if (credencialRepository.existsByCorreo(dto.getCredencial().getCorreo().trim())) {
            throw new BusinessException("Ya existe un usuario con ese correo", HttpStatus.CONFLICT);
        }
        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(dto.getNitEmpresa())
                .orElseThrow(() -> new RuntimeException(EMPRESA_NO_ENCONTRADA));

        Empleado empleado = EmpleadoMapper.toEntity(dto, empresa);
        empleado = empleadoRepository.save(empleado); // necesita ID antes de crear credencial

        Credencial credencial = Credencial.builder()
        .empleado(empleado)
        .correo(dto.getCredencial().getCorreo().trim())
        .contrasena(passwordEncoder.encode(dto.getCredencial().getContrasena()))
        .verificado(false)
        .build();

credencialRepository.save(credencial);
verificacionCorreoService.crearTokenYEnviar(credencial, empleado.getNombre());

empleado.setCredencial(credencial);

        empleadoRolSistemaRepository.save(EmpleadoRolSistema.builder()
                .empleado(empleado)
                .empresa(empresa)
                .tipoRol(TipoRolSistema.READER)
                .eliminado(false)
                .build());

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

        if (dto.getCredencial() != null && empleado.getCredencial() != null) {
    Credencial credencial = empleado.getCredencial();

    if (dto.getCredencial().getCorreo() != null && !dto.getCredencial().getCorreo().isBlank()) {
        String nuevoCorreo = dto.getCredencial().getCorreo().trim();

        if (!nuevoCorreo.equalsIgnoreCase(credencial.getCorreo())) {
            if (credencialRepository.existsByCorreo(nuevoCorreo)) {
                throw new BusinessException("Ya existe un usuario con ese correo", HttpStatus.CONFLICT);
            }

            credencial.setCorreo(nuevoCorreo);
            credencial.setVerificado(false);
            credencial.setFechaVerificacion(null);
            credencialRepository.save(credencial);

            verificacionCorreoService.crearTokenYEnviar(credencial, empleado.getNombre());
        }
    }

    if (dto.getCredencial().getContrasena() != null && !dto.getCredencial().getContrasena().isBlank()) {
        credencial.setContrasena(passwordEncoder.encode(dto.getCredencial().getContrasena()));
        credencialRepository.save(credencial);
    }
}

        return EmpleadoMapper.toResponse(empleadoRepository.save(empleado));
    }

    public void eliminarEmpleado(Long id) {
        Empleado empleado = empleadoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException(EMPLEADO_NO_ENCONTRADO));
        empleado.setDeleted(true);
        empleadoRepository.save(empleado);
    }

    @Transactional
    public EmpleadoLoginResponseDTO login(EmpleadoLoginRequestDTO dto) {
        Credencial credencial = credencialRepository.findByCorreo(dto.getCorreo().trim())
                .orElseThrow(() -> new AuthenticationException(CREDENCIALES_INVALIDAS));

        Empleado empleado = credencial.getEmpleado();
        if (empleado == null || empleado.isDeleted()) {
            throw new AuthenticationException(CREDENCIALES_INVALIDAS);
        }

        String stored = credencial.getContrasena();
boolean passwordOk = stored != null
        && (passwordEncoder.matches(dto.getContrasena(), stored)
        || stored.equals(dto.getContrasena()));

if (!passwordOk) {
    throw new AuthenticationException(CREDENCIALES_INVALIDAS);
}

if (!credencial.isVerificado()) {
    throw new AuthenticationException("Debe verificar su correo antes de iniciar sesion.");
}

String token = null;

        if (securityEnabled) {
            List<TipoRolSistema> roles = empleadoRolSistemaRepository.findAllByEmpleado_IdAndEliminadoFalse(empleado.getId())
                    .stream()
                    .map(EmpleadoRolSistema::getTipoRol)
                    .toList();
            token = jwtService.generarToken(
                    empleado.getId(),
                    empleado.getEmpresa().getNit(),
                    credencial.getCorreo(),
                    empleado.isAdminGlobal(),
                    roles);
        }

        return EmpleadoLoginResponseDTO.builder()
                .id(empleado.getId())
                .nitEmpresa(empleado.getEmpresa().getNit())
                .nombreEmpresa(empleado.getEmpresa().getNombre())
                .nombre(empleado.getNombre())
                .tipoDocumento(empleado.getTipoDocumento())
                .numeroDocumento(empleado.getNumeroDocumento())
                .correo(credencial.getCorreo())
                .token(token)
                .build();
    }
}
