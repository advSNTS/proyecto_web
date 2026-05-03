package com.proyecto.web.util;

import com.proyecto.web.security.UsuarioPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<UsuarioPrincipal> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal p) {
            return Optional.of(p);
        }
        return Optional.empty();
    }

    public static String resolverNitEmpresa(String nitParam) {
        if (nitParam != null && !nitParam.isBlank()) {
            return nitParam;
        }
        return currentUser()
                .map(UsuarioPrincipal::getNitEmpresa)
                .orElse(null);
    }
}
