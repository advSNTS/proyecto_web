package com.proyecto.web.service;

import com.proyecto.web.dto.MensajeCatchRequestDTO;
import com.proyecto.web.dto.MensajeCatchResponseDTO;
import com.proyecto.web.entity.MensajeCatch;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.EmpresaRepository;
import com.proyecto.web.repository.MensajeCatchRepository;
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
@DisplayName("MensajeCatchService Tests")
class MensajeCatchServiceTest {

    @Autowired
    private MensajeCatchService mensajeCatchService;

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
    private MensajeCatchRepository mensajeCatchRepository;

    private Proceso proceso;
    private String nitEmpresa = "123456789";

    @BeforeEach
    void setUp() {
        var creado = IntegrationTestData.crearEmpresaYProceso(empresaService, procesoService, nitEmpresa);
        proceso = IntegrationTestData.cargarProceso(procesoRepository, creado.getId());
    }

    @Test
    @DisplayName("Crear mensaje catch exitosamente")
    void testCrear_Success() {
        MensajeCatchRequestDTO dto = MensajeCatchRequestDTO.builder()
                .procesoId(proceso.getId())
                .nombreMensaje("Mensaje Test")
                .correlacionExpr("expr")
                .iniciarNuevaInstancia(true)
                .build();

        MensajeCatchResponseDTO result = mensajeCatchService.crear( dto);

        assertNotNull(result.getId());
        assertEquals("Mensaje Test", result.getNombreMensaje());
        assertEquals("expr", result.getCorrelacionExpr());
        assertTrue(result.isIniciarNuevaInstancia());
    }

    @Test
    @DisplayName("Fallar si proceso no existe")
    void testCrear_ProcesoNoExiste() {
        MensajeCatchRequestDTO dto = MensajeCatchRequestDTO.builder()
                .procesoId(9999L)
                .nombreMensaje("Mensaje Test")
                .correlacionExpr("expr")
                .iniciarNuevaInstancia(false)
                .build();

        assertThrows(BusinessException.class, () -> mensajeCatchService.crear( dto));
    }

    @Test
    @DisplayName("Fallar si proceso no es vigente")
    void testCrear_ProcesoNoVigente() {
        Proceso procesoNoVigente = IntegrationTestData.crearProcesoInactivo(
                procesoRepository, empresaRepository, poolRepository, nitEmpresa, "Proceso No Vigente");

        MensajeCatchRequestDTO dto = MensajeCatchRequestDTO.builder()
                .procesoId(procesoNoVigente.getId())
                .nombreMensaje("Mensaje Test")
                .correlacionExpr("expr")
                .iniciarNuevaInstancia(false)
                .build();

        assertThrows(BusinessException.class, () -> mensajeCatchService.crear( dto));
    }

    @Test
    @DisplayName("Listar mensajes por proceso exitosamente")
    void testListarPorProceso_Success() {
        // Crear varios mensajes
        MensajeCatch m1 = new MensajeCatch();
        m1.setProceso(proceso);
        m1.setNombreMensaje("Mensaje 1");
        m1.setCorrelacionExpr("expr1");
        m1.setIniciarNuevaInstancia(true);
        m1.setEliminado(false);
        mensajeCatchRepository.save(m1);

        MensajeCatch m2 = new MensajeCatch();
        m2.setProceso(proceso);
        m2.setNombreMensaje("Mensaje 2");
        m2.setCorrelacionExpr("expr2");
        m2.setIniciarNuevaInstancia(false);
        m2.setEliminado(false);
        mensajeCatchRepository.save(m2);

        List<MensajeCatchResponseDTO> result = mensajeCatchService.listarPorProceso( proceso.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(m -> "Mensaje 1".equals(m.getNombreMensaje())));
        assertTrue(result.stream().anyMatch(m -> "Mensaje 2".equals(m.getNombreMensaje())));
    }

