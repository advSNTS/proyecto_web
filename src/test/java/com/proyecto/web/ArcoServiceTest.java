package com.proyecto.web;

import com.proyecto.web.dto.ArcoRequestDTO;
import com.proyecto.web.dto.ArcoResponseDTO;
import com.proyecto.web.dto.NodoRequestDTO;
import com.proyecto.web.dto.NodoResponseDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.enums.TipoNodo;
import com.proyecto.web.service.ArcoService;
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
class ArcoServiceTest {

    @Autowired
    private ArcoService arcoService;

    @Autowired
    private NodoService nodoService;

    @Autowired
    private ProcesoService procesoService;

    private Long procesoId;
    private Long nodoOrigenId;
    private Long nodoDestinoId;

    @BeforeEach
    void setUp() {
        // Crear proceso
        ProcesoRequestDTO procesoDTO = ProcesoRequestDTO.builder()
                .nombre("Proceso Arco Test")
                .descripcion("Descripción proceso arco")
                .categoria("Categoría B")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO proceso = procesoService.crearProceso(procesoDTO);
        this.procesoId = proceso.getId();

        // Crear nodo origen
        NodoRequestDTO nodoOrigen = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Origen")
                .build();

        NodoResponseDTO origen = nodoService.crearNodo(nodoOrigen);
        this.nodoOrigenId = origen.getId();

        // Crear nodo destino
        NodoRequestDTO nodoDestino = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Destino")
                .build();

        NodoResponseDTO destino = nodoService.crearNodo(nodoDestino);
        this.nodoDestinoId = destino.getId();
    }

    @Test
    void crearArco_deberiaRetornarArcoCreado() {
        ArcoRequestDTO dto = ArcoRequestDTO.builder()
                .idProceso(procesoId)
                .nodoOrigenId(nodoOrigenId)
                .nodoDestinoId(nodoDestinoId)
                .build();

        ArcoResponseDTO response = arcoService.crearArco(dto);

        assertNotNull(response);
        assertEquals(nodoOrigenId, response.getNodoOrigenId());
        assertEquals(nodoDestinoId, response.getNodoDestinoId());
        assertNotNull(response.getId());
    }

    @Test
    void obtenerArco_deberiaRetornarArcoExistente() {
        ArcoRequestDTO dto = ArcoRequestDTO.builder()
                .idProceso(procesoId)
                .nodoOrigenId(nodoOrigenId)
                .nodoDestinoId(nodoDestinoId)
                .build();

        ArcoResponseDTO creado = arcoService.crearArco(dto);

        ArcoResponseDTO response = arcoService.obtenerArco(creado.getId());

        assertNotNull(response);
        assertEquals(creado.getId(), response.getId());
    }

    @Test
    void obtenerPorProceso_deberiaRetornarArcosDelProceso() {
        ArcoRequestDTO dto = ArcoRequestDTO.builder()
                .idProceso(procesoId)
                .nodoOrigenId(nodoOrigenId)
                .nodoDestinoId(nodoDestinoId)
                .build();

        arcoService.crearArco(dto);

        List<ArcoResponseDTO> lista = arcoService.obtenerPorProceso(procesoId);

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
    }

    @Test
    void obtenerSalientesDe_deberiaRetornarArcosSalientes() {
        ArcoRequestDTO dto = ArcoRequestDTO.builder()
                .idProceso(procesoId)
                .nodoOrigenId(nodoOrigenId)
                .nodoDestinoId(nodoDestinoId)
                .build();

        arcoService.crearArco(dto);

        List<ArcoResponseDTO> lista = arcoService.obtenerSalientesDe(nodoOrigenId);

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(a -> a.getNodoOrigenId().equals(nodoOrigenId)));
    }

    @Test
    void obtenerEntrantesA_deberiaRetornarArcosEntrantes() {
        ArcoRequestDTO dto = ArcoRequestDTO.builder()
                .idProceso(procesoId)
                .nodoOrigenId(nodoOrigenId)
                .nodoDestinoId(nodoDestinoId)
                .build();

        arcoService.crearArco(dto);

        List<ArcoResponseDTO> lista = arcoService.obtenerEntrantesA(nodoDestinoId);

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(a -> a.getNodoDestinoId().equals(nodoDestinoId)));
    }

    @Test
    void actualizarArco_deberiaRetornarArcoActualizado() {
        // Crear tercer nodo para actualizar destino
        NodoRequestDTO nodoDestino3 = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Destino 3")
                .build();

        NodoResponseDTO destino3 = nodoService.crearNodo(nodoDestino3);

        ArcoRequestDTO dto = ArcoRequestDTO.builder()
                .idProceso(procesoId)
                .nodoOrigenId(nodoOrigenId)
                .nodoDestinoId(nodoDestinoId)
                .build();

        ArcoResponseDTO creado = arcoService.crearArco(dto);

        ArcoRequestDTO update = ArcoRequestDTO.builder()
                .idProceso(procesoId)
                .nodoOrigenId(nodoOrigenId)
                .nodoDestinoId(destino3.getId())
                .build();

        ArcoResponseDTO response = arcoService.actualizarArco(creado.getId(), update);

        assertEquals(destino3.getId(), response.getNodoDestinoId());
    }

    @Test
    void eliminarArco_deberiaMarcarComoEliminado() {
        ArcoRequestDTO dto = ArcoRequestDTO.builder()
                .idProceso(procesoId)
                .nodoOrigenId(nodoOrigenId)
                .nodoDestinoId(nodoDestinoId)
                .build();

        ArcoResponseDTO creado = arcoService.crearArco(dto);

        arcoService.eliminarArco(creado.getId());

        Long id = creado.getId();
        assertThrows(RuntimeException.class, () -> arcoService.obtenerArco(id));
    }
}
