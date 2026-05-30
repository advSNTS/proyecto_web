package com.proyecto.web.service;

import com.proyecto.web.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorreoServiceTest {

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private JavaMailSender mailSender;

    private CorreoService correoService;

    @BeforeEach
    void setUp() {
        correoService = new CorreoService(mailSenderProvider);
        ReflectionTestUtils.setField(correoService, "remitente", "test@example.com");
    }

    @Test
    void enviarCorreoVerificacion_mailDisabled_noLanzaExcepcion() {
        ReflectionTestUtils.setField(correoService, "mailEnabled", false);
        ReflectionTestUtils.setField(correoService, "failOnError", true);

        assertDoesNotThrow(() ->
                correoService.enviarCorreoVerificacion("user@test.com", "Ana", "http://link"));
        verifyNoInteractions(mailSenderProvider);
    }

    @Test
    void enviarCorreoVerificacion_sinMailSenderYFailOnErrorFalse_noLanzaExcepcion() {
        ReflectionTestUtils.setField(correoService, "mailEnabled", true);
        ReflectionTestUtils.setField(correoService, "failOnError", false);
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);

        assertDoesNotThrow(() ->
                correoService.enviarCorreoVerificacion("user@test.com", "Ana", "http://link"));
    }

    @Test
    void enviarCorreoVerificacion_sinMailSenderYFailOnErrorTrue_lanzaBusinessException() {
        ReflectionTestUtils.setField(correoService, "mailEnabled", true);
        ReflectionTestUtils.setField(correoService, "failOnError", true);
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);

        assertThrows(BusinessException.class, () ->
                correoService.enviarCorreoVerificacion("user@test.com", "Ana", "http://link"));
    }

    @Test
    void enviarCorreoVerificacion_nombreNull_usaSaludoHola() {
        ReflectionTestUtils.setField(correoService, "mailEnabled", true);
        ReflectionTestUtils.setField(correoService, "failOnError", false);
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);

        correoService.enviarCorreoVerificacion("user@test.com", null, "http://link");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertTrue(captor.getValue().getText().startsWith("Hola,"));
    }

    @Test
    void enviarCorreoVerificacion_nombreEnBlanco_usaSaludoHola() {
        ReflectionTestUtils.setField(correoService, "mailEnabled", true);
        ReflectionTestUtils.setField(correoService, "failOnError", false);
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);

        correoService.enviarCorreoVerificacion("user@test.com", "   ", "http://link");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertTrue(captor.getValue().getText().startsWith("Hola,"));
    }
}
