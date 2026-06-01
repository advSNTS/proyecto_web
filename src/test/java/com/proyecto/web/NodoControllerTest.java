package com.proyecto.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.NodoRequestDTO;
import com.proyecto.web.dto.NodoResponseDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.enums.TipoNodo;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.NodoService;
import com.proyecto.web.service.ProcesoService;
import com.proyecto.web.support.TestSecurityContext;
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
class NodoControllerTest {

    private static final String NIT = "900NODO-CTRL-1";

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NodoService nodoService;

    @Autowired
    private ProcesoService procesoService;

    @Autowired
    private EmpresaService empresaService;

    private Long procesoId;
    private Long nodoId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        EmpresaRequestDTO emp = new EmpresaRequestDTO();
        emp.setNit(NIT);
        emp.setNombre("Emp Nodo Ctrl");
        emp.setCorreo("enc@test.com");
        empresaService.crearEmpresa(emp);
        TestSecurityContext.authenticate(NIT);

        ProcesoRequestDTO procesoDTO = ProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("Proceso Nodos Ctrl")
                .descripcion("Descripción")
                .categoria("Cat Nodos")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO proceso = procesoService.crearProceso(procesoDTO);
        this.procesoId = proceso.getId();

        NodoRequestDTO nodoDTO = NodoRequestDTO.builder()
                .nitEmpresa(NIT)
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Test")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        NodoResponseDTO nodo = nodoService.crearNodo(nodoDTO);
        this.nodoId = nodo.getId();
    }

    @Test
    void crearNodo_retorna200() throws Exception {
        NodoRequestDTO dto = NodoRequestDTO.builder()
                .nitEmpresa(NIT)
                .idProceso(procesoId)
                .tipo(TipoNodo.GATEWAY)
                .nombre("Nuevo Nodo")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        mockMvc.perform(post("/api/nodos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo Nodo"))
                .andExpect(jsonPath("$.tipo").value("GATEWAY"))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void obtenerNodosPorProceso_retorna200ConContenido() throws Exception {
        mockMvc.perform(get("/api/nodos/proceso/" + procesoId).param("nitEmpresa", NIT)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0]", notNullValue()));
    }

    @Test
    void obtenerNodosPorProcesoYTipo_retorna200() throws Exception {
        mockMvc.perform(get("/api/nodos/proceso/" + procesoId + "/tipo")
                .param("tipo", "ACTIVIDAD")
                .param("nitEmpresa", NIT)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].tipo").value("ACTIVIDAD"));
    }

    @Test
    void obtenerNodoPorId_retorna200() throws Exception {
        mockMvc.perform(get("/api/nodos/" + nodoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nodo Test"))
                .andExpect(jsonPath("$.tipo").value("ACTIVIDAD"))
                .andExpect(jsonPath("$.id").value(nodoId.intValue()))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void obtenerNodoInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/nodos/99999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarNodo_retorna200() throws Exception {
        NodoRequestDTO updateDTO = NodoRequestDTO.builder()
                .nitEmpresa(NIT)
                .idProceso(procesoId)
                .tipo(TipoNodo.GATEWAY)
                .nombre("Nombre Actualizado")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        mockMvc.perform(put("/api/nodos/" + nodoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre Actualizado"))
                .andExpect(jsonPath("$.tipo").value("GATEWAY"));
    }

    @Test
    void eliminarNodo_retorna204() throws Exception {
        mockMvc.perform(delete("/api/nodos/" + nodoId).param("nitEmpresa", NIT)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/nodos/" + nodoId))
                .andExpect(status().isNotFound());
    }
}
