package com.proyecto.web.service;

import com.proyecto.web.dto.EmpleadoLoginRequestDTO;
import com.proyecto.web.dto.EmpleadoLoginResponseDTO;
import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.repository.CredencialRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test", "sec-on"})
@TestPropertySource(properties = "app.security.enabled=true")
@Transactional
class EmpleadoServiceLoginJwtTest {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private CredencialRepository credencialRepository;

    @Autowired
    private VerificacionCorreoService verificacionCorreoService;

    @Test
    void login_conSeguridadActiva_devuelveToken() {
        EmpresaRequestDTO empresa = new EmpresaRequestDTO();
        empresa.setNit("NIT-LOGIN-JWT");
        empresa.setNombre("Emp JWT");
        empresa.setCorreo("admin.jwt@test.com");
        empresa.setContrasenaAdministrador("JwtLogin1!");
        empresaService.crearEmpresa(empresa);

        var credencial = credencialRepository.findByCorreo("admin.jwt@test.com").orElseThrow();
        verificacionCorreoService.verificarCorreo(credencial.getTokenVerificacion());

        EmpleadoLoginResponseDTO response = empleadoService.login(EmpleadoLoginRequestDTO.builder()
                .correo("admin.jwt@test.com")
                .contrasena("JwtLogin1!")
                .build());

        assertNotNull(response.getToken());
        assertFalse(response.getToken().isBlank());
    }
}
