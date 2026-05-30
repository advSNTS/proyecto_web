package com.proyecto.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.MensajeThrowRequestDTO;
import com.proyecto.web.dto.MensajeThrowResponseDTO;
import com.proyecto.web.entity.MensajeThrow;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.repository.MensajeThrowRepository;
import com.proyecto.web.repository.ProcesoRepository;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.MensajeThrowService;
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
@DisplayName("MensajeThrowController Tests")
class MensajeThrowControllerTest {

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
    private MensajeThrowRepository mensajeThrowRepository;
    
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
    @DisplayName("POST /api/mensajes-throw - Crear mensaje throw exitosamente")
    void testCrearMensajeThrow_Success() throws Exception {
        MensajeThrowRequestDTO request = MensajeThrowRequestDTO.builder()
                .procesoId(proceso.getId())
                .nombreMensaje("Mensaje Throw 1")
                .payloadTemplate("{\"data\": \"value\"}")
                .build();

        mockMvc.perform(post("/api/mensajes-throw")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombreMensaje").value("Mensaje Throw 1"))
                .andExpect(jsonPath("$.payloadTemplate").value("{\"data\": \"value\"}"));
    }

    @Test
    @DisplayName("POST /api/mensajes-throw - Fallar si nit es nulo")
    void testCrearMensajeThrow_NitNulo() throws Exception {
        MensajeThrowRequestDTO request = MensajeThrowRequestDTO.builder()
                .procesoId(proceso.getId())
                .nombreMensaje("Mensaje Throw 1")
                .payloadTemplate("{\"data\": \"value\"}")
                .build();

        mockMvc.perform(post("/api/mensajes-throw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/mensajes-throw - Fallar si proceso no existe")
    void testCrearMensajeThrow_ProcesoNoExiste() throws Exception {
        MensajeThrowRequestDTO request = MensajeThrowRequestDTO.builder()
                .procesoId(9999L)
                .nombreMensaje("Mensaje Throw 1")
                .payloadTemplate("{\"data\": \"value\"}")
                .build();

        mockMvc.perform(post("/api/mensajes-throw")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/mensajes-throw/proceso/{procesoId} - Listar por proceso")
    void testListarPorProceso_Success() throws Exception {
        // Crear mensaje throw
        MensajeThrow mensaje = new MensajeThrow();
        mensaje.setProceso(proceso);
        mensaje.setNombreMensaje("Mensaje Proceso");
        mensaje.setPayloadTemplate("{\"data\": \"template\"}");
        mensaje.setEliminado(false);
        mensajeThrowRepository.save(mensaje);

        mockMvc.perform(get("/api/mensajes-throw/proceso/{procesoId}", proceso.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreMensaje").value("Mensaje Proceso"));
    }

    @Test
    @DisplayName("GET /api/mensajes-throw/proceso/{procesoId} - Retorna vacío si no hay mensajes")
    void testListarPorProceso_Empty() throws Exception {
        mockMvc.perform(get("/api/mensajes-throw/proceso/{procesoId}", proceso.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/mensajes-throw/{id} - Obtener por ID")
    void testObtener_Success() throws Exception {
        // Crear mensaje throw
        MensajeThrow mensaje = new MensajeThrow();
        mensaje.setProceso(proceso);
        mensaje.setNombreMensaje("Mensaje Específico");
        mensaje.setPayloadTemplate("{\"data\": \"specific\"}");
        mensaje.setEliminado(false);
        mensaje = mensajeThrowRepository.save(mensaje);

        mockMvc.perform(get("/api/mensajes-throw/{id}", mensaje.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mensaje.getId()))
                .andExpect(jsonPath("$.nombreMensaje").value("Mensaje Específico"));
    }

    @Test
    @DisplayName("GET /api/mensajes-throw/{id} - Fallar si no existe")
    void testObtener_NotFound() throws Exception {
        mockMvc.perform(get("/api/mensajes-throw/{id}", 9999L)
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("PUT /api/mensajes-throw/{id} - Actualizar mensaje throw")
    void testActualizar_Success() throws Exception {
        // Crear mensaje throw inicial
        MensajeThrow mensaje = new MensajeThrow();
        mensaje.setProceso(proceso);
        mensaje.setNombreMensaje("Original");
        mensaje.setPayloadTemplate("{\"data\": \"original\"}");
        mensaje.setEliminado(false);
        mensaje = mensajeThrowRepository.save(mensaje);

        // Actualizar
        MensajeThrowRequestDTO updateRequest = MensajeThrowRequestDTO.builder()
                .procesoId(proceso.getId())
                .nombreMensaje("Actualizado")
                .payloadTemplate("{\"data\": \"actualizado\"}")
                .build();

        mockMvc.perform(put("/api/mensajes-throw/{id}", mensaje.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreMensaje").value("Actualizado"))
                .andExpect(jsonPath("$.payloadTemplate").value("{\"data\": \"actualizado\"}"));
    }

    @Test
    @DisplayName("PUT /api/mensajes-throw/{id} - Fallar si no existe")
    void testActualizar_NotFound() throws Exception {
        MensajeThrowRequestDTO updateRequest = MensajeThrowRequestDTO.builder()
                .procesoId(proceso.getId())
                .nombreMensaje("Actualizado")
                .payloadTemplate("{\"data\": \"test\"}")
                .build();

        mockMvc.perform(put("/api/mensajes-throw/{id}", 9999L)
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("DELETE /api/mensajes-throw/{id} - Eliminar mensaje throw")
    void testEliminar_Success() throws Exception {
        // Crear mensaje throw
        MensajeThrow mensaje = new MensajeThrow();
        mensaje.setProceso(proceso);
        mensaje.setNombreMensaje("A Eliminar");
        mensaje.setPayloadTemplate("{\"data\": \"delete\"}");
        mensaje.setEliminado(false);
        mensaje = mensajeThrowRepository.save(mensaje);

        mockMvc.perform(delete("/api/mensajes-throw/{id}", mensaje.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que está marcado como eliminado
        MensajeThrow eliminado = mensajeThrowRepository.findById(mensaje.getId())
                .orElse(null);
        assert eliminado != null;
        assert eliminado.isEliminado();
    }

    @Test
    @DisplayName("DELETE /api/mensajes-throw/{id} - Fallar si no existe")
    void testEliminar_NotFound() throws Exception {
        mockMvc.perform(delete("/api/mensajes-throw/{id}", 9999L)
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
