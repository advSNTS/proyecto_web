package com.proyecto.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.MensajeCatchRequestDTO;
import com.proyecto.web.dto.MensajeCatchResponseDTO;
import com.proyecto.web.entity.MensajeCatch;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.repository.MensajeCatchRepository;
import com.proyecto.web.repository.ProcesoRepository;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.MensajeCatchService;
import com.proyecto.web.service.ProcesoService;
import com.proyecto.web.support.IntegrationTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("MensajeCatchController Tests")
class MensajeCatchControllerTest {

    @Autowired
    private WebApplicationContext context;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private ProcesoService procesoService;

    @Autowired
    private ProcesoRepository procesoRepository;
    
    @Autowired
    private MensajeCatchRepository mensajeCatchRepository;
    
    private MockMvc mockMvc;
    private Proceso proceso;
    private String nitEmpresa = "123456789";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        setupTestData();
    }

    private void setupTestData() {
        var creado = IntegrationTestData.crearEmpresaYProceso(empresaService, procesoService, nitEmpresa);
        proceso = IntegrationTestData.cargarProceso(procesoRepository, creado.getId());
    }

    @Test
    @DisplayName("POST /api/mensajes-catch - Crear mensaje catch exitosamente")
    void testCrearMensajeCatch_Success() throws Exception {
        MensajeCatchRequestDTO request = MensajeCatchRequestDTO.builder()
                .procesoId(proceso.getId())
                .nombreMensaje("Mensaje 1")
                .correlacionExpr("expr1")
                .iniciarNuevaInstancia(true)
                .build();

        mockMvc.perform(post("/api/mensajes-catch")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombreMensaje").value("Mensaje 1"))
                .andExpect(jsonPath("$.iniciarNuevaInstancia").value(true));
    }

    @Test
    @DisplayName("POST /api/mensajes-catch - Fallar si nit es nulo")
    void testCrearMensajeCatch_NitNulo() throws Exception {
        MensajeCatchRequestDTO request = MensajeCatchRequestDTO.builder()
                .procesoId(proceso.getId())
                .nombreMensaje("Mensaje 1")
                .correlacionExpr("expr1")
                .iniciarNuevaInstancia(false)
                .build();

        mockMvc.perform(post("/api/mensajes-catch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/mensajes-catch - Fallar si proceso no existe")
    void testCrearMensajeCatch_ProcesoNoExiste() throws Exception {
        MensajeCatchRequestDTO request = MensajeCatchRequestDTO.builder()
                .procesoId(9999L)
                .nombreMensaje("Mensaje 1")
                .correlacionExpr("expr1")
                .iniciarNuevaInstancia(false)
                .build();

        mockMvc.perform(post("/api/mensajes-catch")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/mensajes-catch/proceso/{procesoId} - Listar por proceso")
    void testListarPorProceso_Success() throws Exception {
        // Crear mensaje catch
        MensajeCatch mensaje = new MensajeCatch();
        mensaje.setProceso(proceso);
        mensaje.setNombreMensaje("Mensaje Proceso");
        mensaje.setCorrelacionExpr("expr");
        mensaje.setIniciarNuevaInstancia(false);
        mensaje.setEliminado(false);
        mensajeCatchRepository.save(mensaje);

        mockMvc.perform(get("/api/mensajes-catch/proceso/{procesoId}", proceso.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreMensaje").value("Mensaje Proceso"));
    }

    @Test
    @DisplayName("GET /api/mensajes-catch/proceso/{procesoId} - Retorna vacío si no hay mensajes")
    void testListarPorProceso_Empty() throws Exception {
        mockMvc.perform(get("/api/mensajes-catch/proceso/{procesoId}", proceso.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/mensajes-catch/{id} - Obtener por ID")
    void testObtener_Success() throws Exception {
        // Crear mensaje catch
        MensajeCatch mensaje = new MensajeCatch();
        mensaje.setProceso(proceso);
        mensaje.setNombreMensaje("Mensaje Específico");
        mensaje.setCorrelacionExpr("expr");
        mensaje.setIniciarNuevaInstancia(true);
        mensaje.setEliminado(false);
        mensaje = mensajeCatchRepository.save(mensaje);

        mockMvc.perform(get("/api/mensajes-catch/{id}", mensaje.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mensaje.getId()))
                .andExpect(jsonPath("$.nombreMensaje").value("Mensaje Específico"));
    }

    @Test
    @DisplayName("GET /api/mensajes-catch/{id} - Fallar si no existe")
    void testObtener_NotFound() throws Exception {
        mockMvc.perform(get("/api/mensajes-catch/{id}", 9999L)
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("PUT /api/mensajes-catch/{id} - Actualizar mensaje catch")
    void testActualizar_Success() throws Exception {
        // Crear mensaje catch inicial
        MensajeCatch mensaje = new MensajeCatch();
        mensaje.setProceso(proceso);
        mensaje.setNombreMensaje("Original");
        mensaje.setCorrelacionExpr("expr1");
        mensaje.setIniciarNuevaInstancia(false);
        mensaje.setEliminado(false);
        mensaje = mensajeCatchRepository.save(mensaje);

        // Actualizar
        MensajeCatchRequestDTO updateRequest = MensajeCatchRequestDTO.builder()
                .procesoId(proceso.getId())
                .nombreMensaje("Actualizado")
                .correlacionExpr("expr2")
                .iniciarNuevaInstancia(true)
                .build();

        mockMvc.perform(put("/api/mensajes-catch/{id}", mensaje.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreMensaje").value("Actualizado"))
                .andExpect(jsonPath("$.correlacionExpr").value("expr2"))
                .andExpect(jsonPath("$.iniciarNuevaInstancia").value(true));
    }

    @Test
    @DisplayName("PUT /api/mensajes-catch/{id} - Fallar si no existe")
    void testActualizar_NotFound() throws Exception {
        MensajeCatchRequestDTO updateRequest = MensajeCatchRequestDTO.builder()
                .procesoId(proceso.getId())
                .nombreMensaje("Actualizado")
                .correlacionExpr("expr")
                .iniciarNuevaInstancia(false)
                .build();

        mockMvc.perform(put("/api/mensajes-catch/{id}", 9999L)
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("DELETE /api/mensajes-catch/{id} - Eliminar mensaje catch")
    void testEliminar_Success() throws Exception {
        // Crear mensaje catch
        MensajeCatch mensaje = new MensajeCatch();
        mensaje.setProceso(proceso);
        mensaje.setNombreMensaje("A Eliminar");
        mensaje.setCorrelacionExpr("expr");
        mensaje.setIniciarNuevaInstancia(false);
        mensaje.setEliminado(false);
        mensaje = mensajeCatchRepository.save(mensaje);

        mockMvc.perform(delete("/api/mensajes-catch/{id}", mensaje.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que está marcado como eliminado
        MensajeCatch eliminado = mensajeCatchRepository.findById(mensaje.getId())
                .orElse(null);
        assert eliminado != null;
        assert eliminado.isEliminado();
    }

    @Test
    @DisplayName("DELETE /api/mensajes-catch/{id} - Fallar si no existe")
    void testEliminar_NotFound() throws Exception {
        mockMvc.perform(delete("/api/mensajes-catch/{id}", 9999L)
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
