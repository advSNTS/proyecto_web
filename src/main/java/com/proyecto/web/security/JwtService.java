package com.proyecto.web.security;

import com.proyecto.web.enums.TipoRolSistema;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generarToken(Long empleadoId, String nitEmpresa, boolean adminGlobal,
                               Collection<TipoRolSistema> rolesSistema) {
        List<String> authorities = rolesSistema.stream()
                .map(r -> "ROLE_" + r.name())
                .collect(Collectors.toList());
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(String.valueOf(empleadoId))
                .claim("nit", nitEmpresa)
                .claim("adminGlobal", adminGlobal)
                .claim("authorities", authorities)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @SuppressWarnings("unchecked")
    public UsuarioPrincipal toPrincipal(Claims claims) {
        Long empleadoId = Long.parseLong(claims.getSubject());
        String nit = claims.get("nit", String.class);
        Boolean adminGlobal = claims.get("adminGlobal", Boolean.class);
        List<String> auths = claims.get("authorities", List.class);
        Collection<GrantedAuthority> authorities = auths == null ? List.of()
                : auths.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        return new UsuarioPrincipal(empleadoId, nit, Boolean.TRUE.equals(adminGlobal), authorities);
    }
}
