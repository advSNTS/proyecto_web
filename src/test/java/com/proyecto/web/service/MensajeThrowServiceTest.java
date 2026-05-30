package com.proyecto.web.service;

import com.proyecto.web.dto.MensajeThrowRequestDTO;
import com.proyecto.web.dto.MensajeThrowResponseDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.entity.MensajeThrow;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.EmpresaRepository;
import com.proyecto.web.repository.MensajeThrowRepository;
import com.proyecto.web.repository.PoolRepository;
import com.proyecto.web.repository.ProcesoRepository;
import com.proyecto.web.support.IntegrationTestData;
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
@DisplayName("MensajeThrowService Tests")
class MensajeThrowServiceTest {

    @Autowired
    private MensajeThrowService mensajeThrowService;

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private ProcesoService procesoService;

    @Autowired
    private ProcesoRepository procesoRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PoolRepository poolRepository;

    @Autowired
    private MensajeThrowRepository mensajeThrowRepository;

    private Proceso proceso;
    private String nitEmpresa = "123456789";

    @BeforeEach
    void setUp() {
        var creado = IntegrationTestData.crearEmpresaYProceso(empresaService, procesoService, nitEmpresa);
        proceso = IntegrationTestData.cargarProceso(procesoRepository, creado.getId());
    }

    @Test
    @DisplayName("Crear mensaje throw exitosamente")
    void testCrear_Success() {
        MensajeThrowRequestDTO dto = MensajeThrowRequestDTO.builder()
                .procesoId(proceso.getId())
                .nombreMensaje("Mensaje Throw")
                .payloadTemplate("{\"data\": \"value\"}")
                .build();

        MensajeThrowResponseDTO result = mensajeThrowService.crear(nitEmpresa, dto);

        assertNotNull(result.getId());
        assertEquals("Mensaje Throw", result.getNombreMensaje());
        assertEquals("{\"data\": \"value\"}", result.getPayloadTemplate());
    }

    @Test
    @DisplayName("Fallar si proceso no existe")
    void testCrear_ProcesoNoExiste() {
        MensajeThrowRequestDTO dto = MensajeThrowRequestDTO.builder()
                .procesoId(9999L)
                .nombreMensaje("Mensaje Throw")
                .payloadTemplate("{\"data\": \"value\"}")
                .build();

        assertThrows(BusinessException.class, () -> mensajeThrowService.crear(nitEmpresa, dto));
    }

    @Test
    @DisplayName("Fallar si proceso no es vigente")
    void testCrear_ProcesoNoVigente() {
        Proceso procesoNoVigente = IntegrationTestData.crearProcesoInactivo(
                procesoRepository, empresaRepository, poolRepository, nitEmpresa, "Proceso No Vigente");

        MensajeThrowRequestDTO dto = MensajeThrowRequestDTO.builder()
                .procesoId(procesoNoVigente.getId())
                .nombreMensaje("Mensaje Throw")
                .payloadTemplate("{\"data\": \"value\"}")
                .build();

        assertThrows(Exception.class, () -> mensajeThrowService.crear(nitEmpresa, dto));
    }

    @Test
    @DisplayName("Listar mensajes por proceso exitosamente")
    void testListarPorProceso_Success() {
        // Crear varios mensajes
        MensajeThrow m1 = new MensajeThrow();
        m1.setProceso(proceso);
        m1.setNombreMensaje("Mensaje 1");
        m1.setPayloadTemplate("{\"data\": \"1\"}");
        m1.setEliminado(false);
        mensajeThrowRepository.save(m1);

        MensajeThrow m2 = new MensajeThrow();
        m2.setProceso(proceso);
        m2.setNombreMensaje("Mensaje 2");
        m2.setPayloadTemplate("{\"data\": \"2\"}");
        m2.setEliminado(false);
        mensajeThrowRepository.save(m2);

        List<MensajeThrowResponseDTO> result = mensajeThrowService.listarPorProceso(nitEmpresa, proceso.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(m -> "Mensaje 1".equals(m.getNombreMensaje())));
    }

