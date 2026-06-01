package com.proyecto.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.ActividadRequestDTO;
import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.EmpresaXProcesoRequestDTO;
import com.proyecto.web.dto.NodoRequestDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.RequiereRequestDTO;
import com.proyecto.web.dto.RolRequestDTO;
import com.proyecto.web.entity.Actividad;
import com.proyecto.web.entity.Nodo;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.entity.Requiere;
import com.proyecto.web.entity.Rol;
import com.proyecto.web.enums.Permiso;
import com.proyecto.web.enums.TipoNodo;
import com.proyecto.web.repository.ActividadRepository;
import com.proyecto.web.repository.NodoRepository;
import com.proyecto.web.repository.ProcesoRepository;
import com.proyecto.web.repository.RequiereRepository;
import com.proyecto.web.repository.RolRepository;
import com.proyecto.web.service.ActividadService;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.EmpresaXProcesoService;
import com.proyecto.web.service.NodoService;
import com.proyecto.web.service.ProcesoService;
import com.proyecto.web.service.RolService;
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
@DisplayName("RequiereController Tests")
class RequiereControllerTest {

    @Autowired
    private WebApplicationContext context;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private ProcesoService procesoService;

    @Autowired
    private EmpresaXProcesoService empresaXProcesoService;

    @Autowired
    private NodoService nodoService;

    @Autowired
    private ActividadService actividadService;

    @Autowired
    private RolService rolService;

    @Autowired
    private ProcesoRepository procesoRepository;
    
    @Autowired
    private NodoRepository nodoRepository;
    
    @Autowired
    private ActividadRepository actividadRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private RequiereRepository requiereRepository;
    
