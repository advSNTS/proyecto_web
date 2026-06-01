package com.proyecto.web;

import com.proyecto.web.dto.ArcoRequestDTO;
import com.proyecto.web.dto.ArcoResponseDTO;
import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.NodoRequestDTO;
import com.proyecto.web.dto.NodoResponseDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.enums.TipoNodo;
import com.proyecto.web.service.ArcoService;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.NodoService;
import com.proyecto.web.service.ProcesoService;
import com.proyecto.web.support.TestSecurityContext;
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
class ArcoServiceTest {

    private static final String NIT = "900ARCO-SVC-1";

    @Autowired
    private ArcoService arcoService;

    @Autowired
    private NodoService nodoService;

    @Autowired
    private ProcesoService procesoService;

    @Autowired
    private EmpresaService empresaService;

    private Long procesoId;
    private Long nodoOrigenId;
    private Long nodoDestinoId;

    @BeforeEach
    void setUp() {
        EmpresaRequestDTO emp = new EmpresaRequestDTO();
        emp.setNit(NIT);
        emp.setNombre("Emp Arco");
        emp.setCorreo("ea@test.com");
        empresaService.crearEmpresa(emp);
        TestSecurityContext.authenticate(NIT);

        ProcesoRequestDTO procesoDTO = ProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("Proceso Arco Test")
                .descripcion("Descripción proceso arco")
                .categoria("Categoría B")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO proceso = procesoService.crearProceso(procesoDTO);
        this.procesoId = proceso.getId();

        NodoRequestDTO nodoOrigen = NodoRequestDTO.builder()
                .nitEmpresa(NIT)
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Origen")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        NodoResponseDTO origen = nodoService.crearNodo(nodoOrigen);
        this.nodoOrigenId = origen.getId();

        NodoRequestDTO nodoDestino = NodoRequestDTO.builder()
                .nitEmpresa(NIT)
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Destino")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        NodoResponseDTO destino = nodoService.crearNodo(nodoDestino);
        this.nodoDestinoId = destino.getId();
    }

    private ArcoRequestDTO arco(Long destinoId) {
        return ArcoRequestDTO.builder()
                .nitEmpresa(NIT)
                .idProceso(procesoId)
                .nodoOrigenId(nodoOrigenId)
                .nodoDestinoId(destinoId)
                .build();
    }

    @Test
    void crearArco_deberiaRetornarArcoCreado() {
        ArcoResponseDTO response = arcoService.crearArco(arco(nodoDestinoId));

        assertNotNull(response);
        assertEquals(nodoOrigenId, response.getNodoOrigenId());
        assertEquals(nodoDestinoId, response.getNodoDestinoId());
        assertNotNull(response.getId());
    }

    @Test
    void obtenerArco_deberiaRetornarArcoExistente() {
        ArcoResponseDTO creado = arcoService.crearArco(arco(nodoDestinoId));

        ArcoResponseDTO response = arcoService.obtenerArco(creado.getId());

        assertNotNull(response);
        assertEquals(creado.getId(), response.getId());
    }

    @Test
    void obtenerPorProceso_deberiaRetornarArcosDelProceso() {
        arcoService.crearArco(arco(nodoDestinoId));

        List<ArcoResponseDTO> lista = arcoService.obtenerPorProceso(procesoId, NIT);

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
    }

    @Test
    void obtenerSalientesDe_deberiaRetornarArcosSalientes() {
        arcoService.crearArco(arco(nodoDestinoId));

        List<ArcoResponseDTO> lista = arcoService.obtenerSalientesDe(nodoOrigenId);

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(a -> a.getNodoOrigenId().equals(nodoOrigenId)));
    }

    @Test
    void obtenerEntrantesA_deberiaRetornarArcosEntrantes() {
        arcoService.crearArco(arco(nodoDestinoId));

        List<ArcoResponseDTO> lista = arcoService.obtenerEntrantesA(nodoDestinoId);

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(a -> a.getNodoDestinoId().equals(nodoDestinoId)));
    }

    @Test
    void actualizarArco_deberiaRetornarArcoActualizado() {
        NodoRequestDTO nodoDestino3 = NodoRequestDTO.builder()
                .nitEmpresa(NIT)
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Destino 3")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        NodoResponseDTO destino3 = nodoService.crearNodo(nodoDestino3);

        ArcoResponseDTO creado = arcoService.crearArco(arco(nodoDestinoId));

        ArcoRequestDTO update = arco(destino3.getId());

        ArcoResponseDTO response = arcoService.actualizarArco(creado.getId(), update);

        assertEquals(destino3.getId(), response.getNodoDestinoId());
    }

    @Test
    void eliminarArco_deberiaMarcarComoEliminado() {
        ArcoResponseDTO creado = arcoService.crearArco(arco(nodoDestinoId));

        arcoService.eliminarArco(creado.getId());

        Long id = creado.getId();
        assertThrows(RuntimeException.class, () -> arcoService.obtenerArco(id));
    }

    @Test
    void crearArco_conOrigenIgualADestino_deberiaLanzarBusinessException() {
        ArcoRequestDTO dto = arco(nodoOrigenId);

        assertThrows(Exception.class, () -> arcoService.crearArco(dto));
    }
}
