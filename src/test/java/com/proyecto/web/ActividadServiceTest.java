package com.proyecto.web;

import com.proyecto.web.dto.ActividadRequestDTO;
import com.proyecto.web.dto.ActividadResponseDTO;
import com.proyecto.web.dto.NodoRequestDTO;
import com.proyecto.web.dto.NodoResponseDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.enums.TipoNodo;
import com.proyecto.web.service.ActividadService;
import com.proyecto.web.service.NodoService;
import com.proyecto.web.service.ProcesoService;
import org.junit.jupiter.api.BeforeEach;
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
class ActividadServiceTest {

    @Autowired
    private ActividadService actividadService;

    @Autowired
    private NodoService nodoService;

    @Autowired
    private ProcesoService procesoService;

    private Long procesoId;
    private Long nodoId;

    @BeforeEach
    void setUp() {
        // Crear proceso
        ProcesoRequestDTO procesoDTO = ProcesoRequestDTO.builder()
                .nombre("Proceso Test")
                .descripcion("Descripción proceso test")
                .categoria("Categoría A")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO proceso = procesoService.crearProceso(procesoDTO);
        this.procesoId = proceso.getId();

        // Crear nodo de tipo ACTIVIDAD
        NodoRequestDTO nodoDTO = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Actividad 1")
                .build();

        NodoResponseDTO nodo = nodoService.crearNodo(nodoDTO);
        this.nodoId = nodo.getId();
    }

    @Test
    void crearActividad_deberiaRetornarActividadCreada() {
        ActividadRequestDTO dto = ActividadRequestDTO.builder()
                .nodoId(nodoId)
                .descripcion("Actividad de prueba")
                .build();

        ActividadResponseDTO response = actividadService.crearActividad(dto);

        assertNotNull(response);
        assertEquals("Actividad de prueba", response.getDescripcion());
        assertNotNull(response.getId());
    }

    @Test
    void obtenerActividad_deberiaRetornarActividadExistente() {
        ActividadRequestDTO dto = ActividadRequestDTO.builder()
                .nodoId(nodoId)
                .descripcion("Actividad para obtener")
                .build();

        ActividadResponseDTO creada = actividadService.crearActividad(dto);

        ActividadResponseDTO response = actividadService.obtenerActividad(creada.getId());

        assertNotNull(response);
        assertEquals("Actividad para obtener", response.getDescripcion());
        assertEquals(creada.getId(), response.getId());
    }

    @Test
    void obtenerPorProceso_deberiaRetornarActividadesProceso() {
        ActividadRequestDTO dto1 = ActividadRequestDTO.builder()
                .nodoId(nodoId)
                .descripcion("Actividad Uno")
                .build();

        // Crear otro nodo para la segunda actividad
        NodoRequestDTO nodoDTO2 = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Actividad 2")
                .build();

        NodoResponseDTO nodo2 = nodoService.crearNodo(nodoDTO2);

        ActividadRequestDTO dto2 = ActividadRequestDTO.builder()
                .nodoId(nodo2.getId())
                .descripcion("Actividad Dos")
                .build();

        actividadService.crearActividad(dto1);
        actividadService.crearActividad(dto2);

        List<ActividadResponseDTO> lista = actividadService.obtenerPorProceso(procesoId);

        assertNotNull(lista);
        assertTrue(lista.size() >= 2);
    }

    @Test
    void actualizarActividad_deberiaRetornarActividadActualizada() {
        ActividadRequestDTO dto = ActividadRequestDTO.builder()
                .nodoId(nodoId)
                .descripcion("Descripción Original")
                .build();

        ActividadResponseDTO creada = actividadService.crearActividad(dto);

        ActividadRequestDTO update = ActividadRequestDTO.builder()
                .nodoId(nodoId)
                .descripcion("Descripción Actualizada")
                .build();

        ActividadResponseDTO response = actividadService.actualizarActividad(creada.getId(), update, null);

        assertEquals("Descripción Actualizada", response.getDescripcion());
    }

    @Test
    void eliminarActividad_deberiaMarcarComoEliminada() {
        ActividadRequestDTO dto = ActividadRequestDTO.builder()
                .nodoId(nodoId)
                .descripcion("Actividad para eliminar")
                .build();

        ActividadResponseDTO creada = actividadService.crearActividad(dto);

        actividadService.eliminarActividad(creada.getId(), null);

        assertThrows(RuntimeException.class, () -> actividadService.obtenerActividad(creada.getId()));
    }
}