    private MockMvc mockMvc;
    private Proceso proceso;
    private Nodo nodo;
    private Actividad actividad;
    private Rol rol;
    private String nitEmpresa = "123456789";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        TestSecurityContext.authenticate(nitEmpresa);
        setupTestData();
    }

    private void setupTestData() {
        EmpresaRequestDTO empresaDTO = new EmpresaRequestDTO();
        empresaDTO.setNit(nitEmpresa);
        empresaDTO.setNombre("Empresa Test");
        empresaDTO.setCorreo("requiere.ctrl@test.com");
        empresaService.crearEmpresa(empresaDTO);

        var procesoCreado = procesoService.crearProceso(ProcesoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .nombre("Proceso Test")
                .descripcion("Descripción")
                .categoria("General")
                .borrador(false)
                .activo(true)
                .build());
        proceso = IntegrationTestData.cargarProceso(procesoRepository, procesoCreado.getId());

        empresaXProcesoService.asignarProceso(EmpresaXProcesoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .idProceso(proceso.getId())
                .nitOwner(nitEmpresa)
                .permiso(Permiso.ADMINISTRAR)
                .build());

        var nodoCreado = nodoService.crearNodo(NodoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .idProceso(proceso.getId())
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Test")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build());
        nodo = nodoRepository.findById(nodoCreado.getId()).orElseThrow();

        var actividadCreada = actividadService.crearActividad(ActividadRequestDTO.builder()
                .nodoId(nodo.getId())
                .descripcion("Actividad Test")
                .tipoActividad("USER")
                .build());
        actividad = actividadRepository.findById(actividadCreada.getId()).orElseThrow();

        var rolCreado = rolService.crearRol(RolRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .nombre("Rol Test")
                .permiso(Permiso.ADMINISTRAR)
                .build());
        rol = rolRepository.findById(rolCreado.getId()).orElseThrow();
    }

    @Test
    @DisplayName("POST /api/requiere - Asignar rol a actividad exitosamente")
    void testAsignarRol_Success() throws Exception {
        RequiereRequestDTO request = RequiereRequestDTO.builder()
                .actividadId(actividad.getId())
                .rolId(rol.getId())
                .build();

        mockMvc.perform(post("/api/requiere")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreActividad").exists())
                .andExpect(jsonPath("$.actividadId").value(actividad.getId()))
                .andExpect(jsonPath("$.rolId").value(rol.getId()));
    }

    @Test
    @DisplayName("POST /api/requiere - Fallar si rol ya está asignado")
    void testAsignarRol_YaAsignado() throws Exception {
        // Asignar rol primera vez
        RequiereRequestDTO request = RequiereRequestDTO.builder()
                .actividadId(actividad.getId())
                .rolId(rol.getId())
                .build();

        mockMvc.perform(post("/api/requiere")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Intentar asignar el mismo rol nuevamente
        mockMvc.perform(post("/api/requiere")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/requiere - Fallar si actividad no existe")
    void testAsignarRol_ActividadNoExiste() throws Exception {
        RequiereRequestDTO request = RequiereRequestDTO.builder()
                .actividadId(9999L)
                .rolId(rol.getId())
                .build();

        mockMvc.perform(post("/api/requiere")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/requiere - Fallar si rol no existe")
    void testAsignarRol_RolNoExiste() throws Exception {
        RequiereRequestDTO request = RequiereRequestDTO.builder()
                .actividadId(actividad.getId())
                .rolId(9999L)
                .build();

        mockMvc.perform(post("/api/requiere")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/requiere/actividad/{actividadId} - Obtener roles por actividad")
    void testObtenerRolesPorActividad_Success() throws Exception {
        // Asignar rol
        Requiere requiere = new Requiere();
        requiere.setActividad(actividad);
        requiere.setRol(rol);
        requiere.setDeleted(false);
        requiereRepository.save(requiere);

        mockMvc.perform(get("/api/requiere/actividad/{actividadId}", actividad.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rolId").value(rol.getId()));
    }

    @Test
    @DisplayName("GET /api/requiere/actividad/{actividadId} - Retorna vacío si no hay roles")
    void testObtenerRolesPorActividad_Empty() throws Exception {
        mockMvc.perform(get("/api/requiere/actividad/{actividadId}", actividad.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/requiere/actividad/{actividadId} - No lista roles eliminados")
    void testObtenerRolesPorActividad_NoEliminados() throws Exception {
        // Crear un requiere activo
        Requiere requiereActivo = new Requiere();
        requiereActivo.setActividad(actividad);
        requiereActivo.setRol(rol);
        requiereActivo.setDeleted(false);
        requiereRepository.save(requiereActivo);

        // Crear un requiere eliminado
        Rol rol2 = new Rol();
        rol2.setNombre("Rol 2");
        rol2.setEmpresa(proceso.getEmpresa());
        rol2.setDeleted(false);
        rolRepository.save(rol2);

        Requiere requiereEliminado = new Requiere();
        requiereEliminado.setActividad(actividad);
        requiereEliminado.setRol(rol2);
        requiereEliminado.setDeleted(true);
        requiereRepository.save(requiereEliminado);

        mockMvc.perform(get("/api/requiere/actividad/{actividadId}", actividad.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rolId").value(rol.getId()));
    }

    @Test
    @DisplayName("GET /api/requiere/rol/{rolId} - Obtener actividades por rol")
    void testObtenerActividadesPorRol_Success() throws Exception {
        // Asignar rol
        Requiere requiere = new Requiere();
        requiere.setActividad(actividad);
        requiere.setRol(rol);
        requiere.setDeleted(false);
        requiereRepository.save(requiere);

        mockMvc.perform(get("/api/requiere/rol/{rolId}", rol.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].actividadId").value(actividad.getId()));
    }

    @Test
    @DisplayName("GET /api/requiere/rol/{rolId} - Retorna vacío si no hay actividades")
    void testObtenerActividadesPorRol_Empty() throws Exception {
        mockMvc.perform(get("/api/requiere/rol/{rolId}", rol.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("DELETE /api/requiere/actividad/{actividadId}/rol/{rolId} - Quitar rol")
    void testQuitarRol_Success() throws Exception {
        // Asignar rol
        Requiere requiere = new Requiere();
        requiere.setActividad(actividad);
        requiere.setRol(rol);
        requiere.setDeleted(false);
        requiereRepository.save(requiere);

        mockMvc.perform(delete("/api/requiere/actividad/{actividadId}/rol/{rolId}",
                actividad.getId(), rol.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que está marcado como eliminado
        Requiere eliminado = requiereRepository
                .findByActividad_IdAndRol_IdAndDeletedFalse(actividad.getId(), rol.getId())
                .orElse(null);
        assert eliminado == null;
    }

    @Test
    @DisplayName("DELETE /api/requiere/actividad/{actividadId}/rol/{rolId} - Fallar si relación no existe")
    void testQuitarRol_NotFound() throws Exception {
        mockMvc.perform(delete("/api/requiere/actividad/{actividadId}/rol/{rolId}",
                actividad.getId(), rol.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
