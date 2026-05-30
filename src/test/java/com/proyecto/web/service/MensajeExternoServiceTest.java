package com.proyecto.web.service;

import com.proyecto.web.dto.MensajeExternoRequestDTO;
import com.proyecto.web.dto.MensajeExternoResponseDTO;
import com.proyecto.web.entity.MensajeExterno;
import com.proyecto.web.enums.TipoDestinoMensajeExterno;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.MensajeExternoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("MensajeExternoService Tests")
class MensajeExternoServiceTest {

    @Autowired
    private MensajeExternoService mensajeExternoService;

    @Autowired
    private MensajeExternoRepository mensajeExternoRepository;

    @BeforeEach
    void setUp() {
        mensajeExternoRepository.deleteAll();
    }

    @Test
    @DisplayName("Crear mensaje externo EMAIL exitosamente")
    void testCrear_Email_Success() {
        MensajeExternoRequestDTO dto = MensajeExternoRequestDTO.builder()
                .destinoTipo(TipoDestinoMensajeExterno.HTTP)
                .configuracion("{\"servidor\": \"smtp.example.com\"}")
                .credenciales("{\"usuario\": \"test@example.com\"}")
                .build();

        MensajeExternoResponseDTO result = mensajeExternoService.crear(dto);

        assertNotNull(result.getId());
        assertEquals(TipoDestinoMensajeExterno.HTTP, result.getDestinoTipo());
        assertEquals("{\"servidor\": \"smtp.example.com\"}", result.getConfiguracion());
    }

    @Test
    @DisplayName("Crear mensaje externo SMS exitosamente")
    void testCrear_SMS_Success() {
        MensajeExternoRequestDTO dto = MensajeExternoRequestDTO.builder()
                .destinoTipo(TipoDestinoMensajeExterno.COLA)
                .configuracion("{\"api_url\": \"https://api.sms.com\"}")
                .credenciales("{\"api_key\": \"key123\"}")
                .build();

        MensajeExternoResponseDTO result = mensajeExternoService.crear(dto);

        assertNotNull(result.getId());
        assertEquals(TipoDestinoMensajeExterno.COLA, result.getDestinoTipo());
    }

