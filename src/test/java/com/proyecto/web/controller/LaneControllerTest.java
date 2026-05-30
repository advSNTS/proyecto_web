package com.proyecto.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.LaneRequestDTO;
import com.proyecto.web.dto.LaneResponseDTO;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.entity.Lane;
import com.proyecto.web.entity.Pool;
import com.proyecto.web.entity.Rol;
import com.proyecto.web.repository.EmpresaRepository;
import com.proyecto.web.repository.LaneRepository;
import com.proyecto.web.repository.PoolRepository;
import com.proyecto.web.repository.RolRepository;
import com.proyecto.web.service.LaneService;
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
@DisplayName("LaneController Tests")
class LaneControllerTest {

    @Autowired
    private WebApplicationContext context;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private EmpresaRepository empresaRepository;
    
    @Autowired
    private PoolRepository poolRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private LaneRepository laneRepository;
    
    private MockMvc mockMvc;
    private Empresa empresa;
    private Pool pool;
    private Rol rol;
    private String nitEmpresa = "987654321";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        setupTestData();
    }

    private void setupTestData() {
        // Crear empresa
        empresa = new Empresa();
        empresa.setNit(nitEmpresa);
        empresa.setNombre("Empresa Test");
        empresa.setDeleted(false);
        empresaRepository.save(empresa);

        // Crear rol
        rol = new Rol();
        rol.setNombre("Rol Test");
        rol.setEmpresa(empresa);
        rol.setDeleted(false);
        rolRepository.save(rol);

        // Crear pool
        pool = new Pool();
        pool.setNombre("Pool Test");
        pool.setEmpresa(empresa);
        pool.setEliminado(false);
        poolRepository.save(pool);
    }

    @Test
    @DisplayName("POST /api/lanes - Crear lane exitosamente")
    void testCrearLane_Success() throws Exception {
        LaneRequestDTO request = LaneRequestDTO.builder()
                .poolId(pool.getId())
                .nombre("Lane Test")
                .rolProcesoId(rol.getId())
                .build();

        mockMvc.perform(post("/api/lanes")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Lane Test"))
                .andExpect(jsonPath("$.poolId").value(pool.getId()));
    }

    @Test
    @DisplayName("POST /api/lanes - Fallar si nit es nulo")
    void testCrearLane_NitNulo() throws Exception {
        LaneRequestDTO request = LaneRequestDTO.builder()
                .poolId(pool.getId())
                .nombre("Lane Test")
                .rolProcesoId(rol.getId())
                .build();

        mockMvc.perform(post("/api/lanes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/lanes - Fallar si pool no existe")
    void testCrearLane_PoolNoExiste() throws Exception {
        LaneRequestDTO request = LaneRequestDTO.builder()
                .poolId(9999L)
                .nombre("Lane Test")
                .rolProcesoId(rol.getId())
                .build();

        mockMvc.perform(post("/api/lanes")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/lanes - Fallar si rol no existe")
    void testCrearLane_RolNoExiste() throws Exception {
        LaneRequestDTO request = LaneRequestDTO.builder()
                .poolId(pool.getId())
                .nombre("Lane Test")
                .rolProcesoId(9999L)
                .build();

        mockMvc.perform(post("/api/lanes")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /api/lanes - Listar lanes por empresa")
    void testListarLanes_Success() throws Exception {
        // Crear lane
        Lane lane = new Lane();
        lane.setPool(pool);
        lane.setNombre("Lane 1");
        lane.setRolProceso(rol);
        lane.setEliminado(false);
        laneRepository.save(lane);

        mockMvc.perform(get("/api/lanes")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].nombre").value("Lane 1"));
    }

    @Test
    @DisplayName("GET /api/lanes - Retorna lista vacía si no hay lanes")
    void testListarLanes_Empty() throws Exception {
        mockMvc.perform(get("/api/lanes")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/lanes - Fallar si nit es nulo")
    void testListarLanes_NitNulo() throws Exception {
        mockMvc.perform(get("/api/lanes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /api/lanes/pool/{poolId} - Listar lanes por pool")
    void testListarLanesPorPool_Success() throws Exception {
        // Crear lane
        Lane lane = new Lane();
        lane.setPool(pool);
        lane.setNombre("Lane Pool 1");
        lane.setRolProceso(rol);
        lane.setEliminado(false);
        laneRepository.save(lane);

        mockMvc.perform(get("/api/lanes/pool/{poolId}", pool.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].poolId").value(pool.getId()));
    }

    @Test
    @DisplayName("GET /api/lanes/pool/{poolId} - Fallar si pool no existe")
    void testListarLanesPorPool_PoolNoExiste() throws Exception {
        mockMvc.perform(get("/api/lanes/pool/{poolId}", 9999L)
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /api/lanes/{id} - Obtener lane por ID")
    void testObtenerLane_Success() throws Exception {
        // Crear lane
        Lane lane = new Lane();
        lane.setPool(pool);
        lane.setNombre("Lane Específico");
        lane.setRolProceso(rol);
        lane.setEliminado(false);
        lane = laneRepository.save(lane);

        mockMvc.perform(get("/api/lanes/{id}", lane.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lane.getId()))
                .andExpect(jsonPath("$.nombre").value("Lane Específico"));
    }

    @Test
    @DisplayName("GET /api/lanes/{id} - Fallar si lane no existe")
    void testObtenerLane_NotFound() throws Exception {
        mockMvc.perform(get("/api/lanes/{id}", 9999L)
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("PUT /api/lanes/{id} - Actualizar lane exitosamente")
    void testActualizarLane_Success() throws Exception {
        // Crear lane inicial
        Lane lane = new Lane();
        lane.setPool(pool);
        lane.setNombre("Lane Original");
        lane.setRolProceso(rol);
        lane.setEliminado(false);
        lane = laneRepository.save(lane);

        // Actualizar
        LaneRequestDTO updateRequest = LaneRequestDTO.builder()
                .poolId(pool.getId())
                .nombre("Lane Actualizado")
                .rolProcesoId(rol.getId())
                .build();

        mockMvc.perform(put("/api/lanes/{id}", lane.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Lane Actualizado"));
    }

    @Test
    @DisplayName("PUT /api/lanes/{id} - Fallar si lane no existe")
    void testActualizarLane_NotFound() throws Exception {
        LaneRequestDTO updateRequest = LaneRequestDTO.builder()
                .poolId(pool.getId())
                .nombre("Lane Actualizado")
                .rolProcesoId(rol.getId())
                .build();

        mockMvc.perform(put("/api/lanes/{id}", 9999L)
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("DELETE /api/lanes/{id} - Eliminar lane exitosamente")
    void testEliminarLane_Success() throws Exception {
        // Crear lane
        Lane lane = new Lane();
        lane.setPool(pool);
        lane.setNombre("Lane a Eliminar");
        lane.setRolProceso(rol);
        lane.setEliminado(false);
        lane = laneRepository.save(lane);

        mockMvc.perform(delete("/api/lanes/{id}", lane.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que está marcado como eliminado
        Lane eliminada = laneRepository.findByIdAndPool_Empresa_NitAndEliminadoFalse(lane.getId(), nitEmpresa).orElse(null);
        assert eliminada == null;
    }

    @Test
    @DisplayName("DELETE /api/lanes/{id} - Fallar si lane no existe")
    void testEliminarLane_NotFound() throws Exception {
        mockMvc.perform(delete("/api/lanes/{id}", 9999L)
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
