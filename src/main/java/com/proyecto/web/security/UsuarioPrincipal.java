package com.proyecto.web.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class UsuarioPrincipal implements UserDetails {

    private final Long empleadoId;
    private final String nitEmpresa;
    private final boolean adminGlobal;
    private final Collection<? extends GrantedAuthority> authorities;

    public UsuarioPrincipal(Long empleadoId, String nitEmpresa, boolean adminGlobal,
                            Collection<? extends GrantedAuthority> authorities) {
        this.empleadoId = empleadoId;
        this.nitEmpresa = nitEmpresa;
        this.adminGlobal = adminGlobal;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return empleadoId != null ? empleadoId.toString() : "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
