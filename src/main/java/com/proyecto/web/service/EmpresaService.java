package com.proyecto.web.service;

import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.EmpresaResponseDTO;
import com.proyecto.web.entity.Credencial;
import com.proyecto.web.entity.Empleado;
import com.proyecto.web.entity.EmpleadoRolSistema;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.entity.Pool;
import com.proyecto.web.enums.TipoDocumento;
import com.proyecto.web.enums.TipoRolSistema;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.mapper.EmpresaMapper;
import com.proyecto.web.repository.CredencialRepository;
import com.proyecto.web.repository.EmpleadoRepository;
import com.proyecto.web.repository.EmpleadoRolSistemaRepository;
import com.proyecto.web.repository.EmpresaRepository;
import com.proyecto.web.repository.PoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmpresaService {

    private static final String EMPRESA_NO_ENCONTRADA = "Empresa no encontrada";

    public static final String CONTRASENA_ADMIN_POR_DEFECTO = "Admin123!";

    private final EmpresaRepository empresaRepository;
    private final PoolRepository poolRepository;
    private final EmpleadoRepository empleadoRepository;
    private final CredencialRepository credencialRepository;
    private final EmpleadoRolSistemaRepository empleadoRolSistemaRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificacionCorreoService verificacionCorreoService;

    @Transactional
    public EmpresaResponseDTO crearEmpresa(EmpresaRequestDTO dto) {
        validarRegistroEmpresa(dto);

        String correoAdmin = dto.getCorreo().trim();

        if (credencialRepository.existsByCorreoIgnoreCase(correoAdmin)) {
            throw new BusinessException("Ya existe un usuario con ese correo", HttpStatus.CONFLICT);
        }

        Empresa empresa = EmpresaMapper.toEntity(dto);
        empresa.setCorreo(correoAdmin);
        empresa = empresaRepository.save(empresa);

        Pool poolDefault = Pool.builder()
                .empresa(empresa)
                .nombre("Pool principal")
                .descripcion("Creado automáticamente al registrar la empresa")
                .esDefault(true)
                .eliminado(false)
                .build();

        poolRepository.save(poolDefault);

        String passwordPlano = (dto.getContrasenaAdministrador() != null
                && !dto.getContrasenaAdministrador().isBlank())
                ? dto.getContrasenaAdministrador()
                : CONTRASENA_ADMIN_POR_DEFECTO;

        boolean passwordAutogenerada = dto.getContrasenaAdministrador() == null
                || dto.getContrasenaAdministrador().isBlank();

        Empleado admin = Empleado.builder()
                .empresa(empresa)
                .nombre("Administrador")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento(normalizarNumeroDocumento(dto.getNit()))
                .adminGlobal(false)
                .deleted(false)
                .build();

        admin = empleadoRepository.save(admin);

        Credencial credencial = Credencial.builder()
                .empleado(admin)
                .correo(correoAdmin)
                .contrasena(passwordEncoder.encode(passwordPlano))
                .verificado(false)
                .build();

        credencialRepository.save(credencial);

        verificacionCorreoService.crearTokenYEnviar(credencial, admin.getNombre());

        empleadoRolSistemaRepository.save(EmpleadoRolSistema.builder()
                .empleado(admin)
                .empresa(empresa)
                .tipoRol(TipoRolSistema.ADMIN)
                .eliminado(false)
                .build());

        log.info(
                "Empresa, pool y administrador creados nit={} correoAdmin={}",
                empresa.getNit(),
                correoAdmin
        );

        EmpresaResponseDTO response = EmpresaMapper.toResponse(empresa);

        if (passwordAutogenerada) {
            response.setMensajeRegistro(
                    "Registro exitoso. El usuario administrador usa el correo de contacto de la empresa y la "
                            + "contraseña inicial definida en la guía del proyecto. "
                            + "Antes de iniciar sesión debe verificar su correo.");
        } else {
            response.setMensajeRegistro(
                    "Registro exitoso. Enviamos un correo de verificación al correo de contacto. "
                            + "El administrador debe verificar su correo antes de iniciar sesión.");
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponseDTO> obtenerEmpresas() {
        return empresaRepository.findAllByDeletedFalse()
                .stream()
                .map(EmpresaMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmpresaResponseDTO obtenerEmpresa(String nit) {
        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(nit)
                .orElseThrow(() -> new BusinessException(EMPRESA_NO_ENCONTRADA, HttpStatus.NOT_FOUND));

        return EmpresaMapper.toResponse(empresa);
    }

    @Transactional
    public EmpresaResponseDTO actualizarEmpresa(String nit, EmpresaRequestDTO dto) {
        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(nit)
                .orElseThrow(() -> new BusinessException(EMPRESA_NO_ENCONTRADA, HttpStatus.NOT_FOUND));

        empresa.setNombre(dto.getNombre());

        if (dto.getCorreo() != null && !dto.getCorreo().isBlank()) {
            empresa.setCorreo(dto.getCorreo().trim());
        }

        return EmpresaMapper.toResponse(empresaRepository.save(empresa));
    }

    @Transactional
    public void eliminarEmpresa(String nit) {
        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(nit)
                .orElseThrow(() -> new BusinessException(EMPRESA_NO_ENCONTRADA, HttpStatus.NOT_FOUND));

        empresa.setDeleted(true);

        empresaRepository.save(empresa);
    }

    private void validarRegistroEmpresa(EmpresaRequestDTO dto) {
        if (dto.getCorreo() == null || dto.getCorreo().isBlank()) {
            throw new BusinessException("El correo de la empresa es obligatorio", HttpStatus.BAD_REQUEST);
        }
    }

    private String normalizarNumeroDocumento(String nit) {
        String doc = nit == null
                ? "0"
                : nit.replaceAll("[^0-9A-Za-z]", "");

        if (doc.isEmpty()) {
            return "0";
        }

        if (doc.length() > 40) {
            return doc.substring(0, 40);
        }

        return doc;
    }
}