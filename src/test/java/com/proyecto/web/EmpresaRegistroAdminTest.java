package com.proyecto.web;

import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.entity.EmpleadoRolSistema;
import com.proyecto.web.enums.TipoRolSistema;
import com.proyecto.web.repository.CredencialRepository;
import com.proyecto.web.repository.EmpleadoRolSistemaRepository;
import com.proyecto.web.service.EmpresaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EmpresaRegistroAdminTest {

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private CredencialRepository credencialRepository;

    @Autowired
    private EmpleadoRolSistemaRepository empleadoRolSistemaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void crearEmpresa_generaAdministradorConRolAdminYPasswordHash() {
        EmpresaRequestDTO dto = new EmpresaRequestDTO();
        dto.setNit("NIT-REG-ADM-01");
        dto.setNombre("Empresa Registro");
        dto.setCorreo("admin.reg@test.com");
        dto.setContrasenaAdministrador("MiClaveSegura1!");

        empresaService.crearEmpresa(dto);

        var cred = credencialRepository.findByCorreo("admin.reg@test.com").orElseThrow();
        assertTrue(passwordEncoder.matches("MiClaveSegura1!", cred.getContrasena()));

        Long empleadoId = cred.getEmpleado().getId();
        List<EmpleadoRolSistema> roles = empleadoRolSistemaRepository.findAllByEmpleado_IdAndEliminadoFalse(empleadoId);
        assertTrue(roles.stream().anyMatch(r -> r.getTipoRol() == TipoRolSistema.ADMIN));
    }

    @Test
    void crearEmpresa_sinPassword_usaDefaultDocumentada() {
        EmpresaRequestDTO dto = new EmpresaRequestDTO();
        dto.setNit("NIT-REG-ADM-02");
        dto.setNombre("Empresa Default Pwd");
        dto.setCorreo("admin.def@test.com");

        empresaService.crearEmpresa(dto);

        var cred = credencialRepository.findByCorreo("admin.def@test.com").orElseThrow();
        assertTrue(passwordEncoder.matches(EmpresaService.CONTRASENA_ADMIN_POR_DEFECTO, cred.getContrasena()));
    }
}
