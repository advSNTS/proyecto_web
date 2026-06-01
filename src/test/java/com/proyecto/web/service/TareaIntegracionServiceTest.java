package com.proyecto.web.service;

import com.proyecto.web.dto.TareaIntegracionRequestDTO;
import com.proyecto.web.dto.TareaIntegracionResponseDTO;
import com.proyecto.web.entity.MensajeExterno;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.entity.TareaIntegracion;
import com.proyecto.web.enums.TipoDestinoMensajeExterno;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.EmpresaRepository;
import com.proyecto.web.repository.MensajeExternoRepository;
import com.proyecto.web.repository.PoolRepository;
import com.proyecto.web.repository.ProcesoRepository;
import com.proyecto.web.repository.TareaIntegracionRepository;
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
@DisplayName("TareaIntegracionService Tests")
class TareaIntegracionServiceTest {

    @Autowired
    private TareaIntegracionService tareaIntegracionService;

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
    private MensajeExternoRepository mensajeExternoRepository;

    @Autowired
    private TareaIntegracionRepository tareaIntegracionRepository;

    private Proceso proceso;
    private MensajeExterno mensajeExterno;
    private String nitEmpresa = "123456789";

    @BeforeEach
    void setUp() {
        var creado = IntegrationTestData.crearEmpresaYProceso(empresaService, procesoService, nitEmpresa);
        proceso = IntegrationTestData.cargarProceso(procesoRepository, creado.getId());

        // Crear mensaje externo
        mensajeExterno = new MensajeExterno();
        mensajeExterno.setDestinoTipo(TipoDestinoMensajeExterno.HTTP);
        mensajeExterno.setConfiguracion("{\"config\": \"test\"}");
        mensajeExterno.setCredenciales("{\"cred\": \"test\"}");
        mensajeExterno.setEliminado(false);
        mensajeExternoRepository.save(mensajeExterno);
    }

    @Test
    @DisplayName("Crear tarea integración exitosamente")
    void testCrear_Success() {
        TareaIntegracionRequestDTO dto = TareaIntegracionRequestDTO.builder()
                .procesoId(proceso.getId())
                .mensajeExternoId(mensajeExterno.getId())
                .payloadMapping("{\"mapping\": \"value\"}")
                .build();

        TareaIntegracionResponseDTO result = tareaIntegracionService.crear( dto);

        assertNotNull(result.getId());
        assertEquals("{\"mapping\": \"value\"}", result.getPayloadMapping());
        assertEquals(proceso.getId(), result.getProcesoId());
        assertEquals(mensajeExterno.getId(), result.getMensajeExternoId());
    }

    @Test
    @DisplayName("Fallar si proceso no existe")
    void testCrear_ProcesoNoExiste() {
        TareaIntegracionRequestDTO dto = TareaIntegracionRequestDTO.builder()
                .procesoId(9999L)
                .mensajeExternoId(mensajeExterno.getId())
                .payloadMapping("{\"mapping\": \"value\"}")
                .build();

        assertThrows(BusinessException.class, () -> tareaIntegracionService.crear( dto));
    }

    @Test
    @DisplayName("Fallar si mensaje externo no existe")
    void testCrear_MensajeExternoNoExiste() {
        TareaIntegracionRequestDTO dto = TareaIntegracionRequestDTO.builder()
                .procesoId(proceso.getId())
                .mensajeExternoId(9999L)
                .payloadMapping("{\"mapping\": \"value\"}")
                .build();

        assertThrows(BusinessException.class, () -> tareaIntegracionService.crear( dto));
    }

    @Test
    @DisplayName("Fallar si proceso no es vigente")
    void testCrear_ProcesoNoVigente() {
        Proceso procesoNoVigente = IntegrationTestData.crearProcesoInactivo(
                procesoRepository, empresaRepository, poolRepository, nitEmpresa, "Proceso No Vigente");

        TareaIntegracionRequestDTO dto = TareaIntegracionRequestDTO.builder()
                .procesoId(procesoNoVigente.getId())
                .mensajeExternoId(mensajeExterno.getId())
                .payloadMapping("{\"mapping\": \"value\"}")
                .build();

        assertThrows(BusinessException.class, () -> tareaIntegracionService.crear( dto));
    }

