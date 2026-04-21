package com.proyecto.web;

import com.proyecto.web.dto.NodoRequestDTO;
import com.proyecto.web.dto.NodoResponseDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.enums.TipoNodo;
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
class NodoServiceTest {

    @Autowired
    private NodoService nodoService;

    @Autowired
    private ProcesoService procesoService;

    private Long procesoId;

    @BeforeEach
    void setUp() {
        ProcesoRequestDTO procesoDTO = ProcesoRequestDTO.builder()
                .nombre("Proceso Base Nodos")
                .descripcion("Proceso para pruebas de nodos")
                .categoria("Categoría Nodos")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO proceso = procesoService.crearProceso(procesoDTO);
        this.procesoId = proceso.getId();
    }

    @Test
    void crearNodo_deberiaRetornarNodoCreado() {
        NodoRequestDTO dto = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Actividad 001")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        NodoResponseDTO response = nodoService.crearNodo(dto);

        assertNotNull(response);
        assertEquals("Nodo Actividad 001", response.getNombre());
        assertEquals(TipoNodo.ACTIVIDAD, response.getTipo());
        assertNotNull(response.getId());
    }

    @Test
    void obtenerNodo_deberiaRetornarNodoExistente() {
        NodoRequestDTO dto = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.GATEWAY)
                .nombre("Nodo Gateway 001")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        NodoResponseDTO creado = nodoService.crearNodo(dto);

        NodoResponseDTO response = nodoService.obtenerNodo(creado.getId());

        assertNotNull(response);
        assertEquals("Nodo Gateway 001", response.getNombre());
        assertEquals(TipoNodo.GATEWAY, response.getTipo());
        assertEquals(creado.getId(), response.getId());
    }

    @Test
    void obtenerPorProceso_deberiaRetornarNodosDelProceso() {
        NodoRequestDTO dto1 = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Uno")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        NodoRequestDTO dto2 = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.ARCO)
                .nombre("Nodo Dos")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        nodoService.crearNodo(dto1);
        nodoService.crearNodo(dto2);

        List<NodoResponseDTO> lista = nodoService.obtenerPorProceso(procesoId);

        assertNotNull(lista);
        assertTrue(lista.size() >= 2);
        assertTrue(lista.stream().anyMatch(n -> n.getNombre().equals("Nodo Uno")));
    }

    @Test
    void obtenerPorProcesoYTipo_deberiaRetornarNodosDelTipo() {
        NodoRequestDTO dto1 = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Actividad 001")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        NodoRequestDTO dto2 = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Actividad 002")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        NodoRequestDTO dto3 = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.GATEWAY)
                .nombre("Gateway 001")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        nodoService.crearNodo(dto1);
        nodoService.crearNodo(dto2);
        nodoService.crearNodo(dto3);

        List<NodoResponseDTO> listaActividades = nodoService.obtenerPorProcesoYTipo(procesoId, TipoNodo.ACTIVIDAD);

        assertNotNull(listaActividades);
        assertTrue(listaActividades.size() >= 2);
        assertTrue(listaActividades.stream().allMatch(n -> n.getTipo() == TipoNodo.ACTIVIDAD));
    }

    @Test
    void actualizarNodo_deberiaRetornarNodoActualizado() {
        NodoRequestDTO dto = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Original")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        NodoResponseDTO creado = nodoService.crearNodo(dto);

        NodoRequestDTO update = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.GATEWAY)
                .nombre("Nodo Actualizado")
                .coordenadaX(10L)
                .coordenadaY(20L)
                .build();

        NodoResponseDTO response = nodoService.actualizarNodo(creado.getId(), update);

        assertEquals("Nodo Actualizado", response.getNombre());
        assertEquals(TipoNodo.GATEWAY, response.getTipo());
    }

    @Test
    void eliminarNodo_deberiaEliminarNodoHardDelete() {
        NodoRequestDTO dto = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Para Eliminar")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        NodoResponseDTO creado = nodoService.crearNodo(dto);

        nodoService.eliminarNodo(creado.getId());

        Long id = creado.getId();
        assertThrows(RuntimeException.class, () -> nodoService.obtenerNodo(id));
    }

    @Test
    void obtenerPorProcesoYTipo_gateway_deberiaRetornarGateways() {
        NodoRequestDTO dtoGateway = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.GATEWAY)
                .nombre("Gateway Test")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        nodoService.crearNodo(dtoGateway);

        List<NodoResponseDTO> listaGateways = nodoService.obtenerPorProcesoYTipo(procesoId, TipoNodo.GATEWAY);

        assertNotNull(listaGateways);
        assertFalse(listaGateways.isEmpty());
        assertTrue(listaGateways.stream().allMatch(n -> n.getTipo() == TipoNodo.GATEWAY));
    }

    @Test
    void crearMultiplesNodos_deberiaCrearTodosExitosamente() {
        NodoRequestDTO dto1 = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Inicio")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        NodoRequestDTO dto2 = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.ARCO)
                .nombre("Transición")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        NodoRequestDTO dto3 = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Fin")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        NodoResponseDTO nodo1 = nodoService.crearNodo(dto1);
        NodoResponseDTO nodo2 = nodoService.crearNodo(dto2);
        NodoResponseDTO nodo3 = nodoService.crearNodo(dto3);

        assertNotNull(nodo1.getId());
        assertNotNull(nodo2.getId());
        assertNotNull(nodo3.getId());

        List<NodoResponseDTO> lista = nodoService.obtenerPorProceso(procesoId);
        assertTrue(lista.size() >= 3);
    }
}