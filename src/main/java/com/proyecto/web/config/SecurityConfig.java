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

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200",
                "http://localhost:9876",
                "https://grupo11.inphotech.co"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(Customizer.withDefaults());
        if (securityEnabled) {
            http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/empresas").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/empleados/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/empresas").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/empleados/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/verificar-correo").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/reenviar-verificacion").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Solo ADMIN puede gestionar empleados y asignar roles
                        .requestMatchers(HttpMethod.POST, "/api/empleados").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/empleados/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/empleados/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/empleados/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/rol-empleado").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/rol-empleado/**").hasRole("ADMIN")

                        // ADMIN y EDITOR pueden crear/editar/eliminar procesos
                        .requestMatchers(HttpMethod.POST, "/api/pools/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/procesos/**").hasAnyRole("ADMIN", "EDITOR")
                        .requestMatchers(HttpMethod.PUT, "/api/procesos/**").hasAnyRole("ADMIN", "EDITOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/procesos/**").hasAnyRole("ADMIN", "EDITOR")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("ADMIN", "EDITOR")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("ADMIN", "EDITOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")

                        // Cualquier autenticado puede leer
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        .anyRequest().authenticated())
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        } else {
            http.authorizeHttpRequests(a -> a.anyRequest().permitAll());
        }
        return http.build();
    }
}
