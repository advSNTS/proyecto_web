package com.proyecto.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.service.EmpresaService;
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
class EmpresaControllerTest {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmpresaService empresaService;

    private String nitTest = "NIT-CTRL-001";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        // Crear empresa de prueba
        EmpresaRequestDTO empresaDTO = new EmpresaRequestDTO();
        empresaDTO.setNit(nitTest);
        empresaDTO.setNombre("Empresa Controlador");
        empresaDTO.setCorreo("empresa@test.com");
        empresaService.crearEmpresa(empresaDTO);
    }

    @Test
    void crearEmpresa_retorna200() throws Exception {
        EmpresaRequestDTO dto = new EmpresaRequestDTO();
        dto.setNit("NIT-NEW-001");
        dto.setNombre("Nueva Empresa");
        dto.setCorreo("nueva@test.com");

        mockMvc.perform(post("/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nit").value("NIT-NEW-001"))
                .andExpect(jsonPath("$.nombre").value("Nueva Empresa"))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void listarEmpresas_retorna200ConContenido() throws Exception {
        mockMvc.perform(get("/empresas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0]", notNullValue()));
    }

    @Test
    void obtenerEmpresaPorNit_retorna200() throws Exception {
        mockMvc.perform(get("/empresas/" + nitTest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nit").value(nitTest))
                .andExpect(jsonPath("$.nombre").value("Empresa Controlador"))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void obtenerEmpresaInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/empresas/NIT-INEXISTENTE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarEmpresa_retorna200() throws Exception {
        EmpresaRequestDTO updateDTO = new EmpresaRequestDTO();
        updateDTO.setNombre("Nombre Actualizado");
        updateDTO.setCorreo("actualizado@test.com");

        mockMvc.perform(put("/empresas/" + nitTest)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre Actualizado"))
                .andExpect(jsonPath("$.correo").value("actualizado@test.com"));
    }

    @Test
    void eliminarEmpresa_retorna200() throws Exception {
        mockMvc.perform(delete("/empresas/" + nitTest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verificar que ya no existe
        mockMvc.perform(get("/empresas/" + nitTest))
                .andExpect(status().isNotFound());
    }
}
