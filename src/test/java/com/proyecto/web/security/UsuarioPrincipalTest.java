package com.proyecto.web.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioPrincipalTest {

    @Test
    void userDetails_deberiaExponerPropiedadesEsperadas() {
        UsuarioPrincipal principal = new UsuarioPrincipal(
                42L,
                "NIT-001",
                false,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        assertEquals("", principal.getPassword());
        assertEquals("42", principal.getUsername());
        assertTrue(principal.isAccountNonExpired());
        assertTrue(principal.isAccountNonLocked());
        assertTrue(principal.isCredentialsNonExpired());
        assertTrue(principal.isEnabled());
        assertEquals(42L, principal.getEmpleadoId());
        assertEquals("NIT-001", principal.getNitEmpresa());
        assertFalse(principal.isAdminGlobal());
        assertEquals(1, principal.getAuthorities().size());
    }
}
