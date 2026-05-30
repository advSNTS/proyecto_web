package com.proyecto.web.security;

import com.proyecto.web.enums.TipoRolSistema;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "securityEnabled", true);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Con seguridad desactivada no establece autenticación")
    void filtroDesactivado_noAutentica() throws Exception {
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "securityEnabled", false);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/procesos");
        request.addHeader("Authorization", "Bearer token");

        jwtAuthenticationFilter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).parseClaims(any());
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    @DisplayName("Token Bearer válido establece UsuarioPrincipal en el contexto")
    void tokenValido_estableceAutenticacion() throws Exception {
        String token = "jwt-valido";
        var jwt = new JwtService(
                "test-secret-key-must-be-long-enough-for-hs512-algorithm-12345",
                3_600_000L);
        var claims = jwt.parseClaims(jwt.generarToken(
                5L, "NIT-F", "f@test.com", false, List.of(TipoRolSistema.ADMIN)));
        UsuarioPrincipal principal = jwt.toPrincipal(claims);

        when(jwtService.parseClaims(token)).thenReturn(claims);
        when(jwtService.toPrincipal(claims)).thenReturn(principal);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/procesos");
        request.addHeader("Authorization", "Bearer " + token);

        jwtAuthenticationFilter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(principal, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    @DisplayName("Token inválido limpia el contexto de seguridad")
    void tokenInvalido_limpiaContexto() throws Exception {
        when(jwtService.parseClaims("mal")).thenThrow(new RuntimeException("invalid"));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/procesos");
        request.addHeader("Authorization", "Bearer mal");

        jwtAuthenticationFilter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    @DisplayName("Sin cabecera Authorization continúa la cadena sin autenticar")
    void sinToken_continuaCadena() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/procesos");

        jwtAuthenticationFilter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).parseClaims(any());
        verify(filterChain).doFilter(any(), any());
    }
}
