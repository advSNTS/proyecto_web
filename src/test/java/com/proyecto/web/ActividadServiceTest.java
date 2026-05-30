package com.proyecto.web;

import com.proyecto.web.dto.ActividadRequestDTO;
import com.proyecto.web.dto.ActividadResponseDTO;
import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.NodoRequestDTO;
import com.proyecto.web.dto.NodoResponseDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.enums.TipoNodo;
import com.proyecto.web.service.ActividadService;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.NodoService;
import com.proyecto.web.exception.BusinessException;
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

    private static final String NIT = "900ACT-SVC-1";

    @Autowired
    private ActividadService actividadService;

    @Autowired
    private NodoService nodoService;

    @Autowired
    private ProcesoService procesoService;

    @Autowired
    private EmpresaService empresaService;

    private Long procesoId;
    private Long nodoId;

    @BeforeEach
    void setUp() {
        EmpresaRequestDTO emp = new EmpresaRequestDTO();
        emp.setNit(NIT);
        emp.setNombre("Emp Act");
        emp.setCorreo("ea@test.com");
        empresaService.crearEmpresa(emp);

        ProcesoRequestDTO procesoDTO = ProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("Proceso Test")
                .descripcion("Descripción proceso test")
                .categoria("Categoría A")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO proceso = procesoService.crearProceso(procesoDTO);
        this.procesoId = proceso.getId();

        NodoRequestDTO nodoDTO = NodoRequestDTO.builder()
                .nitEmpresa(NIT)
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Actividad 1")
                .coordenadaX(0L)
                .coordenadaY(0L)
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

        NodoRequestDTO nodoDTO2 = NodoRequestDTO.builder()
                .nitEmpresa(NIT)
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Actividad 2")
                .coordenadaX(0L)
                .coordenadaY(0L)
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

        Long id = creada.getId();
        assertThrows(BusinessException.class, () -> actividadService.obtenerActividad(id));
    }

    @Test
    void crearActividad_nodoConActividadExistente_deberiaLanzarBusinessException() {
        ActividadRequestDTO dto = ActividadRequestDTO.builder()
                .nodoId(nodoId)
                .descripcion("Primera actividad")
                .build();
        actividadService.crearActividad(dto);

        ActividadRequestDTO duplicada = ActividadRequestDTO.builder()
                .nodoId(nodoId)
                .descripcion("Segunda actividad")
                .build();

        assertThrows(BusinessException.class, () -> actividadService.crearActividad(duplicada));
    }

    @Test
    void actualizarActividad_cambiandoNodo_deberiaActualizarNodo() {
        ActividadRequestDTO dto = ActividadRequestDTO.builder()
                .nodoId(nodoId)
                .descripcion("Actividad original")
                .build();
        ActividadResponseDTO creada = actividadService.crearActividad(dto);

        NodoRequestDTO nodoDTO2 = NodoRequestDTO.builder()
                .nitEmpresa(NIT)
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Actividad 2")
                .coordenadaX(10L)
                .coordenadaY(10L)
                .build();
        NodoResponseDTO nodo2 = nodoService.crearNodo(nodoDTO2);

        ActividadRequestDTO update = ActividadRequestDTO.builder()
                .nodoId(nodo2.getId())
                .descripcion("Actividad movida")
                .build();

        ActividadResponseDTO actualizada =
                actividadService.actualizarActividad(creada.getId(), update, null);

        assertEquals(nodo2.getId(), actualizada.getNodoId());
        assertEquals("Actividad movida", actualizada.getDescripcion());
    }

    @Test
    void obtenerHistorial_deberiaRetornarCambios() {
        ActividadRequestDTO dto = ActividadRequestDTO.builder()
                .nodoId(nodoId)
                .descripcion("Actividad historial")
                .build();
        ActividadResponseDTO creada = actividadService.crearActividad(dto);

        ActividadRequestDTO update = ActividadRequestDTO.builder()
                .nodoId(nodoId)
                .descripcion("Actividad historial actualizada")
                .build();
        actividadService.actualizarActividad(creada.getId(), update, null);

        var historial = actividadService.obtenerHistorial(creada.getId());

        assertFalse(historial.isEmpty());
    }
}