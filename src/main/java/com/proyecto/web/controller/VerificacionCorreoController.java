package com.proyecto.web.controller;

import com.proyecto.web.dto.VerificacionCorreoRequestDTO;
import com.proyecto.web.dto.VerificacionCorreoResponseDTO;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.service.VerificacionCorreoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class VerificacionCorreoController {

    private final VerificacionCorreoService verificacionCorreoService;

    @GetMapping("/verificar-correo")
    public VerificacionCorreoResponseDTO verificarCorreo(@RequestParam String token) {
        return verificacionCorreoService.verificarCorreo(token);
    }

    @GetMapping("/verificar-correo/{token}")
    public VerificacionCorreoResponseDTO verificarCorreoPorPath(@PathVariable String token) {
        return verificacionCorreoService.verificarCorreo(token);
    }

    @PostMapping("/verificar-correo")
    public VerificacionCorreoResponseDTO verificarCorreoPorBody(
            @RequestBody(required = false) VerificacionCorreoRequestDTO request,
            @RequestParam(required = false) String token) {
        String tokenFinal = token;

        if ((tokenFinal == null || tokenFinal.isBlank()) && request != null) {
            tokenFinal = request.getToken();
        }

        return verificacionCorreoService.verificarCorreo(tokenFinal);
    }

    @PostMapping("/reenviar-verificacion")
    public VerificacionCorreoResponseDTO reenviarVerificacion(
            @RequestBody(required = false) VerificacionCorreoRequestDTO request,
            @RequestParam(required = false) String correo) {
        String correoFinal = correo;

        if ((correoFinal == null || correoFinal.isBlank()) && request != null) {
            correoFinal = request.getCorreo();
        }

        if (correoFinal == null || correoFinal.isBlank()) {
            throw new BusinessException("Debe indicar el correo", HttpStatus.BAD_REQUEST);
        }

        return verificacionCorreoService.reenviarVerificacion(correoFinal);
    }
}

//prueba merge, ingorenlo uwu 