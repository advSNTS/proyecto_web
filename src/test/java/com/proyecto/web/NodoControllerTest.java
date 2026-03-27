package com.proyecto.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.NodoRequestDTO;
import com.proyecto.web.dto.NodoResponseDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.enums.TipoNodo;
import com.proyecto.web.service.NodoService;
import com.proyecto.web.service.ProcesoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // Compila correctamente con Maven. El error rojo es solo del IDE de VSCode, NO eliminar.

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class NodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NodoService nodoService;

    @Autowired
    private ProcesoService procesoService;

    private Long procesoId;
    private Long nodoId;

    @BeforeEach
    void setUp() {
        // Crear proceso
        ProcesoRequestDTO procesoDTO = ProcesoRequestDTO.builder()
                .nombre("Proceso Nodos Ctrl")
                .descripcion("Descripción")
                .categoria("Cat Nodos")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO proceso = procesoService.crearProceso(procesoDTO);
        this.procesoId = proceso.getId();

        // Crear nodo de prueba
        NodoRequestDTO nodoDTO = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Test")
                .build();

        NodoResponseDTO nodo = nodoService.crearNodo(nodoDTO);
        this.nodoId = nodo.getId();
    }

    @Test
    void crearNodo_retorna200() throws Exception {
        NodoRequestDTO dto = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.GATEWAY)
                .nombre("Nuevo Nodo")
                .build();

        mockMvc.perform(post("/nodos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo Nodo"))
                .andExpect(jsonPath("$.tipo").value("GATEWAY"))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void obtenerNodosPorProceso_retorna200ConContenido() throws Exception {
        mockMvc.perform(get("/nodos/proceso/" + procesoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0]", notNullValue()));
    }

    @Test
    void obtenerNodosPorProcesoYTipo_retorna200() throws Exception {
        mockMvc.perform(get("/nodos/proceso/" + procesoId + "/tipo?tipo=ACTIVIDAD")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].tipo").value("ACTIVIDAD"));
    }

    @Test
    void obtenerNodoPorId_retorna200() throws Exception {
        mockMvc.perform(get("/nodos/" + nodoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nodo Test"))
                .andExpect(jsonPath("$.tipo").value("ACTIVIDAD"))
                .andExpect(jsonPath("$.id").value(nodoId.intValue()))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void obtenerNodoInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/nodos/99999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarNodo_retorna200() throws Exception {
        NodoRequestDTO updateDTO = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.GATEWAY)
                .nombre("Nombre Actualizado")
                .build();

        mockMvc.perform(put("/nodos/" + nodoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre Actualizado"))
                .andExpect(jsonPath("$.tipo").value("GATEWAY"));
    }

    @Test
    void eliminarNodo_retorna204() throws Exception {
        mockMvc.perform(delete("/nodos/" + nodoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que ya no existe (hard delete)
        mockMvc.perform(get("/nodos/" + nodoId))
                .andExpect(status().isNotFound());
    }
}
