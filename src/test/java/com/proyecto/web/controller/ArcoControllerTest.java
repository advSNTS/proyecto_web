package com.proyecto.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.ArcoRequestDTO;
import com.proyecto.web.dto.ArcoResponseDTO;
import com.proyecto.web.entity.Arco;
import com.proyecto.web.entity.Nodo;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.enums.TipoNodo;
import com.proyecto.web.repository.ArcoRepository;
import com.proyecto.web.repository.NodoRepository;
import com.proyecto.web.repository.ProcesoRepository;
import com.proyecto.web.service.ArcoService;
import com.proyecto.web.service.EmpresaService;
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
@DisplayName("ArcoController Tests")
class ArcoControllerTest {

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
    
    @Autowired
    private ArcoRepository arcoRepository;
    
    @Autowired
    private ArcoService arcoService;
    
    private MockMvc mockMvc;
    private Proceso proceso;
    private Nodo nodoOrigen;
    private Nodo nodoDestino;
    private String nitEmpresa = "123456789";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        setupTestData();
    }

    private void setupTestData() {
        var creado = IntegrationTestData.crearEmpresaYProceso(empresaService, procesoService, nitEmpresa);
        proceso = IntegrationTestData.cargarProceso(procesoRepository, creado.getId());
        nodoOrigen = IntegrationTestData.crearNodo(
                nodoRepository, proceso, TipoNodo.ACTIVIDAD, "Nodo Origen", 0, 0);
        nodoDestino = IntegrationTestData.crearNodo(
                nodoRepository, proceso, TipoNodo.ACTIVIDAD, "Nodo Destino", 100, 0);
    }

    @Test
    @DisplayName("POST /api/arcos - Crear arco exitosamente")
    void testCrearArco_Success() throws Exception {
        ArcoRequestDTO request = ArcoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .idProceso(proceso.getId())
                .nodoOrigenId(nodoOrigen.getId())
                .nodoDestinoId(nodoDestino.getId())
                .build();

        mockMvc.perform(post("/api/arcos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nodoOrigenId").value(nodoOrigen.getId()))
                .andExpect(jsonPath("$.nodoDestinoId").value(nodoDestino.getId()));
    }

    @Test
    @DisplayName("POST /api/arcos - Fallar si nit es nulo")
    void testCrearArco_NitNulo() throws Exception {
        ArcoRequestDTO request = ArcoRequestDTO.builder()
                .nitEmpresa(null)
                .idProceso(proceso.getId())
                .nodoOrigenId(nodoOrigen.getId())
                .nodoDestinoId(nodoDestino.getId())
                .build();

        mockMvc.perform(post("/api/arcos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/arcos - Fallar si nodos son iguales")
    void testCrearArco_NodosIguales() throws Exception {
        ArcoRequestDTO request = ArcoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .idProceso(proceso.getId())
                .nodoOrigenId(nodoOrigen.getId())
                .nodoDestinoId(nodoOrigen.getId())
                .build();

        mockMvc.perform(post("/api/arcos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/arcos - Fallar si arco ya existe")
    void testCrearArco_ArcoYaExiste() throws Exception {
        // Crear primer arco
        ArcoRequestDTO request = ArcoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .idProceso(proceso.getId())
                .nodoOrigenId(nodoOrigen.getId())
                .nodoDestinoId(nodoDestino.getId())
                .build();

        mockMvc.perform(post("/api/arcos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Intentar crear el mismo arco
        mockMvc.perform(post("/api/arcos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /api/arcos/{id} - Obtener arco por ID")
    void testObtenerArcoPorId_Success() throws Exception {
        // Crear arco
        Arco arco = new Arco();
        arco.setProceso(proceso);
        arco.setNodoOrigen(nodoOrigen);
        arco.setNodoDestino(nodoDestino);
        arco.setEliminado(false);
        arco = arcoRepository.save(arco);

        mockMvc.perform(get("/api/arcos/{id}", arco.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(arco.getId()))
                .andExpect(jsonPath("$.nodoOrigenId").value(nodoOrigen.getId()))
                .andExpect(jsonPath("$.nodoDestinoId").value(nodoDestino.getId()));
    }

    @Test
    @DisplayName("GET /api/arcos/{id} - Fallar si arco no existe")
    void testObtenerArcoPorId_NotFound() throws Exception {
        mockMvc.perform(get("/api/arcos/{id}", 9999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/arcos/proceso/{idProceso} - Obtener arcos por proceso")
    void testObtenerArcosPorProceso_Success() throws Exception {
        // Crear varios arcos
        Nodo nodo3 = IntegrationTestData.crearNodo(
                nodoRepository, proceso, TipoNodo.ACTIVIDAD, "Nodo Tercero", 50, 50);

        Arco arco1 = new Arco();
        arco1.setProceso(proceso);
        arco1.setNodoOrigen(nodoOrigen);
        arco1.setNodoDestino(nodoDestino);
        arco1.setEliminado(false);
        arcoRepository.save(arco1);

        Arco arco2 = new Arco();
        arco2.setProceso(proceso);
        arco2.setNodoOrigen(nodoDestino);
        arco2.setNodoDestino(nodo3);
        arco2.setEliminado(false);
        arcoRepository.save(arco2);

        mockMvc.perform(get("/api/arcos/proceso/{idProceso}", proceso.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[1].id").exists());
    }

    @Test
    @DisplayName("GET /api/arcos/origen/{nodoId} - Obtener arcos salientes")
    void testObtenerArcosSalientes_Success() throws Exception {
        // Crear arco
        Arco arco = new Arco();
        arco.setProceso(proceso);
        arco.setNodoOrigen(nodoOrigen);
        arco.setNodoDestino(nodoDestino);
        arco.setEliminado(false);
        arcoRepository.save(arco);

        mockMvc.perform(get("/api/arcos/origen/{nodoId}", nodoOrigen.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nodoOrigenId").value(nodoOrigen.getId()));
    }

    @Test
    @DisplayName("GET /api/arcos/origen/{nodoId} - Retorna lista vacía si no hay arcos")
    void testObtenerArcosSalientes_Empty() throws Exception {
        mockMvc.perform(get("/api/arcos/origen/{nodoId}", nodoOrigen.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/arcos/destino/{nodoId} - Obtener arcos entrantes")
    void testObtenerArcosEntrantes_Success() throws Exception {
        // Crear arco
        Arco arco = new Arco();
        arco.setProceso(proceso);
        arco.setNodoOrigen(nodoOrigen);
        arco.setNodoDestino(nodoDestino);
        arco.setEliminado(false);
        arcoRepository.save(arco);

        mockMvc.perform(get("/api/arcos/destino/{nodoId}", nodoDestino.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nodoDestinoId").value(nodoDestino.getId()));
    }

    @Test
    @DisplayName("GET /api/arcos/destino/{nodoId} - Retorna lista vacía si no hay arcos")
    void testObtenerArcosEntrantes_Empty() throws Exception {
        mockMvc.perform(get("/api/arcos/destino/{nodoId}", nodoDestino.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("PUT /api/arcos/{id} - Actualizar arco exitosamente")
    void testActualizarArco_Success() throws Exception {
        // Crear arco inicial
        Arco arco = new Arco();
        arco.setProceso(proceso);
        arco.setNodoOrigen(nodoOrigen);
        arco.setNodoDestino(nodoDestino);
        arco.setEliminado(false);
        arco = arcoRepository.save(arco);

        // Crear nuevo nodo destino
        Nodo nuevoDestino = IntegrationTestData.crearNodo(
                nodoRepository, proceso, TipoNodo.ACTIVIDAD, "Nuevo Destino", 200, 0);

        // Actualizar
        ArcoRequestDTO updateRequest = ArcoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .idProceso(proceso.getId())
                .nodoOrigenId(nodoOrigen.getId())
                .nodoDestinoId(nuevoDestino.getId())
                .build();

        mockMvc.perform(put("/api/arcos/{id}", arco.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodoDestinoId").value(nuevoDestino.getId()));
    }

    @Test
    @DisplayName("PUT /api/arcos/{id} - Fallar si actualiza a nodos iguales")
    void testActualizarArco_NodosIguales() throws Exception {
        // Crear arco
        Arco arco = new Arco();
        arco.setProceso(proceso);
        arco.setNodoOrigen(nodoOrigen);
        arco.setNodoDestino(nodoDestino);
        arco.setEliminado(false);
        arco = arcoRepository.save(arco);

        // Intentar actualizar con nodos iguales
        ArcoRequestDTO updateRequest = ArcoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .idProceso(proceso.getId())
                .nodoOrigenId(nodoOrigen.getId())
                .nodoDestinoId(nodoOrigen.getId())
                .build();

        mockMvc.perform(put("/api/arcos/{id}", arco.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("DELETE /api/arcos/{id} - Eliminar arco exitosamente")
    void testEliminarArco_Success() throws Exception {
        // Crear arco
        Arco arco = new Arco();
        arco.setProceso(proceso);
        arco.setNodoOrigen(nodoOrigen);
        arco.setNodoDestino(nodoDestino);
        arco.setEliminado(false);
        arco = arcoRepository.save(arco);

        mockMvc.perform(delete("/api/arcos/{id}", arco.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que está marcado como eliminado (soft delete)
        Arco eliminado = arcoRepository.findById(arco.getId()).orElse(null);
        assert eliminado != null;
        assert eliminado.isEliminado();
    }

    @Test
    @DisplayName("DELETE /api/arcos/{id} - Fallar si arco no existe")
    void testEliminarArco_NotFound() throws Exception {
        mockMvc.perform(delete("/api/arcos/{id}", 9999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
