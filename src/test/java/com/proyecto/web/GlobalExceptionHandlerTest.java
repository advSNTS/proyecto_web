package com.proyecto.web;

import com.proyecto.web.exception.AuthenticationException;
import com.proyecto.web.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void accessDenied_deberiaRetornar403() {
        ResponseEntity<String> response =
                handler.handleAccessDenied(new AccessDeniedException("denegado"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("No tiene permisos para esta operación.", response.getBody());
    }

    @Test
    void authenticationException_deberiaRetornar401() {
        ResponseEntity<String> response =
                handler.handleAuthenticationException(new AuthenticationException("no autenticado"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("no autenticado", response.getBody());
    }

    @Test
    void runtimeExceptionGenerica_deberiaRetornar404() {
        ResponseEntity<String> response =
                handler.handleRuntimeException(new RuntimeException("recurso no hallado"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("recurso no hallado", response.getBody());
    }

    @Test
    void businessExceptionViaRuntimeHandler_deberiaRespetarStatus() {
        ResponseEntity<String> response = handler.handleRuntimeException(
                new BusinessException("conflicto", HttpStatus.CONFLICT));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("conflicto", response.getBody());
    }
}
