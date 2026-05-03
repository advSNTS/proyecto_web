package com.proyecto.web;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RolControllerTest {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RolService rolService;

    @Autowired
    private EmpresaService empresaService;

    private Long rolId;
    private String nitEmpresa = "NIT-ROL-CTRL-001";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        // Crear empresa
        EmpresaRequestDTO empresaDTO = new EmpresaRequestDTO();
        empresaDTO.setNit(nitEmpresa);
        empresaDTO.setNombre("Empresa Roles");
        empresaDTO.setCorreo("roles@test.com");
        empresaService.crearEmpresa(empresaDTO);

        // Crear rol de prueba
        RolRequestDTO rolDTO = RolRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .nombre("Rol Test")
                .permiso(Permiso.ADMINISTRAR)
                .build();

        RolResponseDTO rol = rolService.crearRol(rolDTO);
        this.rolId = rol.getId();
    }

    @Test
    void crearRol_retorna200() throws Exception {
        RolRequestDTO dto = RolRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .nombre("Nuevo Rol")
                .permiso(Permiso.EDITAR)
                .build();

        mockMvc.perform(post("/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo Rol"))
                .andExpect(jsonPath("$.permiso").value("EDITAR"))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void obtenerTodosRoles_retorna200ConContenido() throws Exception {
        mockMvc.perform(get("/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0]", notNullValue()));
    }

    @Test
    void obtenerRolesPorEmpresa_retorna200() throws Exception {
        mockMvc.perform(get("/roles/empresa/" + nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void obtenerRolPorId_retorna200() throws Exception {
        mockMvc.perform(get("/roles/" + rolId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Rol Test"))
                .andExpect(jsonPath("$.permiso").value("ADMINISTRAR"))
                .andExpect(jsonPath("$.id").value(rolId.intValue()))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void obtenerRolInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/roles/99999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarRol_retorna200() throws Exception {
        RolRequestDTO updateDTO = RolRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .nombre("Nombre Actualizado")
                .permiso(Permiso.VER)
                .build();

        mockMvc.perform(put("/roles/" + rolId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre Actualizado"))
                .andExpect(jsonPath("$.permiso").value("VER"));
    }

    @Test
    void eliminarRol_retorna204() throws Exception {
        mockMvc.perform(delete("/roles/" + rolId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que ya no existe
        mockMvc.perform(get("/roles/" + rolId))
                .andExpect(status().isNotFound());
    }
}
