package com.proyecto.web;

import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.RolRequestDTO;
import com.proyecto.web.dto.RolResponseDTO;
import com.proyecto.web.enums.Permiso;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.RolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RolServiceTest {

    @Autowired
    private RolService rolService;

    @Autowired
    private EmpresaService empresaService;

    @BeforeEach
    void setUp() {
        // Crear empresa de prueba
        EmpresaRequestDTO empresaDTO = new EmpresaRequestDTO();
        empresaDTO.setNit("NIT-ROL-TEST-001");
        empresaDTO.setNombre("Empresa Roles");
        empresaDTO.setCorreo("roles@test.com");
        empresaService.crearEmpresa(empresaDTO);
    }

    @Test
    void crearRol_deberiaRetornarRolCreado() {
        RolRequestDTO dto = RolRequestDTO.builder()
                .nitEmpresa("NIT-ROL-TEST-001")
                .nombre("Admin")
                .permiso(Permiso.ADMINISTRAR)
                .build();

        RolResponseDTO response = rolService.crearRol(dto);

        assertNotNull(response);
        assertEquals("Admin", response.getNombre());
        assertEquals(Permiso.ADMINISTRAR, response.getPermiso());
        assertNotNull(response.getId());
    }

    @Test
    void obtenerRol_deberiaRetornarRolExistente() {
        RolRequestDTO dto = RolRequestDTO.builder()
                .nitEmpresa("NIT-ROL-TEST-001")
                .nombre("Editor")
                .permiso(Permiso.EDITAR)
                .build();

        RolResponseDTO creado = rolService.crearRol(dto);

        RolResponseDTO response = rolService.obtenerRol(creado.getId());

        assertNotNull(response);
        assertEquals("Editor", response.getNombre());
        assertEquals(Permiso.EDITAR, response.getPermiso());
    }

    @Test
    void obtenerRoles_deberiaRetornarLista() {
        RolRequestDTO dto1 = RolRequestDTO.builder()
                .nitEmpresa("NIT-ROL-TEST-001")
                .nombre("Viewer")
                .permiso(Permiso.VER)
                .build();

        RolRequestDTO dto2 = RolRequestDTO.builder()
                .nitEmpresa("NIT-ROL-TEST-001")
                .nombre("Moderador")
                .permiso(Permiso.EDITAR)
                .build();

        rolService.crearRol(dto1);
        rolService.crearRol(dto2);

        List<RolResponseDTO> lista = rolService.obtenerRoles();

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(r -> r.getNombre().equals("Viewer")));
    }

    @Test
    void obtenerRolesPorEmpresa_deberiaRetornarRolesDeLaEmpresa() {
        RolRequestDTO dto1 = RolRequestDTO.builder()
                .nitEmpresa("NIT-ROL-TEST-001")
                .nombre("Rol Uno")
                .permiso(Permiso.VER)
                .build();

        RolRequestDTO dto2 = RolRequestDTO.builder()
                .nitEmpresa("NIT-ROL-TEST-001")
                .nombre("Rol Dos")
                .permiso(Permiso.ADMINISTRAR)
                .build();

        rolService.crearRol(dto1);
        rolService.crearRol(dto2);

        List<RolResponseDTO> lista = rolService.obtenerRolesPorEmpresa("NIT-ROL-TEST-001");

        assertNotNull(lista);
        assertTrue(lista.size() >= 2);
    }

    @Test
    void actualizarRol_deberiaRetornarRolActualizado() {
        RolRequestDTO dto = RolRequestDTO.builder()
                .nitEmpresa("NIT-ROL-TEST-001")
                .nombre("RolOriginal")
                .permiso(Permiso.VER)
                .build();

        RolResponseDTO creado = rolService.crearRol(dto);

        RolRequestDTO update = RolRequestDTO.builder()
                .nitEmpresa("NIT-ROL-TEST-001")
                .nombre("RolActualizado")
                .permiso(Permiso.ADMINISTRAR)
                .build();

        RolResponseDTO response = rolService.actualizarRol(creado.getId(), update);

        assertEquals("RolActualizado", response.getNombre());
        assertEquals(Permiso.ADMINISTRAR, response.getPermiso());
    }

    @Test
    void eliminarRol_deberiaMarcarComoEliminado() {
        RolRequestDTO dto = RolRequestDTO.builder()
                .nitEmpresa("NIT-ROL-TEST-001")
                .nombre("RolParaEliminar")
                .permiso(Permiso.EDITAR)
                .build();

        RolResponseDTO creado = rolService.crearRol(dto);

        rolService.eliminarRol(creado.getId());

        Long id = creado.getId();
        assertThrows(RuntimeException.class, () -> rolService.obtenerRol(id));
    }
}
