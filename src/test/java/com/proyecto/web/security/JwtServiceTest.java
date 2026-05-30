package com.proyecto.web.security;

import com.proyecto.web.enums.TipoRolSistema;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    private static final String SECRET =
            "test-secret-key-must-be-long-enough-for-hs512-algorithm-12345";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3_600_000L);
    }

    @Test
    @DisplayName("Generar token y parsear claims correctamente")
    void generarToken_y_parseClaims() {
        String token = jwtService.generarToken(
                42L,
                "900-JWT-01",
                "user@test.com",
                false,
                List.of(TipoRolSistema.ADMIN, TipoRolSistema.EDITOR));

        Claims claims = jwtService.parseClaims(token);

        assertEquals("42", claims.getSubject());
        assertEquals(42L, claims.get("userId", Long.class));
        assertEquals("900-JWT-01", claims.get("nit", String.class));
        assertEquals("user@test.com", claims.get("email", String.class));
        assertEquals(false, claims.get("adminGlobal", Boolean.class));

        @SuppressWarnings("unchecked")
        List<String> authorities = claims.get("authorities", List.class);
        assertNotNull(authorities);
        assertEquals(
                Set.of("ROLE_ADMIN", "ROLE_EDITOR"),
                authorities.stream().collect(Collectors.toSet()));
    }

    @Test
    @DisplayName("toPrincipal mapea claims a UsuarioPrincipal")
    void toPrincipal_mapeaCorrectamente() {
        String token = jwtService.generarToken(
                7L,
                "NIT-PRIN",
                "admin@test.com",
                true,
                List.of(TipoRolSistema.ADMIN));

        UsuarioPrincipal principal = jwtService.toPrincipal(jwtService.parseClaims(token));

        assertEquals(7L, principal.getEmpleadoId());
        assertEquals("NIT-PRIN", principal.getNitEmpresa());
        assertTrue(principal.isAdminGlobal());
        assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("toPrincipal sin authorities devuelve lista vacía")
    void toPrincipal_sinAuthorities() {
        String token = jwtService.generarToken(1L, "NIT", null, false, List.of());

        UsuarioPrincipal principal = jwtService.toPrincipal(jwtService.parseClaims(token));

        assertTrue(principal.getAuthorities().isEmpty());
    }

    @Test
    @DisplayName("Token inválido lanza excepción al parsear")
    void parseClaims_tokenInvalido() {
        assertThrows(Exception.class, () -> jwtService.parseClaims("token.invalido"));
    }

    @Test
    @DisplayName("Token firmado con otra clave es rechazado")
    void parseClaims_claveDistinta() {
        JwtService otro = new JwtService(
                "otra-clave-secreta-suficientemente-larga-para-hs512-xyz",
                3_600_000L);
        String token = otro.generarToken(1L, "NIT", "a@b.com", false, List.of(TipoRolSistema.READER));

        assertThrows(Exception.class, () -> jwtService.parseClaims(token));
    }
}
