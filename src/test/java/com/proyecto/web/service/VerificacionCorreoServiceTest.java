package com.proyecto.web.service;

import com.proyecto.web.dto.CredencialRequestDTO;
import com.proyecto.web.dto.EmpleadoRequestDTO;
import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.VerificacionCorreoResponseDTO;
import com.proyecto.web.entity.Credencial;
import com.proyecto.web.enums.TipoDocumento;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.CredencialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("VerificacionCorreoService Tests")
class VerificacionCorreoServiceTest {

    @Autowired
    private VerificacionCorreoService verificacionCorreoService;

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private CredencialRepository credencialRepository;

    private static final String NIT = "900-VERIF-SVC";

    @BeforeEach
    void crearEmpresaBase() {
        EmpresaRequestDTO empresa = new EmpresaRequestDTO();
        empresa.setNit(NIT);
        empresa.setNombre("Empresa Verificacion");
        empresa.setCorreo("empresa.verif@test.com");
        empresaService.crearEmpresa(empresa);
    }

    @Test
    @DisplayName("verificarCorreo activa credencial con token válido")
    void verificarCorreo_exito() {
        empleadoService.crearEmpleado(empleado("verificar.ok@test.com", "111"));

        Credencial credencial = credencialRepository.findByCorreoIgnoreCase("verificar.ok@test.com").orElseThrow();
        String token = credencial.getTokenVerificacion();
        assertNotNull(token);

        VerificacionCorreoResponseDTO response = verificacionCorreoService.verificarCorreo(token);

        assertTrue(response.isVerificado());
        assertEquals("verificar.ok@test.com", response.getCorreo());

        Credencial actualizada = credencialRepository.findByCorreoIgnoreCase("verificar.ok@test.com").orElseThrow();
        assertTrue(actualizada.isVerificado());
        assertNull(actualizada.getTokenVerificacion());
    }

    @Test
    @DisplayName("verificarCorreo rechaza token nulo o vacío")
    void verificarCorreo_tokenInvalido() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> verificacionCorreoService.verificarCorreo("  "));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    @DisplayName("verificarCorreo rechaza token inexistente")
    void verificarCorreo_tokenNoEncontrado() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> verificacionCorreoService.verificarCorreo("token-inexistente"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    @DisplayName("reenviarVerificacion para correo no verificado genera nuevo token")
    void reenviarVerificacion_pendiente() {
        empleadoService.crearEmpleado(empleado("reenviar@test.com", "222"));

        VerificacionCorreoResponseDTO response =
                verificacionCorreoService.reenviarVerificacion("reenviar@test.com");

        assertFalse(response.isVerificado());
        assertEquals("reenviar@test.com", response.getCorreo());

        Credencial credencial = credencialRepository.findByCorreoIgnoreCase("reenviar@test.com").orElseThrow();
        assertNotNull(credencial.getTokenVerificacion());
    }

    @Test
    @DisplayName("reenviarVerificacion para correo ya verificado no reenvía")
    void reenviarVerificacion_yaVerificado() {
        empleadoService.crearEmpleado(empleado("ya.verif@test.com", "333"));

        Credencial credencial = credencialRepository.findByCorreoIgnoreCase("ya.verif@test.com").orElseThrow();
        verificacionCorreoService.verificarCorreo(credencial.getTokenVerificacion());

        VerificacionCorreoResponseDTO response =
                verificacionCorreoService.reenviarVerificacion("ya.verif@test.com");

        assertTrue(response.isVerificado());
    }

    @Test
    @DisplayName("reenviarVerificacion rechaza correo inexistente")
    void reenviarVerificacion_correoNoExiste() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> verificacionCorreoService.reenviarVerificacion("noexiste@test.com"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("reenviarVerificacion rechaza correo vacío")
    void reenviarVerificacion_correoVacio() {
        assertThrows(BusinessException.class, () -> verificacionCorreoService.reenviarVerificacion(null));
    }

    private EmpleadoRequestDTO empleado(String correo, String documento) {
        return EmpleadoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("Empleado Verif")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento(documento)
                .credencial(CredencialRequestDTO.builder()
                        .correo(correo)
                        .contrasena("password123")
                        .build())
                .build();
    }
}
