package com.proyecto.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.MensajeExternoRequestDTO;
import com.proyecto.web.entity.MensajeExterno;
import com.proyecto.web.enums.TipoDestinoMensajeExterno;
import com.proyecto.web.repository.MensajeExternoRepository;
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
@DisplayName("MensajeExternoController Tests")
class MensajeExternoControllerTest {

    @Autowired
    private WebApplicationContext context;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MensajeExternoRepository mensajeExternoRepository;
    
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("POST /api/mensajes-externos - Crear mensaje externo exitosamente")
    void testCrearMensajeExterno_Success() throws Exception {
        MensajeExternoRequestDTO request = MensajeExternoRequestDTO.builder()
                .destinoTipo(TipoDestinoMensajeExterno.HTTP)
                .configuracion("{\"servidor\": \"smtp.example.com\"}")
                .credenciales("{\"usuario\": \"test@example.com\"}")
                .build();

        mockMvc.perform(post("/api/mensajes-externos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.destinoTipo").value("HTTP"));
    }

    @Test
    @DisplayName("POST /api/mensajes-externos - Crear con tipo SMS")
    void testCrearMensajeExterno_SMS() throws Exception {
        MensajeExternoRequestDTO request = MensajeExternoRequestDTO.builder()
                .destinoTipo(TipoDestinoMensajeExterno.COLA)
                .configuracion("{\"api_url\": \"https://api.sms.com\"}")
                .credenciales("{\"api_key\": \"key123\"}")
                .build();

        mockMvc.perform(post("/api/mensajes-externos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destinoTipo").value("COLA"));
    }

    @Test
    @DisplayName("GET /api/mensajes-externos - Listar todos los mensajes externos")
    void testListar_Success() throws Exception {
        // Crear varios mensajes externos
        MensajeExterno m1 = new MensajeExterno();
        m1.setDestinoTipo(TipoDestinoMensajeExterno.HTTP);
        m1.setConfiguracion("{\"config\": \"1\"}");
        m1.setCredenciales("{\"cred\": \"1\"}");
        m1.setEliminado(false);
        mensajeExternoRepository.save(m1);

        MensajeExterno m2 = new MensajeExterno();
        m2.setDestinoTipo(TipoDestinoMensajeExterno.COLA);
        m2.setConfiguracion("{\"config\": \"2\"}");
        m2.setCredenciales("{\"cred\": \"2\"}");
        m2.setEliminado(false);
        mensajeExternoRepository.save(m2);

        mockMvc.perform(get("/api/mensajes-externos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[1].id").exists())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/mensajes-externos - Retorna lista vacía si no hay mensajes")
    void testListar_Empty() throws Exception {
        mockMvc.perform(get("/api/mensajes-externos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/mensajes-externos - No lista mensajes eliminados")
    void testListar_NoEliminados() throws Exception {
        // Crear mensaje activo
        MensajeExterno activo = new MensajeExterno();
        activo.setDestinoTipo(TipoDestinoMensajeExterno.HTTP);
        activo.setConfiguracion("{\"config\": \"1\"}");
        activo.setCredenciales("{\"cred\": \"1\"}");
        activo.setEliminado(false);
        mensajeExternoRepository.save(activo);

        // Crear mensaje eliminado
        MensajeExterno eliminado = new MensajeExterno();
        eliminado.setDestinoTipo(TipoDestinoMensajeExterno.COLA);
        eliminado.setConfiguracion("{\"config\": \"2\"}");
        eliminado.setCredenciales("{\"cred\": \"2\"}");
        eliminado.setEliminado(true);
        mensajeExternoRepository.save(eliminado);

        mockMvc.perform(get("/api/mensajes-externos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].destinoTipo").value("HTTP"));
    }

    @Test
    @DisplayName("GET /api/mensajes-externos/{id} - Obtener mensaje externo por ID")
    void testObtener_Success() throws Exception {
        // Crear mensaje externo
        MensajeExterno mensaje = new MensajeExterno();
        mensaje.setDestinoTipo(TipoDestinoMensajeExterno.HTTP);
        mensaje.setConfiguracion("{\"config\": \"test\"}");
        mensaje.setCredenciales("{\"cred\": \"test\"}");
        mensaje.setEliminado(false);
        mensaje = mensajeExternoRepository.save(mensaje);

        mockMvc.perform(get("/api/mensajes-externos/{id}", mensaje.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mensaje.getId()))
                .andExpect(jsonPath("$.destinoTipo").value("HTTP"));
    }

    @Test
    @DisplayName("GET /api/mensajes-externos/{id} - Fallar si no existe")
    void testObtener_NotFound() throws Exception {
        mockMvc.perform(get("/api/mensajes-externos/{id}", 9999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("PUT /api/mensajes-externos/{id} - Actualizar mensaje externo")
    void testActualizar_Success() throws Exception {
        // Crear mensaje externo inicial
        MensajeExterno mensaje = new MensajeExterno();
        mensaje.setDestinoTipo(TipoDestinoMensajeExterno.HTTP);
        mensaje.setConfiguracion("{\"config\": \"original\"}");
        mensaje.setCredenciales("{\"cred\": \"original\"}");
        mensaje.setEliminado(false);
        mensaje = mensajeExternoRepository.save(mensaje);

        // Actualizar
        MensajeExternoRequestDTO updateRequest = MensajeExternoRequestDTO.builder()
                .destinoTipo(TipoDestinoMensajeExterno.COLA)
                .configuracion("{\"config\": \"actualizado\"}")
                .credenciales("{\"cred\": \"actualizado\"}")
                .build();

        mockMvc.perform(put("/api/mensajes-externos/{id}", mensaje.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destinoTipo").value("COLA"))
                .andExpect(jsonPath("$.configuracion").value("{\"config\": \"actualizado\"}"));
    }

    @Test
    @DisplayName("PUT /api/mensajes-externos/{id} - Fallar si no existe")
    void testActualizar_NotFound() throws Exception {
        MensajeExternoRequestDTO updateRequest = MensajeExternoRequestDTO.builder()
                .destinoTipo(TipoDestinoMensajeExterno.HTTP)
                .configuracion("{\"config\": \"test\"}")
                .credenciales("{\"cred\": \"test\"}")
                .build();

        mockMvc.perform(put("/api/mensajes-externos/{id}", 9999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("DELETE /api/mensajes-externos/{id} - Eliminar mensaje externo")
    void testEliminar_Success() throws Exception {
        // Crear mensaje externo
        MensajeExterno mensaje = new MensajeExterno();
        mensaje.setDestinoTipo(TipoDestinoMensajeExterno.HTTP);
        mensaje.setConfiguracion("{\"config\": \"test\"}");
        mensaje.setCredenciales("{\"cred\": \"test\"}");
        mensaje.setEliminado(false);
        mensaje = mensajeExternoRepository.save(mensaje);

        mockMvc.perform(delete("/api/mensajes-externos/{id}", mensaje.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verificar que está marcado como eliminado
        MensajeExterno eliminado = mensajeExternoRepository.findById(mensaje.getId())
                .orElse(null);
        assert eliminado != null;
        assert eliminado.isEliminado();
    }

    @Test
    @DisplayName("DELETE /api/mensajes-externos/{id} - Fallar si no existe")
    void testEliminar_NotFound() throws Exception {
        mockMvc.perform(delete("/api/mensajes-externos/{id}", 9999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
