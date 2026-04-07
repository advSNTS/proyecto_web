package com.proyecto.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public void sendVerificationEmail(String to, String token) {
        String link = frontendBaseUrl + "/verificar-correo?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Verifica tu correo");
        message.setText(
                "Hola,\n\n" +
                "Haz clic en el siguiente enlace para verificar tu cuenta:\n" +
                link + "\n\n" +
                "Este enlace vence en 24 horas."
        );

        mailSender.send(message);
    }
}