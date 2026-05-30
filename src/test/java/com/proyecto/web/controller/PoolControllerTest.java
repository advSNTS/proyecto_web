package com.proyecto.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.PoolRequestDTO;
import com.proyecto.web.dto.PoolResponseDTO;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.entity.Pool;
import com.proyecto.web.repository.EmpresaRepository;
import com.proyecto.web.repository.PoolRepository;
import com.proyecto.web.service.PoolService;
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
@DisplayName("PoolController Tests")
class PoolControllerTest {

    @Autowired
    private WebApplicationContext context;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private EmpresaRepository empresaRepository;
    
    @Autowired
    private PoolRepository poolRepository;
    
    private MockMvc mockMvc;
    private Empresa empresa;
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
    }

    @Test
    @DisplayName("GET /api/pools - Listar pools por empresa")
    void testListarPools_Success() throws Exception {
        // Crear varios pools
        Pool pool1 = new Pool();
        pool1.setEmpresa(empresa);
        pool1.setNombre("Pool 1");
        pool1.setDescripcion("Descripción 1");
        pool1.setEsDefault(true);
        pool1.setEliminado(false);
        poolRepository.save(pool1);

        Pool pool2 = new Pool();
        pool2.setEmpresa(empresa);
        pool2.setNombre("Pool 2");
        pool2.setDescripcion("Descripción 2");
        pool2.setEsDefault(false);
        pool2.setEliminado(false);
        poolRepository.save(pool2);

        mockMvc.perform(get("/api/pools")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").exists())
                .andExpect(jsonPath("$[1].nombre").exists())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/pools - Retorna lista vacía si no hay pools")
    void testListarPools_Empty() throws Exception {
        mockMvc.perform(get("/api/pools")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/pools - Fallar si nit es nulo")
    void testListarPools_NitNulo() throws Exception {
        mockMvc.perform(get("/api/pools")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /api/pools/{id} - Obtener pool por ID")
    void testObtenerPool_Success() throws Exception {
        // Crear pool
        Pool pool = new Pool();
        pool.setEmpresa(empresa);
        pool.setNombre("Pool Específico");
        pool.setDescripcion("Descripción específica");
        pool.setEsDefault(false);
        pool.setEliminado(false);
        pool = poolRepository.save(pool);

        mockMvc.perform(get("/api/pools/{id}", pool.getId())
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pool.getId()))
                .andExpect(jsonPath("$.nombre").value("Pool Específico"));
    }

    @Test
    @DisplayName("GET /api/pools/{id} - Fallar si pool no existe")
    void testObtenerPool_NotFound() throws Exception {
        mockMvc.perform(get("/api/pools/{id}", 9999L)
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/pools - Crear pool exitosamente")
    void testCrearPool_Success() throws Exception {
        PoolRequestDTO request = PoolRequestDTO.builder()
                .nombre("Pool Nuevo")
                .descripcion("Descripción nueva")
                .esDefault(false)
                .build();

        mockMvc.perform(post("/api/pools")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Pool Nuevo"))
                .andExpect(jsonPath("$.esDefault").value(false));
    }

    @Test
    @DisplayName("POST /api/pools - Crear pool como default")
    void testCrearPool_AsDefault() throws Exception {
        PoolRequestDTO request = PoolRequestDTO.builder()
                .nombre("Pool Default")
                .descripcion("Pool por defecto")
                .esDefault(true)
                .build();

        mockMvc.perform(post("/api/pools")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esDefault").value(true));
    }

    @Test
    @DisplayName("POST /api/pools - Fallar si nombre es nulo")
    void testCrearPool_NombreNulo() throws Exception {
        PoolRequestDTO request = PoolRequestDTO.builder()
                .nombre(null)
                .descripcion("Descripción")
                .esDefault(false)
                .build();

        mockMvc.perform(post("/api/pools")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/pools - Fallar si nombre está vacío")
    void testCrearPool_NombreVacio() throws Exception {
        PoolRequestDTO request = PoolRequestDTO.builder()
                .nombre("   ")
                .descripcion("Descripción")
                .esDefault(false)
                .build();

        mockMvc.perform(post("/api/pools")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/pools - Fallar si nit es nulo")
    void testCrearPool_NitNulo() throws Exception {
        PoolRequestDTO request = PoolRequestDTO.builder()
                .nombre("Pool Nuevo")
                .descripcion("Descripción")
                .esDefault(false)
                .build();

        mockMvc.perform(post("/api/pools")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/pools - Fallar si ya existe pool con ese nombre")
    void testCrearPool_NombreDuplicado() throws Exception {
        // Crear primer pool
        PoolRequestDTO request1 = PoolRequestDTO.builder()
                .nombre("Pool Original")
                .descripcion("Descripción 1")
                .esDefault(false)
                .build();

        mockMvc.perform(post("/api/pools")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        // Intentar crear otro con mismo nombre
        PoolRequestDTO request2 = PoolRequestDTO.builder()
                .nombre("Pool Original")
                .descripcion("Descripción 2")
                .esDefault(false)
                .build();

        mockMvc.perform(post("/api/pools")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/pools - Cambiar default anterior cuando se crea nuevo default")
    void testCrearPool_CambiarDefault() throws Exception {
        // Crear primer pool default
        PoolRequestDTO request1 = PoolRequestDTO.builder()
                .nombre("Pool Default 1")
                .descripcion("Primer default")
                .esDefault(true)
                .build();

        mockMvc.perform(post("/api/pools")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        // Crear segundo pool como default
        PoolRequestDTO request2 = PoolRequestDTO.builder()
                .nombre("Pool Default 2")
                .descripcion("Segundo default")
                .esDefault(true)
                .build();

        mockMvc.perform(post("/api/pools")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk());

        // Verificar que solo el segundo es default
        mockMvc.perform(get("/api/pools")
                .param("nitEmpresa", nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.esDefault == true)].nombre").value("Pool Default 2"));
    }
}
