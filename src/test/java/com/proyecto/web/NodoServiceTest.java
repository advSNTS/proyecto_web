package com.proyecto.web;

import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.NodoRequestDTO;
import com.proyecto.web.dto.NodoResponseDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.enums.TipoNodo;
import com.proyecto.web.service.EmpresaService;
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

    private static final String NIT = "900NODO-SVC-1";

    @Autowired
    private NodoService nodoService;

    @Autowired
    private ProcesoService procesoService;

    @Autowired
    private EmpresaService empresaService;

    private Long procesoId;

    @BeforeEach
    void setUp() {
        EmpresaRequestDTO emp = new EmpresaRequestDTO();
        emp.setNit(NIT);
        emp.setNombre("Emp Nodo");
        emp.setCorreo("en@test.com");
        empresaService.crearEmpresa(emp);

        ProcesoRequestDTO procesoDTO = ProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("Proceso Base Nodos")
                .descripcion("Proceso para pruebas de nodos")
                .categoria("Categoría Nodos")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO proceso = procesoService.crearProceso(procesoDTO);
        this.procesoId = proceso.getId();
    }

    private NodoRequestDTO nodo(TipoNodo tipo, String nombre, long x, long y) {
        return NodoRequestDTO.builder()
                .nitEmpresa(NIT)
                .idProceso(procesoId)
                .tipo(tipo)
                .nombre(nombre)
                .coordenadaX(x)
                .coordenadaY(y)
                .build();
    }

    @Test
    void crearNodo_deberiaRetornarNodoCreado() {
        NodoRequestDTO dto = nodo(TipoNodo.ACTIVIDAD, "Nodo Actividad 001", 0L, 0L);

        NodoResponseDTO response = nodoService.crearNodo(dto);

        assertNotNull(response);
        assertEquals("Nodo Actividad 001", response.getNombre());
        assertEquals(TipoNodo.ACTIVIDAD, response.getTipo());
        assertNotNull(response.getId());
    }

    @Test
    void obtenerNodo_deberiaRetornarNodoExistente() {
        NodoRequestDTO dto = nodo(TipoNodo.GATEWAY, "Nodo Gateway 001", 0L, 0L);

        NodoResponseDTO creado = nodoService.crearNodo(dto);

        NodoResponseDTO response = nodoService.obtenerNodo(creado.getId());

        assertNotNull(response);
        assertEquals("Nodo Gateway 001", response.getNombre());
        assertEquals(TipoNodo.GATEWAY, response.getTipo());
        assertEquals(creado.getId(), response.getId());
    }

    @Test
    void obtenerPorProceso_deberiaRetornarNodosDelProceso() {
        nodoService.crearNodo(nodo(TipoNodo.ACTIVIDAD, "Nodo Uno", 0L, 0L));
        nodoService.crearNodo(nodo(TipoNodo.ARCO, "Nodo Dos", 0L, 0L));

        List<NodoResponseDTO> lista = nodoService.obtenerPorProceso(procesoId, NIT);

        assertNotNull(lista);
        assertTrue(lista.size() >= 2);
        assertTrue(lista.stream().anyMatch(n -> n.getNombre().equals("Nodo Uno")));
    }

    @Test
    void obtenerPorProcesoYTipo_deberiaRetornarNodosDelTipo() {
        nodoService.crearNodo(nodo(TipoNodo.ACTIVIDAD, "Actividad 001", 0L, 0L));
        nodoService.crearNodo(nodo(TipoNodo.ACTIVIDAD, "Actividad 002", 0L, 0L));
        nodoService.crearNodo(nodo(TipoNodo.GATEWAY, "Gateway 001", 0L, 0L));

        List<NodoResponseDTO> listaActividades = nodoService.obtenerPorProcesoYTipo(procesoId, TipoNodo.ACTIVIDAD, NIT);

        assertNotNull(listaActividades);
        assertTrue(listaActividades.size() >= 2);
        assertTrue(listaActividades.stream().allMatch(n -> n.getTipo() == TipoNodo.ACTIVIDAD));
    }

    @Test
    void actualizarNodo_deberiaRetornarNodoActualizado() {
        NodoResponseDTO creado = nodoService.crearNodo(nodo(TipoNodo.ACTIVIDAD, "Nodo Original", 0L, 0L));

        NodoRequestDTO update = nodo(TipoNodo.GATEWAY, "Nodo Actualizado", 10L, 20L);

        NodoResponseDTO response = nodoService.actualizarNodo(creado.getId(), update);

        assertEquals("Nodo Actualizado", response.getNombre());
        assertEquals(TipoNodo.GATEWAY, response.getTipo());
    }

    @Test
    void eliminarNodo_deberiaMarcarEliminado() {
        NodoResponseDTO creado = nodoService.crearNodo(nodo(TipoNodo.ACTIVIDAD, "Nodo Para Eliminar", 0L, 0L));

        nodoService.eliminarNodo(creado.getId(), NIT);

        Long id = creado.getId();
        assertThrows(RuntimeException.class, () -> nodoService.obtenerNodo(id));
    }

    @Test
    void obtenerPorProcesoYTipo_gateway_deberiaRetornarGateways() {
        nodoService.crearNodo(nodo(TipoNodo.GATEWAY, "Gateway Test", 0L, 0L));

        List<NodoResponseDTO> listaGateways = nodoService.obtenerPorProcesoYTipo(procesoId, TipoNodo.GATEWAY, NIT);

        assertNotNull(listaGateways);
        assertFalse(listaGateways.isEmpty());
        assertTrue(listaGateways.stream().allMatch(n -> n.getTipo() == TipoNodo.GATEWAY));
    }

    @Test
    void crearMultiplesNodos_deberiaCrearTodosExitosamente() {
        nodoService.crearNodo(nodo(TipoNodo.ACTIVIDAD, "Inicio", 0L, 0L));
        nodoService.crearNodo(nodo(TipoNodo.ARCO, "Transición", 0L, 0L));
        nodoService.crearNodo(nodo(TipoNodo.ACTIVIDAD, "Fin", 0L, 0L));

        List<NodoResponseDTO> lista = nodoService.obtenerPorProceso(procesoId, NIT);
        assertTrue(lista.size() >= 3);
    }
}
