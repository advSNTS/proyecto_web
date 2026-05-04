package com.proyecto.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CorreoService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:no-reply@grupo11.inphotech.co}")
    private String remitente;

    public void enviarCorreoVerificacion(String destinatario, String nombre, String enlaceVerificacion) {
        if (!mailEnabled) {
            log.info("Correo desactivado. Enlace de verificacion para {}: {}", destinatario, enlaceVerificacion);
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null) {
            log.warn("No hay JavaMailSender configurado. Enlace de verificacion para {}: {}", destinatario, enlaceVerificacion);
            return;
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Confirma tu correo - Proyecto Web");
        mensaje.setText(construirCuerpo(nombre, enlaceVerificacion));

        try {
            mailSender.send(mensaje);
            log.info("Correo de verificacion enviado a {}", destinatario);
        } catch (MailException ex) {
            log.error("No se pudo enviar el correo de verificacion a {}. Enlace: {}", destinatario, enlaceVerificacion, ex);
        }
    }

    private String construirCuerpo(String nombre, String enlaceVerificacion) {
        String saludo = (nombre == null || nombre.isBlank()) ? "Hola" : "Hola " + nombre.trim();

        return saludo + ",\n\n"
                + "Gracias por registrarte en el sistema. Para activar tu cuenta, confirma tu correo entrando al siguiente enlace:\n\n"
                + enlaceVerificacion + "\n\n"
                + "Si no solicitaste este registro, puedes ignorar este mensaje.\n\n"
                + "Equipo Proyecto Web";
    }
}