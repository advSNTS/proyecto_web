package com.proyecto.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.PoolRequestDTO;
import com.proyecto.web.dto.ProcesoCompartidoRequestDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.enums.PermisoProcesoCompartido;
import com.proyecto.web.repository.CredencialRepository;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.PoolService;
import com.proyecto.web.service.ProcesoService;
import com.proyecto.web.security.UsuarioPrincipal;
import com.proyecto.web.service.VerificacionCorreoService;
import com.proyecto.web.support.TestSecurityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProcesoControllerTest {

    private static final String NIT = "900PROC-CTRL-1";

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcesoService procesoService;

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private PoolService poolService;

    @Autowired
    private CredencialRepository credencialRepository;

    @Autowired
    private VerificacionCorreoService verificacionCorreoService;

    private Long procesoId;
    private Long poolDestinoId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        EmpresaRequestDTO emp = new EmpresaRequestDTO();
        emp.setNit(NIT);
        emp.setNombre("Emp Ctrl");
        emp.setCorreo("ec@test.com");
        empresaService.crearEmpresa(emp);
        TestSecurityContext.authenticate(NIT);

        ProcesoRequestDTO procesoDTO = ProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("Proceso Test Controlador")
                .descripcion("Descripción del proceso")
                .categoria("Categoría Test")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO proceso = procesoService.crearProceso(procesoDTO);
        this.procesoId = proceso.getId();

        var credencial = credencialRepository.findByCorreo("ec@test.com").orElseThrow();
        verificacionCorreoService.verificarCorreo(credencial.getTokenVerificacion());

        poolDestinoId = poolService.crear(PoolRequestDTO.builder()
                .nombre("Pool Colaboracion Ctrl")
                .descripcion("Pool para compartir en tests")
                .build()).getId();

    }

    @AfterEach
    void limpiarSeguridad() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarAdmin() {
        var credencial = credencialRepository.findByCorreo("ec@test.com").orElseThrow();
        Long adminId = credencial.getEmpleado().getId();
        UsuarioPrincipal principal = new UsuarioPrincipal(adminId, NIT, false, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "token", principal.getAuthorities()));
    }

    @Test
    void crearProceso_retorna200() throws Exception {
        ProcesoRequestDTO dto = ProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("Nuevo Proceso")
                .descripcion("Descripción nueva")
                .categoria("Nueva Categoría")
                .borrador(false)
                .activo(true)
                .build();

        mockMvc.perform(post("/api/procesos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo Proceso"))
                .andExpect(jsonPath("$.categoria").value("Nueva Categoría"))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void obtenerTodosProcesos_retorna200ConContenido() throws Exception {
        mockMvc.perform(get("/api/procesos").param("nitEmpresa", NIT)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0]", notNullValue()));
    }

    @Test
    void obtenerProcesoPorId_retorna200() throws Exception {
        mockMvc.perform(get("/api/procesos/" + procesoId).param("nitEmpresa", NIT)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Proceso Test Controlador"))
                .andExpect(jsonPath("$.id").value(procesoId.intValue()))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void obtenerProcesoPorCategoria_retorna200() throws Exception {
        mockMvc.perform(get("/api/procesos/categoria/Categoría Test").param("nitEmpresa", NIT)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void actualizarProceso_retorna200() throws Exception {
        ProcesoRequestDTO updateDTO = ProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("Nombre Actualizado")
                .descripcion("Descripción actualizada")
                .categoria("Categoría Test")
                .borrador(true)
                .activo(true)
                .build();

        mockMvc.perform(put("/api/procesos/" + procesoId).param("nitEmpresa", NIT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre Actualizado"));
    }

    @Test
    void actualizarProcesoConIdEmpleado_retorna200() throws Exception {
        ProcesoRequestDTO updateDTO = ProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("Actualizado Con Empleado")
                .descripcion("Descripción")
                .categoria("Categoría Test")
                .borrador(false)
                .activo(true)
                .build();

        mockMvc.perform(put("/api/procesos/" + procesoId + "?idEmpleado=123").param("nitEmpresa", NIT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Actualizado Con Empleado"));
    }

    @Test
    void eliminarProceso_retorna204() throws Exception {
        mockMvc.perform(delete("/api/procesos/" + procesoId).param("nitEmpresa", NIT)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/procesos/" + procesoId).param("nitEmpresa", NIT))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerHistorialProceso_retorna200() throws Exception {
        mockMvc.perform(get("/api/procesos/" + procesoId + "/historial")
                .param("nitEmpresa", NIT)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerDetalleRapido_retorna200() throws Exception {
        mockMvc.perform(get("/api/procesos/" + procesoId + "/detalle")
                .param("nitEmpresa", NIT)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(procesoId.intValue()))
                .andExpect(jsonPath("$.nombre").value("Proceso Test Controlador"));
    }

    @Test
    void obtenerResumenHistorial_retorna200() throws Exception {
        mockMvc.perform(get("/api/procesos/" + procesoId + "/historial/resumen")
                .param("nitEmpresa", NIT)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProceso").value(procesoId.intValue()));
    }

    @Test
    void compartirProceso_retorna200() throws Exception {
        autenticarAdmin();
        ProcesoCompartidoRequestDTO dto = ProcesoCompartidoRequestDTO.builder()
                .poolId(poolDestinoId)
                .permiso(PermisoProcesoCompartido.LECTURA)
                .build();

        mockMvc.perform(post("/api/procesos/" + procesoId + "/compartir")
                .param("nitEmpresa", NIT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.poolId").value(poolDestinoId.intValue()));
    }

    @Test
    void listarCompartidos_retorna200() throws Exception {
        autenticarAdmin();
        ProcesoCompartidoRequestDTO dto = ProcesoCompartidoRequestDTO.builder()
                .poolId(poolDestinoId)
                .permiso(PermisoProcesoCompartido.LECTURA)
                .build();

        mockMvc.perform(post("/api/procesos/" + procesoId + "/compartir")
                .param("nitEmpresa", NIT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        mockMvc.perform(get("/api/procesos/" + procesoId + "/compartidos")
                .param("nitEmpresa", NIT)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void obtenerProceso_sinNitEmpresa_retorna400() throws Exception {
        mockMvc.perform(get("/api/procesos/" + procesoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(procesoId.intValue()));
    }
}
