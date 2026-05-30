package com.proyecto.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.GatewayRequestDTO;
import com.proyecto.web.dto.GatewayResponseDTO;
import com.proyecto.web.entity.Gateway;
import com.proyecto.web.entity.Nodo;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.enums.TipoGateway;
import com.proyecto.web.enums.TipoNodo;
import com.proyecto.web.repository.GatewayRepository;
import com.proyecto.web.repository.NodoRepository;
import com.proyecto.web.repository.ProcesoRepository;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.GatewayService;
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
@DisplayName("GatewayController Tests")
class GatewayControllerTest {

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
    private NodoRepository nodoRepository;

    private static final String NIT_EMPRESA = "123456789";
    
    @Autowired
    private GatewayRepository gatewayRepository;
    
    private MockMvc mockMvc;
    private Proceso proceso;
    private Nodo nodo;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        setupTestData();
    }

    private void setupTestData() {
        var creado = IntegrationTestData.crearEmpresaYProceso(empresaService, procesoService, NIT_EMPRESA);
        proceso = IntegrationTestData.cargarProceso(procesoRepository, creado.getId());
        nodo = IntegrationTestData.crearNodo(
                nodoRepository, proceso, TipoNodo.GATEWAY, "Nodo Gateway", 0, 0);
    }

    @Test
    @DisplayName("POST /api/gateways - Crear gateway exitosamente")
    void testCrearGateway_Success() throws Exception {
        GatewayRequestDTO request = GatewayRequestDTO.builder()
                .nodoId(nodo.getId())
                .tipoGateway(TipoGateway.XOR)
                .build();

        mockMvc.perform(post("/api/gateways")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nodoId").value(nodo.getId()))
                .andExpect(jsonPath("$.tipoGateway").value("XOR"));
    }

    @Test
    @DisplayName("POST /api/gateways - Fallar si nodo no existe")
    void testCrearGateway_NodoNoExiste() throws Exception {
        GatewayRequestDTO request = GatewayRequestDTO.builder()
                .nodoId(9999L)
                .tipoGateway(TipoGateway.AND)
                .build();

        mockMvc.perform(post("/api/gateways")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("POST /api/gateways - Fallar si nodo ya tiene gateway")
    void testCrearGateway_NodoYaTieneGateway() throws Exception {
        // Crear primer gateway
        GatewayRequestDTO request = GatewayRequestDTO.builder()
                .nodoId(nodo.getId())
                .tipoGateway(TipoGateway.OR)
                .build();

        mockMvc.perform(post("/api/gateways")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Intentar crear otro en el mismo nodo
        mockMvc.perform(post("/api/gateways")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /api/gateways/{id} - Obtener gateway por ID")
    void testObtenerGatewayPorId_Success() throws Exception {
        // Crear gateway
        Gateway gateway = new Gateway();
        gateway.setNodo(nodo);
        gateway.setTipoGateway(TipoGateway.XOR);
        gateway.setDeleted(false);
        gateway = gatewayRepository.save(gateway);

        mockMvc.perform(get("/api/gateways/{id}", gateway.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gateway.getId()))
                .andExpect(jsonPath("$.nodoId").value(nodo.getId()));
    }

    @Test
    @DisplayName("GET /api/gateways/{id} - Fallar si gateway no existe")
    void testObtenerGatewayPorId_NotFound() throws Exception {
        mockMvc.perform(get("/api/gateways/{id}", 9999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("GET /api/gateways/proceso/{procesoId} - Obtener gateways por proceso")
    void testObtenerGatewaysPorProceso_Success() throws Exception {
        // Crear nodo adicional
        Nodo nodo2 = IntegrationTestData.crearNodo(
                nodoRepository, proceso, TipoNodo.GATEWAY, "Nodo Gateway 2", 100, 0);

        // Crear gateways
        Gateway gateway1 = new Gateway();
        gateway1.setNodo(nodo);
        gateway1.setTipoGateway(TipoGateway.AND);
        gateway1.setDeleted(false);
        gatewayRepository.save(gateway1);

        Gateway gateway2 = new Gateway();
        gateway2.setNodo(nodo2);
        gateway2.setTipoGateway(TipoGateway.OR);
        gateway2.setDeleted(false);
        gatewayRepository.save(gateway2);

        mockMvc.perform(get("/api/gateways/proceso/{procesoId}", proceso.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[1].id").exists());
    }

    @Test
    @DisplayName("GET /api/gateways/proceso/{procesoId} - Retorna vacío si no hay gateways")
    void testObtenerGatewaysPorProceso_Empty() throws Exception {
        mockMvc.perform(get("/api/gateways/proceso/{procesoId}", proceso.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/gateways/proceso/{procesoId}/tipo - Filtrar por tipo XOR")
    void testObtenerGatewaysPorProcesoYTipo_XOR() throws Exception {
        // Crear nodo adicional
        Nodo nodo2 = IntegrationTestData.crearNodo(
                nodoRepository, proceso, TipoNodo.GATEWAY, "Nodo Gateway 2", 100, 0);

        // Crear gateways de diferentes tipos
        Gateway gateway1 = new Gateway();
        gateway1.setNodo(nodo);
        gateway1.setTipoGateway(TipoGateway.XOR);
        gateway1.setDeleted(false);
        gatewayRepository.save(gateway1);

        Gateway gateway2 = new Gateway();
        gateway2.setNodo(nodo2);
        gateway2.setTipoGateway(TipoGateway.AND);
        gateway2.setDeleted(false);
        gatewayRepository.save(gateway2);

        mockMvc.perform(get("/api/gateways/proceso/{procesoId}/tipo", proceso.getId())
                .param("tipo", "XOR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoGateway").value("XOR"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/gateways/proceso/{procesoId}/tipo - Filtrar por tipo AND")
    void testObtenerGatewaysPorProcesoYTipo_AND() throws Exception {
        // Crear gateway
        Gateway gateway = new Gateway();
        gateway.setNodo(nodo);
        gateway.setTipoGateway(TipoGateway.AND);
        gateway.setDeleted(false);
        gatewayRepository.save(gateway);

        mockMvc.perform(get("/api/gateways/proceso/{procesoId}/tipo", proceso.getId())
                .param("tipo", "AND")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoGateway").value("AND"));
    }

    @Test
    @DisplayName("PUT /api/gateways/{id} - Actualizar tipo de gateway")
    void testActualizarGateway_Success() throws Exception {
        // Crear gateway inicial
        Gateway gateway = new Gateway();
        gateway.setNodo(nodo);
        gateway.setTipoGateway(TipoGateway.XOR);
        gateway.setDeleted(false);
        gateway = gatewayRepository.save(gateway);

        // Actualizar
        GatewayRequestDTO updateRequest = GatewayRequestDTO.builder()
                .nodoId(nodo.getId())
                .tipoGateway(TipoGateway.AND)
                .build();

        mockMvc.perform(put("/api/gateways/{id}", gateway.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoGateway").value("AND"));
    }

    @Test
    @DisplayName("PUT /api/gateways/{id} - Cambiar nodo de gateway")
    void testActualizarGateway_CambiarNodo() throws Exception {
        // Crear nodo nuevo
        Nodo nodoNuevo = IntegrationTestData.crearNodo(
                nodoRepository, proceso, TipoNodo.GATEWAY, "Nodo Nuevo", 150, 0);

        // Crear gateway inicial
        Gateway gateway = new Gateway();
        gateway.setNodo(nodo);
        gateway.setTipoGateway(TipoGateway.XOR);
        gateway.setDeleted(false);
        gateway = gatewayRepository.save(gateway);

        // Actualizar a nuevo nodo
        GatewayRequestDTO updateRequest = GatewayRequestDTO.builder()
                .nodoId(nodoNuevo.getId())
                .tipoGateway(TipoGateway.OR)
                .build();

        mockMvc.perform(put("/api/gateways/{id}", gateway.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodoId").value(nodoNuevo.getId()));
    }

    @Test
    @DisplayName("DELETE /api/gateways/{id} - Eliminar gateway exitosamente")
    void testEliminarGateway_Success() throws Exception {
        // Crear gateway
        Gateway gateway = new Gateway();
        gateway.setNodo(nodo);
        gateway.setTipoGateway(TipoGateway.XOR);
        gateway.setDeleted(false);
        gateway = gatewayRepository.save(gateway);

        mockMvc.perform(delete("/api/gateways/{id}", gateway.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que está marcado como eliminado
        Gateway eliminado = gatewayRepository.findByIdAndDeletedFalse(gateway.getId()).orElse(null);
        assert eliminado == null;
    }

    @Test
    @DisplayName("DELETE /api/gateways/{id} - Fallar si gateway no existe")
    void testEliminarGateway_NotFound() throws Exception {
        mockMvc.perform(delete("/api/gateways/{id}", 9999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }
}
