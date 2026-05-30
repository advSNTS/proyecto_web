package com.proyecto.web.config;

import com.proyecto.web.security.JwtAuthenticationFilter;
import com.proyecto.web.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Security beans smoke tests")
class SecurityIntegrationTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("Beans de seguridad están registrados en el contexto")
    void beansSeguridad_registrados() {
        assertNotNull(securityConfig);
        assertNotNull(jwtAuthenticationFilter);
        assertNotNull(jwtService);
    }
}