    @Test
    @DisplayName("Listar todos los mensajes externos")
    void testListar_Success() {
        // Crear varios mensajes
        MensajeExterno m1 = new MensajeExterno();
        m1.setDestinoTipo(TipoDestinoMensajeExterno.HTTP);
        m1.setConfiguracion("{\"config\": \"1\"}");
        m1.setCredenciales("{\"cred\": \"1\"}");
        m1.setEliminado(false);
        mensajeExternoRepository.save(m1);

        MensajeExterno m2 = new MensajeExterno();
        m2.setDestinoTipo(TipoDestinoMensajeExterno.COLA);
        m2.setConfiguracion("{\"config\": \"2\"}");
        m2.setCredenciales("{\"cred\": \"2\"}");
        m2.setEliminado(false);
        mensajeExternoRepository.save(m2);

        List<MensajeExternoResponseDTO> result = mensajeExternoService.listar();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Listar retorna vacío si no hay mensajes")
    void testListar_Empty() {
        List<MensajeExternoResponseDTO> result = mensajeExternoService.listar();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("No lista mensajes eliminados")
    void testListar_NoEliminados() {
        // Crear mensaje activo
        MensajeExterno activo = new MensajeExterno();
        activo.setDestinoTipo(TipoDestinoMensajeExterno.HTTP);
        activo.setConfiguracion("{\"config\": \"1\"}");
        activo.setCredenciales("{\"cred\": \"1\"}");
        activo.setEliminado(false);
        mensajeExternoRepository.save(activo);

        // Crear mensaje eliminado
        MensajeExterno eliminado = new MensajeExterno();
        eliminado.setDestinoTipo(TipoDestinoMensajeExterno.COLA);
        eliminado.setConfiguracion("{\"config\": \"2\"}");
        eliminado.setCredenciales("{\"cred\": \"2\"}");
        eliminado.setEliminado(true);
        mensajeExternoRepository.save(eliminado);

        List<MensajeExternoResponseDTO> result = mensajeExternoService.listar();

        assertEquals(1, result.size());
        assertEquals(TipoDestinoMensajeExterno.HTTP, result.get(0).getDestinoTipo());
    }

    @Test
    @DisplayName("Obtener mensaje por ID exitosamente")
    void testObtener_Success() {
        MensajeExterno mensaje = new MensajeExterno();
        mensaje.setDestinoTipo(TipoDestinoMensajeExterno.HTTP);
        mensaje.setConfiguracion("{\"config\": \"test\"}");
        mensaje.setCredenciales("{\"cred\": \"test\"}");
        mensaje.setEliminado(false);
        mensaje = mensajeExternoRepository.save(mensaje);

        MensajeExternoResponseDTO result = mensajeExternoService.obtener(mensaje.getId());

        assertNotNull(result);
        assertEquals(mensaje.getId(), result.getId());
        assertEquals(TipoDestinoMensajeExterno.HTTP, result.getDestinoTipo());
    }

    @Test
    @DisplayName("Fallar si mensaje no existe")
    void testObtener_NotFound() {
        assertThrows(BusinessException.class, () -> mensajeExternoService.obtener(9999L));
    }

    @Test
    @DisplayName("Fallar si mensaje está eliminado")
    void testObtener_Eliminado() {
        MensajeExterno mensaje = new MensajeExterno();
        mensaje.setDestinoTipo(TipoDestinoMensajeExterno.HTTP);
        mensaje.setConfiguracion("{\"config\": \"test\"}");
        mensaje.setCredenciales("{\"cred\": \"test\"}");
        mensaje.setEliminado(true);
        Long mensajeId = mensajeExternoRepository.save(mensaje).getId();

        assertThrows(BusinessException.class, () -> mensajeExternoService.obtener(mensajeId));
    }

    @Test
    @DisplayName("Actualizar mensaje exitosamente")
    void testActualizar_Success() {
        MensajeExterno mensaje = new MensajeExterno();
        mensaje.setDestinoTipo(TipoDestinoMensajeExterno.HTTP);
        mensaje.setConfiguracion("{\"config\": \"original\"}");
        mensaje.setCredenciales("{\"cred\": \"original\"}");
        mensaje.setEliminado(false);
        mensaje = mensajeExternoRepository.save(mensaje);

        MensajeExternoRequestDTO dto = MensajeExternoRequestDTO.builder()
                .destinoTipo(TipoDestinoMensajeExterno.COLA)
                .configuracion("{\"config\": \"actualizado\"}")
                .credenciales("{\"cred\": \"actualizado\"}")
                .build();

        MensajeExternoResponseDTO result = mensajeExternoService.actualizar(mensaje.getId(), dto);

        assertEquals(TipoDestinoMensajeExterno.COLA, result.getDestinoTipo());
        assertEquals("{\"config\": \"actualizado\"}", result.getConfiguracion());
    }

    @Test
    @DisplayName("Fallar al actualizar si mensaje no existe")
    void testActualizar_NotFound() {
        MensajeExternoRequestDTO dto = MensajeExternoRequestDTO.builder()
                .destinoTipo(TipoDestinoMensajeExterno.HTTP)
                .configuracion("{\"config\": \"test\"}")
                .credenciales("{\"cred\": \"test\"}")
                .build();

        assertThrows(BusinessException.class, () -> mensajeExternoService.actualizar(9999L, dto));
    }

    @Test
    @DisplayName("Eliminar mensaje exitosamente")
    void testEliminar_Success() {
        MensajeExterno mensaje = new MensajeExterno();
        mensaje.setDestinoTipo(TipoDestinoMensajeExterno.HTTP);
        mensaje.setConfiguracion("{\"config\": \"test\"}");
        mensaje.setCredenciales("{\"cred\": \"test\"}");
        mensaje.setEliminado(false);
        mensaje = mensajeExternoRepository.save(mensaje);

        mensajeExternoService.eliminar(mensaje.getId());

        // Verificar que está marcado como eliminado
        MensajeExterno eliminado = mensajeExternoRepository.findById(mensaje.getId()).orElse(null);
        assertNotNull(eliminado);
        assertTrue(eliminado.isEliminado());
    }

    @Test
    @DisplayName("Fallar al eliminar si mensaje no existe")
    void testEliminar_NotFound() {
        assertThrows(BusinessException.class, () -> mensajeExternoService.eliminar(9999L));
    }

    @Test
    @DisplayName("buscarActivoEntidad retorna mensaje activo")
    void testBuscarActivoEntidad_Success() {
        MensajeExterno mensaje = new MensajeExterno();
        mensaje.setDestinoTipo(TipoDestinoMensajeExterno.HTTP);
        mensaje.setConfiguracion("{\"config\": \"test\"}");
        mensaje.setCredenciales("{\"cred\": \"test\"}");
        mensaje.setEliminado(false);
        mensaje = mensajeExternoRepository.save(mensaje);

        MensajeExterno result = mensajeExternoService.buscarActivoEntidad(mensaje.getId());

        assertNotNull(result);
        assertEquals(mensaje.getId(), result.getId());
    }

    @Test
    @DisplayName("buscarActivoEntidad falla si mensaje no existe")
    void testBuscarActivoEntidad_NotFound() {
        assertThrows(BusinessException.class, () -> mensajeExternoService.buscarActivoEntidad(9999L));
    }
}
