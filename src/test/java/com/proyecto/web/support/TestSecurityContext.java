package com.proyecto.web.support;

import com.proyecto.web.security.UsuarioPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;

public final class TestSecurityContext {

    private TestSecurityContext() {
    }

    public static void authenticate(String nitEmpresa) {
        authenticate(1L, nitEmpresa, "ROLE_ADMIN", "ROLE_EDITOR");
    }

    public static void authenticate(Long empleadoId, String nitEmpresa, String... authorities) {
        List<SimpleGrantedAuthority> grantedAuthorities = Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .toList();
        UsuarioPrincipal principal = new UsuarioPrincipal(empleadoId, nitEmpresa, false, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "test-token", principal.getAuthorities()));
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
