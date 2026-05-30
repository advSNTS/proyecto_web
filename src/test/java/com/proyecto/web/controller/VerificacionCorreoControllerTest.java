package com.proyecto.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.web.dto.VerificacionCorreoRequestDTO;
import com.proyecto.web.dto.VerificacionCorreoResponseDTO;
import com.proyecto.web.entity.Credencial;
import com.proyecto.web.repository.CredencialRepository;
import com.proyecto.web.service.VerificacionCorreoService;
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
@DisplayName("VerificacionCorreoController Tests")
class VerificacionCorreoControllerTest {

    @Autowired
    private WebApplicationContext context;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private CredencialRepository credencialRepository;
    
    private MockMvc mockMvc;
    private Credencial credencial;
    private String tokenVerificacion = "test_token_123456789";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        setupTestData();
    }

    private void setupTestData() {
        // Crear credencial sin verificar
        credencial = new Credencial();
        credencial.setCorreo("test@example.com");
        credencial.setContrasena("password123");
        credencial.setVerificado(false);
        credencial.setTokenVerificacion(tokenVerificacion);
        credencialRepository.save(credencial);
    }

    @Test
    @DisplayName("GET /api/auth/verificar-correo - Verificar con query param exitosamente")
    void testVerificarCorreoQueryParam_Success() throws Exception {
        mockMvc.perform(get("/api/auth/verificar-correo")
                .param("token", tokenVerificacion)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificado").value(true))
                .andExpect(jsonPath("$.correo").value("test@example.com"));

        // Verificar que se actualizó en BD
        Credencial actualizado = credencialRepository.findByCorreoIgnoreCase("test@example.com")
                .orElse(null);
        assert actualizado != null;
        assert actualizado.isVerificado();
        assert actualizado.getTokenVerificacion() == null;
    }

    @Test
    @DisplayName("GET /api/auth/verificar-correo - Fallar si token es nulo")
    void testVerificarCorreoQueryParam_TokenNulo() throws Exception {
        mockMvc.perform(get("/api/auth/verificar-correo")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /api/auth/verificar-correo - Fallar si token es inválido")
    void testVerificarCorreoQueryParam_TokenInvalido() throws Exception {
        mockMvc.perform(get("/api/auth/verificar-correo")
                .param("token", "invalid_token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /api/auth/verificar-correo/{token} - Verificar por path exitosamente")
    void testVerificarCorreoPorPath_Success() throws Exception {
        mockMvc.perform(get("/api/auth/verificar-correo/{token}", tokenVerificacion)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificado").value(true))
                .andExpect(jsonPath("$.correo").value("test@example.com"));
    }

    @Test
    @DisplayName("GET /api/auth/verificar-correo/{token} - Fallar si token inválido")
    void testVerificarCorreoPorPath_TokenInvalido() throws Exception {
        mockMvc.perform(get("/api/auth/verificar-correo/{token}", "invalid_token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/auth/verificar-correo - Verificar con query param")
    void testVerificarCorreoPorBody_QueryParam() throws Exception {
        mockMvc.perform(post("/api/auth/verificar-correo")
                .param("token", tokenVerificacion)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificado").value(true));
    }

    @Test
    @DisplayName("POST /api/auth/verificar-correo - Verificar con body")
    void testVerificarCorreoPorBody_Body() throws Exception {
        VerificacionCorreoRequestDTO request = new VerificacionCorreoRequestDTO();
        request.setToken(tokenVerificacion);

        mockMvc.perform(post("/api/auth/verificar-correo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificado").value(true));
    }

    @Test
    @DisplayName("POST /api/auth/verificar-correo - Fallar si token vacío")
    void testVerificarCorreoPorBody_TokenVacio() throws Exception {
        VerificacionCorreoRequestDTO request = new VerificacionCorreoRequestDTO();
        request.setToken("");

        mockMvc.perform(post("/api/auth/verificar-correo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/auth/reenviar-verificacion - Reenviar con query param exitosamente")
    void testReenviarVerificacion_QueryParam_Success() throws Exception {
        mockMvc.perform(post("/api/auth/reenviar-verificacion")
                .param("correo", "test@example.com")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("test@example.com"))
                .andExpect(jsonPath("$.verificado").value(false))
                .andExpect(jsonPath("$.mensaje").value("Se envio un nuevo correo de verificacion."));
    }

    @Test
    @DisplayName("POST /api/auth/reenviar-verificacion - Reenviar con body exitosamente")
    void testReenviarVerificacion_Body_Success() throws Exception {
        VerificacionCorreoRequestDTO request = new VerificacionCorreoRequestDTO();
        request.setCorreo("test@example.com");

        mockMvc.perform(post("/api/auth/reenviar-verificacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("test@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/reenviar-verificacion - Fallar si correo no existe")
    void testReenviarVerificacion_CorreoNoExiste() throws Exception {
        mockMvc.perform(post("/api/auth/reenviar-verificacion")
                .param("correo", "noexiste@example.com")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/auth/reenviar-verificacion - Fallar si correo está vacío")
    void testReenviarVerificacion_CorreoVacio() throws Exception {
        mockMvc.perform(post("/api/auth/reenviar-verificacion")
                .param("correo", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/auth/reenviar-verificacion - Fallar si no hay correo ni en body ni en query")
    void testReenviarVerificacion_SinCorreo() throws Exception {
        mockMvc.perform(post("/api/auth/reenviar-verificacion")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/auth/reenviar-verificacion - Indicar si correo ya está verificado")
    void testReenviarVerificacion_CorreoYaVerificado() throws Exception {
        // Verificar el correo primero
        credencial.setVerificado(true);
        credencial.setTokenVerificacion(null);
        credencialRepository.save(credencial);

        mockMvc.perform(post("/api/auth/reenviar-verificacion")
                .param("correo", "test@example.com")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificado").value(true))
                .andExpect(jsonPath("$.mensaje").value("Este correo ya se encuentra verificado."));
    }

    @Test
    @DisplayName("GET /api/auth/verificar-correo - Idempotencia (verificar dos veces)")
    void testVerificarCorreo_Idempotencia() throws Exception {
        // Primera verificación
        mockMvc.perform(get("/api/auth/verificar-correo")
                .param("token", tokenVerificacion)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificado").value(true));

        // Segunda verificación con mismo token (ya debe estar usado)
        mockMvc.perform(get("/api/auth/verificar-correo")
                .param("token", tokenVerificacion)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