    @Test
    @DisplayName("Listar mensajes retorna vacío si no hay")
    void testListarPorProceso_Empty() {
        List<MensajeThrowResponseDTO> result = mensajeThrowService.listarPorProceso(nitEmpresa, proceso.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("No lista mensajes eliminados")
    void testListarPorProceso_NoEliminados() {
        // Crear mensaje activo
        MensajeThrow activo = new MensajeThrow();
        activo.setProceso(proceso);
        activo.setNombreMensaje("Activo");
        activo.setPayloadTemplate("{\"data\": \"1\"}");
        activo.setEliminado(false);
        mensajeThrowRepository.save(activo);

        // Crear mensaje eliminado
        MensajeThrow eliminado = new MensajeThrow();
        eliminado.setProceso(proceso);
        eliminado.setNombreMensaje("Eliminado");
        eliminado.setPayloadTemplate("{\"data\": \"2\"}");
        eliminado.setEliminado(true);
        mensajeThrowRepository.save(eliminado);

        List<MensajeThrowResponseDTO> result = mensajeThrowService.listarPorProceso(nitEmpresa, proceso.getId());

        assertEquals(1, result.size());
        assertEquals("Activo", result.get(0).getNombreMensaje());
    }

    @Test
    @DisplayName("Obtener mensaje por ID exitosamente")
    void testObtener_Success() {
        MensajeThrow mensaje = new MensajeThrow();
        mensaje.setProceso(proceso);
        mensaje.setNombreMensaje("Mensaje Test");
        mensaje.setPayloadTemplate("{\"data\": \"test\"}");
        mensaje.setEliminado(false);
        mensaje = mensajeThrowRepository.save(mensaje);

        MensajeThrowResponseDTO result = mensajeThrowService.obtener(nitEmpresa, mensaje.getId());

        assertNotNull(result);
        assertEquals(mensaje.getId(), result.getId());
        assertEquals("Mensaje Test", result.getNombreMensaje());
    }

    @Test
    @DisplayName("Fallar si mensaje no existe")
    void testObtener_NotFound() {
        assertThrows(BusinessException.class, () -> mensajeThrowService.obtener(nitEmpresa, 9999L));
    }

    @Test
    @DisplayName("Fallar si mensaje está eliminado")
    void testObtener_Eliminado() {
        MensajeThrow mensaje = new MensajeThrow();
        mensaje.setProceso(proceso);
        mensaje.setNombreMensaje("Eliminado");
        mensaje.setPayloadTemplate("{\"data\": \"test\"}");
        mensaje.setEliminado(true);
        Long mensajeId = mensajeThrowRepository.save(mensaje).getId();

        assertThrows(BusinessException.class, () -> mensajeThrowService.obtener(nitEmpresa, mensajeId));
    }

    @Test
    @DisplayName("Actualizar mensaje exitosamente")
    void testActualizar_Success() {
        MensajeThrow mensaje = new MensajeThrow();
        mensaje.setProceso(proceso);
        mensaje.setNombreMensaje("Original");
        mensaje.setPayloadTemplate("{\"data\": \"original\"}");
        mensaje.setEliminado(false);
        mensaje = mensajeThrowRepository.save(mensaje);

        MensajeThrowRequestDTO dto = MensajeThrowRequestDTO.builder()
                .procesoId(proceso.getId())
                .nombreMensaje("Actualizado")
                .payloadTemplate("{\"data\": \"actualizado\"}")
                .build();

        MensajeThrowResponseDTO result = mensajeThrowService.actualizar(nitEmpresa, mensaje.getId(), dto);

        assertEquals("Actualizado", result.getNombreMensaje());
        assertEquals("{\"data\": \"actualizado\"}", result.getPayloadTemplate());
    }

    @Test
    @DisplayName("Fallar al actualizar si mensaje no existe")
    void testActualizar_NotFound() {
        MensajeThrowRequestDTO dto = MensajeThrowRequestDTO.builder()
                .procesoId(proceso.getId())
                .nombreMensaje("Actualizado")
                .payloadTemplate("{\"data\": \"test\"}")
                .build();

        assertThrows(BusinessException.class, () -> mensajeThrowService.actualizar(nitEmpresa, 9999L, dto));
    }

    @Test
    @DisplayName("Actualizar a proceso diferente")
    void testActualizar_CambiarProceso() {
        var proceso2Dto = procesoService.crearProceso(ProcesoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .nombre("Proceso 2")
                .descripcion("Segundo proceso")
                .categoria("General")
                .borrador(false)
                .activo(true)
                .build());
        Proceso proceso2 = IntegrationTestData.cargarProceso(procesoRepository, proceso2Dto.getId());

        MensajeThrow mensaje = new MensajeThrow();
        mensaje.setProceso(proceso);
        mensaje.setNombreMensaje("Original");
        mensaje.setPayloadTemplate("{\"data\": \"original\"}");
        mensaje.setEliminado(false);
        mensaje = mensajeThrowRepository.save(mensaje);

        MensajeThrowRequestDTO dto = MensajeThrowRequestDTO.builder()
                .procesoId(proceso2.getId())
                .nombreMensaje("Actualizado")
                .payloadTemplate("{\"data\": \"test\"}")
                .build();

        MensajeThrowResponseDTO result = mensajeThrowService.actualizar(nitEmpresa, mensaje.getId(), dto);

        assertEquals(proceso2.getId(), result.getProcesoId());
    }

    @Test
    @DisplayName("Eliminar mensaje exitosamente")
    void testEliminar_Success() {
        MensajeThrow mensaje = new MensajeThrow();
        mensaje.setProceso(proceso);
        mensaje.setNombreMensaje("A Eliminar");
        mensaje.setPayloadTemplate("{\"data\": \"delete\"}");
        mensaje.setEliminado(false);
        mensaje = mensajeThrowRepository.save(mensaje);

        mensajeThrowService.eliminar(nitEmpresa, mensaje.getId());

        // Verificar que está marcado como eliminado
        MensajeThrow eliminado = mensajeThrowRepository.findById(mensaje.getId()).orElse(null);
        assertNotNull(eliminado);
        assertTrue(eliminado.isEliminado());
    }

    @Test
    @DisplayName("Fallar al eliminar si mensaje no existe")
    void testEliminar_NotFound() {
        assertThrows(BusinessException.class, () -> mensajeThrowService.eliminar(nitEmpresa, 9999L));
    }
}