    @Test
    @DisplayName("Fallar si mensaje externo está eliminado")
    void testCrear_MensajeExternoEliminado() {
        // Marcar mensaje como eliminado
        mensajeExterno.setEliminado(true);
        mensajeExternoRepository.save(mensajeExterno);

        TareaIntegracionRequestDTO dto = TareaIntegracionRequestDTO.builder()
                .procesoId(proceso.getId())
                .mensajeExternoId(mensajeExterno.getId())
                .payloadMapping("{\"mapping\": \"value\"}")
                .build();

        assertThrows(BusinessException.class, () -> tareaIntegracionService.crear( dto));
    }

    @Test
    @DisplayName("Listar tareas por proceso exitosamente")
    void testListarPorProceso_Success() {
        // Crear varias tareas
        TareaIntegracion tarea1 = new TareaIntegracion();
        tarea1.setProceso(proceso);
        tarea1.setMensajeExterno(mensajeExterno);
        tarea1.setPayloadMapping("{\"mapping\": \"1\"}");
        tarea1.setEliminado(false);
        tareaIntegracionRepository.save(tarea1);

        TareaIntegracion tarea2 = new TareaIntegracion();
        tarea2.setProceso(proceso);
        tarea2.setMensajeExterno(mensajeExterno);
        tarea2.setPayloadMapping("{\"mapping\": \"2\"}");
        tarea2.setEliminado(false);
        tareaIntegracionRepository.save(tarea2);

        List<TareaIntegracionResponseDTO> result = tareaIntegracionService.listarPorProceso( proceso.getId());

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Listar tareas retorna vacío si no hay")
    void testListarPorProceso_Empty() {
        List<TareaIntegracionResponseDTO> result = tareaIntegracionService.listarPorProceso( proceso.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("No lista tareas eliminadas")
    void testListarPorProceso_NoEliminadas() {
        // Crear tarea activa
        TareaIntegracion activa = new TareaIntegracion();
        activa.setProceso(proceso);
        activa.setMensajeExterno(mensajeExterno);
        activa.setPayloadMapping("{\"mapping\": \"1\"}");
        activa.setEliminado(false);
        tareaIntegracionRepository.save(activa);

        // Crear tarea eliminada
        TareaIntegracion eliminada = new TareaIntegracion();
        eliminada.setProceso(proceso);
        eliminada.setMensajeExterno(mensajeExterno);
        eliminada.setPayloadMapping("{\"mapping\": \"2\"}");
        eliminada.setEliminado(true);
        tareaIntegracionRepository.save(eliminada);

        List<TareaIntegracionResponseDTO> result = tareaIntegracionService.listarPorProceso( proceso.getId());

        assertEquals(1, result.size());
        assertEquals("{\"mapping\": \"1\"}", result.get(0).getPayloadMapping());
    }

    @Test
    @DisplayName("Obtener tarea por ID exitosamente")
    void testObtener_Success() {
        TareaIntegracion tarea = new TareaIntegracion();
        tarea.setProceso(proceso);
        tarea.setMensajeExterno(mensajeExterno);
        tarea.setPayloadMapping("{\"mapping\": \"test\"}");
        tarea.setEliminado(false);
        tarea = tareaIntegracionRepository.save(tarea);

        TareaIntegracionResponseDTO result = tareaIntegracionService.obtener( tarea.getId());

        assertNotNull(result);
        assertEquals(tarea.getId(), result.getId());
        assertEquals("{\"mapping\": \"test\"}", result.getPayloadMapping());
    }

    @Test
    @DisplayName("Fallar si tarea no existe")
    void testObtener_NotFound() {
        assertThrows(BusinessException.class, () -> tareaIntegracionService.obtener( 9999L));
    }

    @Test
    @DisplayName("Fallar si tarea está eliminada")
    void testObtener_Eliminada() {
        TareaIntegracion tarea = new TareaIntegracion();
        tarea.setProceso(proceso);
        tarea.setMensajeExterno(mensajeExterno);
        tarea.setPayloadMapping("{\"mapping\": \"test\"}");
        tarea.setEliminado(true);
        Long tareaId = tareaIntegracionRepository.save(tarea).getId();

        assertThrows(BusinessException.class, () -> tareaIntegracionService.obtener( tareaId));
    }

    @Test
    @DisplayName("Actualizar tarea exitosamente")
    void testActualizar_Success() {
        TareaIntegracion tarea = new TareaIntegracion();
        tarea.setProceso(proceso);
        tarea.setMensajeExterno(mensajeExterno);
        tarea.setPayloadMapping("{\"mapping\": \"original\"}");
        tarea.setEliminado(false);
        tarea = tareaIntegracionRepository.save(tarea);

        TareaIntegracionRequestDTO dto = TareaIntegracionRequestDTO.builder()
                .procesoId(proceso.getId())
                .mensajeExternoId(mensajeExterno.getId())
                .payloadMapping("{\"mapping\": \"actualizado\"}")
                .build();

        TareaIntegracionResponseDTO result = tareaIntegracionService.actualizar( tarea.getId(), dto);

        
        assertEquals("{\"mapping\": \"actualizado\"}", result.getPayloadMapping());
    }

    @Test
    @DisplayName("Fallar al actualizar si tarea no existe")
    void testActualizar_NotFound() {
        TareaIntegracionRequestDTO dto = TareaIntegracionRequestDTO.builder()
                .procesoId(proceso.getId())
                .mensajeExternoId(mensajeExterno.getId())
                .payloadMapping("{\"mapping\": \"test\"}")
                .build();

        assertThrows(BusinessException.class, () -> tareaIntegracionService.actualizar( 9999L, dto));
    }

    @Test
    @DisplayName("Cambiar mensaje externo al actualizar")
    void testActualizar_CambiarMensajeExterno() {
        // Crear segundo mensaje externo
        MensajeExterno mensajeExterno2 = new MensajeExterno();
        mensajeExterno2.setDestinoTipo(TipoDestinoMensajeExterno.COLA);
        mensajeExterno2.setConfiguracion("{\"config\": \"test2\"}");
        mensajeExterno2.setCredenciales("{\"cred\": \"test2\"}");
        mensajeExterno2.setEliminado(false);
        mensajeExternoRepository.save(mensajeExterno2);

        TareaIntegracion tarea = new TareaIntegracion();
        tarea.setProceso(proceso);
        tarea.setMensajeExterno(mensajeExterno);
        tarea.setPayloadMapping("{\"mapping\": \"original\"}");
        tarea.setEliminado(false);
        tarea = tareaIntegracionRepository.save(tarea);

        TareaIntegracionRequestDTO dto = TareaIntegracionRequestDTO.builder()
                .procesoId(proceso.getId())
                .mensajeExternoId(mensajeExterno2.getId())
                .payloadMapping("{\"mapping\": \"test\"}")
                .build();

        TareaIntegracionResponseDTO result = tareaIntegracionService.actualizar( tarea.getId(), dto);

        assertEquals(mensajeExterno2.getId(), result.getMensajeExternoId());
    }

    @Test
    @DisplayName("Eliminar tarea exitosamente")
    void testEliminar_Success() {
        TareaIntegracion tarea = new TareaIntegracion();
        tarea.setProceso(proceso);
        tarea.setMensajeExterno(mensajeExterno);
        tarea.setPayloadMapping("{\"mapping\": \"delete\"}");
        tarea.setEliminado(false);
        tarea = tareaIntegracionRepository.save(tarea);

        tareaIntegracionService.eliminar( tarea.getId());

        // Verificar que está marcada como eliminada
        TareaIntegracion eliminada = tareaIntegracionRepository.findById(tarea.getId()).orElse(null);
        assertNotNull(eliminada);
        assertTrue(eliminada.isEliminado());
    }

    @Test
    @DisplayName("Fallar al eliminar si tarea no existe")
    void testEliminar_NotFound() {
        assertThrows(BusinessException.class, () -> tareaIntegracionService.eliminar( 9999L));
    }
}
