package com.proyecto.web;

import com.proyecto.web.dto.CredencialRequestDTO;
import com.proyecto.web.dto.EmpleadoLoginRequestDTO;
import com.proyecto.web.dto.EmpleadoLoginResponseDTO;
import com.proyecto.web.dto.EmpleadoRequestDTO;
import com.proyecto.web.dto.EmpleadoResponseDTO;
import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.enums.TipoDocumento;
import com.proyecto.web.exception.AuthenticationException;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.CredencialRepository;
import com.proyecto.web.service.EmpleadoService;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.VerificacionCorreoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EmpleadoServiceTest {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private CredencialRepository credencialRepository;

    @Autowired
    private VerificacionCorreoService verificacionCorreoService;

    @BeforeEach
    void setUp() {
        // Crear empresa de prueba para que exista antes de crear empleados
        EmpresaRequestDTO empresaDTO = new EmpresaRequestDTO();
        empresaDTO.setNit("NIT-EMPRESA-001");
        empresaDTO.setNombre("Empresa Base Test");
        empresaDTO.setCorreo("empresa@test.com");
        empresaService.crearEmpresa(empresaDTO);
    }

    @Test
    void crearEmpleado_deberiaRetornarEmpleadoCreado() {
        EmpleadoRequestDTO dto = EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("Juan Pérez")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("123456789")
                .credencial(CredencialRequestDTO.builder()
                        .correo("juan@test.com")
                        .contrasena("password123")
                        .build())
                .build();

        EmpleadoResponseDTO response = empleadoService.crearEmpleado(dto);

        assertNotNull(response);
        assertEquals("Juan Pérez", response.getNombre());
        assertEquals("123456789", response.getNumeroDocumento());
        assertNotNull(response.getId());
    }

    @Test
    void obtenerEmpleado_deberiaRetornarEmpleadoExistente() {
        // Crear primero
        EmpleadoRequestDTO dto = EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("Carlos López")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("987654321")
                .credencial(CredencialRequestDTO.builder()
                        .correo("carlos@test.com")
                        .contrasena("password123")
                        .build())
                .build();

        EmpleadoResponseDTO creado = empleadoService.crearEmpleado(dto);

        // Luego buscar
        EmpleadoResponseDTO response = empleadoService.obtenerEmpleado(creado.getId());

        assertNotNull(response);
        assertEquals("Carlos López", response.getNombre());
        assertEquals(creado.getId(), response.getId());
    }

    @Test
    void obtenerEmpleados_deberiaRetornarLista() {
        EmpleadoRequestDTO dto = EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("María García")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("555666777")
                .credencial(CredencialRequestDTO.builder()
                        .correo("maria@test.com")
                        .contrasena("password123")
                        .build())
                .build();

        empleadoService.crearEmpleado(dto);

        List<EmpleadoResponseDTO> lista = empleadoService.obtenerEmpleados();

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(e -> e.getNombre().equals("María García")));
    }

    @Test
    void obtenerEmpleadosPorEmpresa_deberiaRetornarEmpleadosDeLaEmpresa() {
        EmpleadoRequestDTO dto1 = EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("Empleado Uno")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("111222333")
                .credencial(CredencialRequestDTO.builder()
                        .correo("empleado1@test.com")
                        .contrasena("password123")
                        .build())
                .build();

        EmpleadoRequestDTO dto2 = EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("Empleado Dos")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("444555666")
                .credencial(CredencialRequestDTO.builder()
                        .correo("empleado2@test.com")
                        .contrasena("password123")
                        .build())
                .build();

        empleadoService.crearEmpleado(dto1);
        empleadoService.crearEmpleado(dto2);

        List<EmpleadoResponseDTO> lista = empleadoService.obtenerEmpleadosPorEmpresa("NIT-EMPRESA-001");

        assertNotNull(lista);
        assertTrue(lista.size() >= 2);
    }

    @Test
    void actualizarEmpleado_deberiaRetornarEmpleadoActualizado() {
        EmpleadoRequestDTO dto = EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("Original")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("777888999")
                .credencial(CredencialRequestDTO.builder()
                        .correo("original@test.com")
                        .contrasena("password123")
                        .build())
                .build();

        EmpleadoResponseDTO creado = empleadoService.crearEmpleado(dto);

        EmpleadoRequestDTO update = EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("Actualizado")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("777888999")
                .credencial(CredencialRequestDTO.builder()
                        .correo("actualizado@test.com")
                        .contrasena("newpass")
                        .build())
                .build();

        EmpleadoResponseDTO response = empleadoService.actualizarEmpleado(creado.getId(), update);

        assertEquals("Actualizado", response.getNombre());
    }

    @Test
    void eliminarEmpleado_deberiaMarcarComoEliminado() {
        EmpleadoRequestDTO dto = EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("Para Eliminar")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("999888777")
                .credencial(CredencialRequestDTO.builder()
                        .correo("eliminar@test.com")
                        .contrasena("password123")
                        .build())
                .build();

        EmpleadoResponseDTO creado = empleadoService.crearEmpleado(dto);

        empleadoService.eliminarEmpleado(creado.getId());

        Long id = creado.getId();
        assertThrows(RuntimeException.class, () -> empleadoService.obtenerEmpleado(id));
    }

    @Test
    @DisplayName("login rechaza credenciales vacías")
    void login_credencialesVacias() {
        assertThrows(AuthenticationException.class, () -> empleadoService.login(
                EmpleadoLoginRequestDTO.builder().correo(" ").contrasena("").build()));
    }

    @Test
    @DisplayName("login rechaza correo inexistente")
    void login_correoInexistente() {
        assertThrows(AuthenticationException.class, () -> empleadoService.login(
                EmpleadoLoginRequestDTO.builder()
                        .correo("noexiste@test.com")
                        .contrasena("password123")
                        .build()));
    }

    @Test
    @DisplayName("login rechaza correo no verificado")
    void login_correoNoVerificado() {
        EmpleadoResponseDTO creado = empleadoService.crearEmpleado(EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("Sin Verificar")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("login-unver-01")
                .credencial(CredencialRequestDTO.builder()
                        .correo("noverif@test.com")
                        .contrasena("password123")
                        .build())
                .build());

        assertNotNull(creado.getId());
        assertThrows(AuthenticationException.class, () -> empleadoService.login(
                EmpleadoLoginRequestDTO.builder()
                        .correo("noverif@test.com")
                        .contrasena("password123")
                        .build()));
    }

    @Test
    @DisplayName("login exitoso tras verificar correo")
    void login_exitoTrasVerificacion() {
        empleadoService.crearEmpleado(EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("Login Verificado")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("login-ver-01")
                .credencial(CredencialRequestDTO.builder()
                        .correo("login.verif@test.com")
                        .contrasena("password123")
                        .build())
                .build());

        var credencial = credencialRepository.findByCorreoIgnoreCase("login.verif@test.com").orElseThrow();
        verificacionCorreoService.verificarCorreo(credencial.getTokenVerificacion());

        EmpleadoLoginResponseDTO response = empleadoService.login(EmpleadoLoginRequestDTO.builder()
                .correo("login.verif@test.com")
                .contrasena("password123")
                .build());

        assertEquals("login.verif@test.com", response.getCorreo());
        assertNull(response.getToken());
    }

    @Test
    @DisplayName("crearEmpleado sin credencial lanza BusinessException")
    void crearEmpleado_sinCredencial() {
        EmpleadoRequestDTO dto = EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("Sin Credencial")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("sin-cred-01")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> empleadoService.crearEmpleado(dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}
