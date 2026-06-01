package com.proyecto.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.TareaIntegracionRequestDTO;
import com.proyecto.web.entity.MensajeExterno;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.entity.TareaIntegracion;
import com.proyecto.web.enums.TipoDestinoMensajeExterno;
import com.proyecto.web.repository.MensajeExternoRepository;
import com.proyecto.web.repository.ProcesoRepository;
import com.proyecto.web.repository.TareaIntegracionRepository;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.ProcesoService;
import com.proyecto.web.support.IntegrationTestData;
import com.proyecto.web.support.TestSecurityContext;
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
@DisplayName("TareaIntegracionController Tests")
class TareaIntegracionControllerTest {

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
    private MensajeExternoRepository mensajeExternoRepository;
    
    @Autowired
    private TareaIntegracionRepository tareaIntegracionRepository;
    
    private MockMvc mockMvc;
    private Proceso proceso;
    private MensajeExterno mensajeExterno;
    private String nitEmpresa = "123456789";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        setupTestData();
    }

    private void setupTestData() {
        var creado = IntegrationTestData.crearEmpresaYProceso(empresaService, procesoService, nitEmpresa);
        proceso = IntegrationTestData.cargarProceso(procesoRepository, creado.getId());

        // Crear mensaje externo
        mensajeExterno = new MensajeExterno();
        mensajeExterno.setDestinoTipo(TipoDestinoMensajeExterno.HTTP);
        mensajeExterno.setConfiguracion("{\"servidor\": \"smtp.example.com\"}");
        mensajeExterno.setCredenciales("{\"usuario\": \"test@example.com\"}");
        mensajeExterno.setEliminado(false);
        mensajeExternoRepository.save(mensajeExterno);
    }

    @Test
    @DisplayName("POST /api/tareas-integracion - Crear tarea integracion exitosamente")
    void testCrearTareaIntegracion_Success() throws Exception {
        TareaIntegracionRequestDTO request = TareaIntegracionRequestDTO.builder()
                .procesoId(proceso.getId())
                .mensajeExternoId(mensajeExterno.getId())
                .payloadMapping("{\"data\": \"mapping\"}")
                .build();

        mockMvc.perform(post("/api/tareas-integracion")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.procesoId").value(proceso.getId()))
                .andExpect(jsonPath("$.mensajeExternoId").value(mensajeExterno.getId()));
    }

    @Test
    @DisplayName("POST /api/tareas-integracion - Fallar sin autenticación")
    void testCrearTareaIntegracion_SinAutenticacion() throws Exception {
        TestSecurityContext.clear();
        
        TareaIntegracionRequestDTO request = TareaIntegracionRequestDTO.builder()
                .procesoId(proceso.getId())
                .mensajeExternoId(mensajeExterno.getId())
                .payloadMapping("{\"data\": \"mapping\"}")
                .build();

        mockMvc.perform(post("/api/tareas-integracion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/tareas-integracion - Fallar si proceso no existe")
    void testCrearTareaIntegracion_ProcesoNoExiste() throws Exception {
        TareaIntegracionRequestDTO request = TareaIntegracionRequestDTO.builder()
                .procesoId(9999L)
                .mensajeExternoId(mensajeExterno.getId())
                .payloadMapping("{\"data\": \"mapping\"}")
                .build();

        mockMvc.perform(post("/api/tareas-integracion")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/tareas-integracion - Fallar si mensaje externo no existe")
    void testCrearTareaIntegracion_MensajeExternoNoExiste() throws Exception {
        TareaIntegracionRequestDTO request = TareaIntegracionRequestDTO.builder()
                .procesoId(proceso.getId())
                .mensajeExternoId(9999L)
                .payloadMapping("{\"data\": \"mapping\"}")
                .build();

        mockMvc.perform(post("/api/tareas-integracion")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /api/tareas-integracion/proceso/{procesoId} - Listar por proceso")
    void testListarPorProceso_Success() throws Exception {
        // Crear tarea integracion
        TareaIntegracion tarea = new TareaIntegracion();
        tarea.setProceso(proceso);
        tarea.setMensajeExterno(mensajeExterno);
        tarea.setPayloadMapping("{\"data\": \"test\"}");
        tarea.setEliminado(false);
        tareaIntegracionRepository.save(tarea);

        mockMvc.perform(get("/api/tareas-integracion/proceso/{procesoId}", proceso.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].procesoId").value(proceso.getId()));
    }

    @Test
    @DisplayName("GET /api/tareas-integracion/proceso/{procesoId} - Retorna vacío si no hay tareas")
    void testListarPorProceso_Empty() throws Exception {
        mockMvc.perform(get("/api/tareas-integracion/proceso/{procesoId}", proceso.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/tareas-integracion/{id} - Obtener por ID")
    void testObtener_Success() throws Exception {
        // Crear tarea integracion
        TareaIntegracion tarea = new TareaIntegracion();
        tarea.setProceso(proceso);
        tarea.setMensajeExterno(mensajeExterno);
        tarea.setPayloadMapping("{\"data\": \"specific\"}");
        tarea.setEliminado(false);
        tarea = tareaIntegracionRepository.save(tarea);

        mockMvc.perform(get("/api/tareas-integracion/{id}", tarea.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tarea.getId()))
                .andExpect(jsonPath("$.mensajeExternoId").value(mensajeExterno.getId()));
    }

    @Test
    @DisplayName("GET /api/tareas-integracion/{id} - Fallar si no existe")
    void testObtener_NotFound() throws Exception {
        mockMvc.perform(get("/api/tareas-integracion/{id}", 9999L)
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("PUT /api/tareas-integracion/{id} - Actualizar tarea integracion")
    void testActualizar_Success() throws Exception {
        // Crear tarea inicial
        TareaIntegracion tarea = new TareaIntegracion();
        tarea.setProceso(proceso);
        tarea.setMensajeExterno(mensajeExterno);
        tarea.setPayloadMapping("{\"data\": \"original\"}");
        tarea.setEliminado(false);
        tarea = tareaIntegracionRepository.save(tarea);

        // Crear nuevo mensaje externo
        MensajeExterno mensajeExterno2 = new MensajeExterno();
        mensajeExterno2.setDestinoTipo(TipoDestinoMensajeExterno.COLA);
        mensajeExterno2.setConfiguracion("{\"api\": \"sms\"}");
        mensajeExterno2.setCredenciales("{\"key\": \"value\"}");
        mensajeExterno2.setEliminado(false);
        mensajeExternoRepository.save(mensajeExterno2);

        // Actualizar
        TareaIntegracionRequestDTO updateRequest = TareaIntegracionRequestDTO.builder()
                .procesoId(proceso.getId())
                .mensajeExternoId(mensajeExterno2.getId())
                .payloadMapping("{\"data\": \"actualizado\"}")
                .build();

        mockMvc.perform(put("/api/tareas-integracion/{id}", tarea.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensajeExternoId").value(mensajeExterno2.getId()))
                .andExpect(jsonPath("$.payloadMapping").value("{\"data\": \"actualizado\"}"));
    }

    @Test
    @DisplayName("PUT /api/tareas-integracion/{id} - Fallar si no existe")
    void testActualizar_NotFound() throws Exception {
        TareaIntegracionRequestDTO updateRequest = TareaIntegracionRequestDTO.builder()
                .procesoId(proceso.getId())
                .mensajeExternoId(mensajeExterno.getId())
                .payloadMapping("{\"data\": \"test\"}")
                .build();

        mockMvc.perform(put("/api/tareas-integracion/{id}", 9999L)
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("DELETE /api/tareas-integracion/{id} - Eliminar tarea integracion")
    void testEliminar_Success() throws Exception {
        // Crear tarea
        TareaIntegracion tarea = new TareaIntegracion();
        tarea.setProceso(proceso);
        tarea.setMensajeExterno(mensajeExterno);
        tarea.setPayloadMapping("{\"data\": \"delete\"}");
        tarea.setEliminado(false);
        tarea = tareaIntegracionRepository.save(tarea);

        mockMvc.perform(delete("/api/tareas-integracion/{id}", tarea.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que está marcado como eliminado
        TareaIntegracion eliminada = tareaIntegracionRepository.findById(tarea.getId())
                .orElse(null);
        assert eliminada != null;
        assert eliminada.isEliminado();
    }

    @Test
    @DisplayName("DELETE /api/tareas-integracion/{id} - Fallar si no existe")
    void testEliminar_NotFound() throws Exception {
        mockMvc.perform(delete("/api/tareas-integracion/{id}", 9999L)
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
