package com.proyecto.web.service;

import com.proyecto.web.dto.VerificacionCorreoResponseDTO;
import com.proyecto.web.entity.Credencial;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.CredencialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class VerificacionCorreoService {

    private final CredencialRepository credencialRepository;
    private final CorreoService correoService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    @Transactional
    public void crearTokenYEnviar(Credencial credencial, String nombreDestinatario) {
        credencial.setVerificado(false);
        credencial.setFechaVerificacion(null);
        credencial.setTokenVerificacion(generarTokenSeguro());
        credencialRepository.save(credencial);

        correoService.enviarCorreoVerificacion(
                credencial.getCorreo(),
                nombreDestinatario,
                construirUrlVerificacion(credencial.getTokenVerificacion())
        );
    }

    @Transactional
    public VerificacionCorreoResponseDTO verificarCorreo(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException("Token de verificacion invalido", HttpStatus.BAD_REQUEST);
        }

        Credencial credencial = credencialRepository.findByTokenVerificacion(token.trim())
                .orElseThrow(() -> new BusinessException("Token de verificacion invalido o ya usado", HttpStatus.BAD_REQUEST));

        if (!credencial.isVerificado()) {
            credencial.setVerificado(true);
            credencial.setFechaVerificacion(LocalDateTime.now());
            credencial.setTokenVerificacion(null);
            credencialRepository.save(credencial);
        }

        return VerificacionCorreoResponseDTO.builder()
                .verificado(true)
                .correo(credencial.getCorreo())
                .mensaje("Correo verificado correctamente. Ya puedes iniciar sesion.")
                .build();
    }

    @Transactional
    public VerificacionCorreoResponseDTO reenviarVerificacion(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new BusinessException("Debe indicar el correo", HttpStatus.BAD_REQUEST);
        }

        Credencial credencial = credencialRepository.findByCorreo(correo.trim())
                .orElseThrow(() -> new BusinessException("No existe una cuenta con ese correo", HttpStatus.NOT_FOUND));

        if (credencial.isVerificado()) {
            return VerificacionCorreoResponseDTO.builder()
                    .verificado(true)
                    .correo(credencial.getCorreo())
                    .mensaje("Este correo ya se encuentra verificado.")
                    .build();
        }

        String nombre = credencial.getEmpleado() != null ? credencial.getEmpleado().getNombre() : null;

        crearTokenYEnviar(credencial, nombre);

        return VerificacionCorreoResponseDTO.builder()
                .verificado(false)
                .correo(credencial.getCorreo())
                .mensaje("Se envio un nuevo correo de verificacion.")
                .build();
    }

    private String generarTokenSeguro() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String construirUrlVerificacion(String token) {
        String base = frontendBaseUrl == null || frontendBaseUrl.isBlank()
                ? "http://localhost:4200"
                : frontendBaseUrl.trim();

        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        return base + "/verificar-correo?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }
}