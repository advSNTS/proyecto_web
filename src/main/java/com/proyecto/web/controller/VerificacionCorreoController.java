package com.proyecto.web.controller;

import com.proyecto.web.dto.VerificacionCorreoResponseDTO;
import com.proyecto.web.service.VerificacionCorreoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/reenviar-verificacion")
    public VerificacionCorreoResponseDTO reenviarVerificacion(@RequestParam String correo) {
        return verificacionCorreoService.reenviarVerificacion(correo);
    }
}