    @Test
    @DisplayName("Listar mensajes retorna vacío si no hay")
    void testListarPorProceso_Empty() {
        List<MensajeCatchResponseDTO> result = mensajeCatchService.listarPorProceso( proceso.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("No lista mensajes eliminados")
    void testListarPorProceso_NoEliminados() {
        // Crear mensaje activo
        MensajeCatch activo = new MensajeCatch();
        activo.setProceso(proceso);
        activo.setNombreMensaje("Activo");
        activo.setCorrelacionExpr("expr");
        activo.setIniciarNuevaInstancia(false);
        activo.setEliminado(false);
        mensajeCatchRepository.save(activo);

        // Crear mensaje eliminado
        MensajeCatch eliminado = new MensajeCatch();
        eliminado.setProceso(proceso);
        eliminado.setNombreMensaje("Eliminado");
        eliminado.setCorrelacionExpr("expr");
        eliminado.setIniciarNuevaInstancia(false);
        eliminado.setEliminado(true);
        mensajeCatchRepository.save(eliminado);

        List<MensajeCatchResponseDTO> result = mensajeCatchService.listarPorProceso( proceso.getId());

        assertEquals(1, result.size());
        assertEquals("Activo", result.get(0).getNombreMensaje());
    }

    @Test
    @DisplayName("Obtener mensaje por ID exitosamente")
    void testObtener_Success() {
        MensajeCatch mensaje = new MensajeCatch();
        mensaje.setProceso(proceso);
        mensaje.setNombreMensaje("Mensaje Test");
        mensaje.setCorrelacionExpr("expr");
        mensaje.setIniciarNuevaInstancia(true);
        mensaje.setEliminado(false);
        mensaje = mensajeCatchRepository.save(mensaje);

        MensajeCatchResponseDTO result = mensajeCatchService.obtener( mensaje.getId());

        assertNotNull(result);
        assertEquals(mensaje.getId(), result.getId());
        assertEquals("Mensaje Test", result.getNombreMensaje());
    }

    @Test
    @DisplayName("Fallar si mensaje no existe")
    void testObtener_NotFound() {
        assertThrows(BusinessException.class, () -> mensajeCatchService.obtener( 9999L));
    }

    @Test
    @DisplayName("Fallar si mensaje está eliminado")
    void testObtener_Eliminado() {
        MensajeCatch mensaje = new MensajeCatch();
        mensaje.setProceso(proceso);
        mensaje.setNombreMensaje("Eliminado");
        mensaje.setCorrelacionExpr("expr");
        mensaje.setIniciarNuevaInstancia(false);
        mensaje.setEliminado(true);
        Long mensajeId = mensajeCatchRepository.save(mensaje).getId();

        assertThrows(BusinessException.class, () -> mensajeCatchService.obtener( mensajeId));
    }

    @Test
    @DisplayName("Actualizar mensaje exitosamente")
    void testActualizar_Success() {
        MensajeCatch mensaje = new MensajeCatch();
        mensaje.setProceso(proceso);
        mensaje.setNombreMensaje("Original");
        mensaje.setCorrelacionExpr("expr1");
        mensaje.setIniciarNuevaInstancia(false);
        mensaje.setEliminado(false);
        mensaje = mensajeCatchRepository.save(mensaje);

        MensajeCatchRequestDTO dto = MensajeCatchRequestDTO.builder()
                .procesoId(proceso.getId())
                .nombreMensaje("Actualizado")
                .correlacionExpr("expr2")
                .iniciarNuevaInstancia(true)
                .build();

        MensajeCatchResponseDTO result = mensajeCatchService.actualizar( mensaje.getId(), dto);

        assertEquals("Actualizado", result.getNombreMensaje());
        assertEquals("expr2", result.getCorrelacionExpr());
        assertTrue(result.isIniciarNuevaInstancia());
    }

    @Test
    @DisplayName("Fallar al actualizar si mensaje no existe")
    void testActualizar_NotFound() {
        MensajeCatchRequestDTO dto = MensajeCatchRequestDTO.builder()
                .procesoId(proceso.getId())
                .nombreMensaje("Actualizado")
                .correlacionExpr("expr")
                .iniciarNuevaInstancia(false)
                .build();

        assertThrows(BusinessException.class, () -> mensajeCatchService.actualizar( 9999L, dto));
    }

    @Test
    @DisplayName("Eliminar mensaje exitosamente")
    void testEliminar_Success() {
        MensajeCatch mensaje = new MensajeCatch();
        mensaje.setProceso(proceso);
        mensaje.setNombreMensaje("A Eliminar");
        mensaje.setCorrelacionExpr("expr");
        mensaje.setIniciarNuevaInstancia(false);
        mensaje.setEliminado(false);
        mensaje = mensajeCatchRepository.save(mensaje);

        mensajeCatchService.eliminar( mensaje.getId());

        // Verificar que está marcado como eliminado
        MensajeCatch eliminado = mensajeCatchRepository.findById(mensaje.getId()).orElse(null);
        assertNotNull(eliminado);
        assertTrue(eliminado.isEliminado());
    }

    @Test
    @DisplayName("Fallar al eliminar si mensaje no existe")
    void testEliminar_NotFound() {
        assertThrows(BusinessException.class, () -> mensajeCatchService.eliminar( 9999L));
    }

    @Test
    @DisplayName("Guardar con iniciarNuevaInstancia null como false")
    void testCrear_IniciarNuevaInstanciaNullComeFalse() {
        MensajeCatchRequestDTO dto = MensajeCatchRequestDTO.builder()
                .procesoId(proceso.getId())
                .nombreMensaje("Mensaje Test")
                .correlacionExpr("expr")
                .iniciarNuevaInstancia(null)
                .build();

        MensajeCatchResponseDTO result = mensajeCatchService.crear( dto);

        assertFalse(result.isIniciarNuevaInstancia());
    }
}
