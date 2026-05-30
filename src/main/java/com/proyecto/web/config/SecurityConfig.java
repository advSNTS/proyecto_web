package com.proyecto.web.config;

import com.proyecto.web.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_EDITOR = "EDITOR";
    private static final String API_EMPLEADOS_PATH = "/api/empleados/**";
    private static final String API_PROCESOS_PATH = "/api/procesos/**";
    private static final String API_PATH = "/api/**";

    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

    @Value("${app.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200,http://localhost:9876,https://grupo11.inphotech.co}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(parseAllowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(Customizer.withDefaults());

        if (securityEnabled) {
            http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/empresas").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/empleados/login").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/auth/verificar-correo").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/auth/verificar-correo/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/auth/verificar-correo").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/auth/reenviar-verificacion").permitAll()
                            .requestMatchers("/error").permitAll()

                            // Solo ADMIN puede gestionar empleados y asignar roles
                            .requestMatchers(HttpMethod.POST, "/api/empleados").hasRole(ROLE_ADMIN)
                            .requestMatchers(HttpMethod.GET, API_EMPLEADOS_PATH).hasRole(ROLE_ADMIN)
                            .requestMatchers(HttpMethod.PUT, API_EMPLEADOS_PATH).hasRole(ROLE_ADMIN)
                            .requestMatchers(HttpMethod.DELETE, API_EMPLEADOS_PATH).hasRole(ROLE_ADMIN)
                            .requestMatchers(HttpMethod.POST, "/api/rol-empleado").hasRole(ROLE_ADMIN)
                            .requestMatchers(HttpMethod.DELETE, "/api/rol-empleado/**").hasRole(ROLE_ADMIN)

                            // ADMIN y EDITOR pueden crear/editar/eliminar procesos
                            .requestMatchers(HttpMethod.POST, "/api/pools/**").hasRole(ROLE_ADMIN)
                            .requestMatchers(HttpMethod.POST, API_PROCESOS_PATH).hasAnyRole(ROLE_ADMIN, ROLE_EDITOR)
                            .requestMatchers(HttpMethod.PUT, API_PROCESOS_PATH).hasAnyRole(ROLE_ADMIN, ROLE_EDITOR)
                            .requestMatchers(HttpMethod.DELETE, API_PROCESOS_PATH).hasAnyRole(ROLE_ADMIN, ROLE_EDITOR)
                            .requestMatchers(HttpMethod.POST, API_PATH).hasAnyRole(ROLE_ADMIN, ROLE_EDITOR)
                            .requestMatchers(HttpMethod.PUT, API_PATH).hasAnyRole(ROLE_ADMIN, ROLE_EDITOR)
                            .requestMatchers(HttpMethod.DELETE, API_PATH).hasRole(ROLE_ADMIN)

                            // Cualquier autenticado puede leer
                            .requestMatchers(HttpMethod.GET, API_PATH).authenticated()
                            .anyRequest().authenticated())
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        } else {
            http.authorizeHttpRequests(a -> a.anyRequest().permitAll());
        }

        return http.build();
    }

    private List<String> parseAllowedOrigins() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
    }
}