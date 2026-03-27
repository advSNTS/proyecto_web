
package com.proyecto.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.service.ProcesoService;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProcesoControllerTest {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcesoService procesoService;

    private Long procesoId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        // Crear proceso de prueba
        ProcesoRequestDTO procesoDTO = ProcesoRequestDTO.builder()
                .nombre("Proceso Test Controlador")
                .descripcion("Descripción del proceso")
                .categoria("Categoría Test")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO proceso = procesoService.crearProceso(procesoDTO);
        this.procesoId = proceso.getId();
    }

    @Test
    void crearProceso_retorna200() throws Exception {
        ProcesoRequestDTO dto = ProcesoRequestDTO.builder()
                .nombre("Nuevo Proceso")
                .descripcion("Descripción nueva")
                .categoria("Nueva Categoría")
                .borrador(false)
                .activo(true)
                .build();

        mockMvc.perform(post("/procesos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo Proceso"))
                .andExpect(jsonPath("$.categoria").value("Nueva Categoría"))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void obtenerTodosProcesos_retorna200ConContenido() throws Exception {
        mockMvc.perform(get("/procesos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0]", notNullValue()));
    }

    @Test
    void obtenerProcesoPorId_retorna200() throws Exception {
        mockMvc.perform(get("/procesos/" + procesoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Proceso Test Controlador"))
                .andExpect(jsonPath("$.id").value(procesoId.intValue()))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void obtenerProcesoPorCategoria_retorna200() throws Exception {
        mockMvc.perform(get("/procesos/categoria/Categoría Test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void actualizarProceso_retorna200() throws Exception {
        ProcesoRequestDTO updateDTO = ProcesoRequestDTO.builder()
                .nombre("Nombre Actualizado")
                .descripcion("Descripción actualizada")
                .categoria("Categoría Test")
                .borrador(true)
                .activo(true)
                .build();

        mockMvc.perform(put("/procesos/" + procesoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre Actualizado"));
    }

    @Test
    void actualizarProcesoConIdEmpleado_retorna200() throws Exception {
        ProcesoRequestDTO updateDTO = ProcesoRequestDTO.builder()
                .nombre("Actualizado Con Empleado")
                .descripcion("Descripción")
                .categoria("Categoría Test")
                .borrador(false)
                .activo(true)
                .build();

        mockMvc.perform(put("/procesos/" + procesoId + "?idEmpleado=123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Actualizado Con Empleado"));
    }

    @Test
    void eliminarProceso_retorna204() throws Exception {
        mockMvc.perform(delete("/procesos/" + procesoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que ya no existe
        mockMvc.perform(get("/procesos/" + procesoId))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerHistorialProceso_retorna200() throws Exception {
        mockMvc.perform(get("/procesos/" + procesoId + "/historial")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
