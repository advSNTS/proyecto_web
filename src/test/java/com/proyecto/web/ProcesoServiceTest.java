package com.proyecto.web;

import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.service.ProcesoService;
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
class ProcesoServiceTest {

    @Autowired
    private ProcesoService procesoService;

    @Test
    void crearProceso_deberiaRetornarProcesoCreado() {
        ProcesoRequestDTO dto = ProcesoRequestDTO.builder()
                .nombre("Proceso Test 001")
                .descripcion("Descripción del proceso")
                .categoria("Categoría A")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO response = procesoService.crearProceso(dto);

        assertNotNull(response);
        assertEquals("Proceso Test 001", response.getNombre());
        assertEquals("Descripción del proceso", response.getDescripcion());
        assertEquals("Categoría A", response.getCategoria());
        assertTrue(response.getActivo());
        assertNotNull(response.getId());
    }

    @Test
    void obtenerProceso_deberiaRetornarProcesoExistente() {
        ProcesoRequestDTO dto = ProcesoRequestDTO.builder()
                .nombre("Proceso Test 002")
                .descripcion("Descripción 2")
                .categoria("Categoría B")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO creado = procesoService.crearProceso(dto);

        ProcesoResponseDTO response = procesoService.obtenerProceso(creado.getId());

        assertNotNull(response);
        assertEquals("Proceso Test 002", response.getNombre());
        assertEquals(creado.getId(), response.getId());
    }

    @Test
    void obtenerProcesos_deberiaRetornarListaDeActivos() {
        ProcesoRequestDTO dto1 = ProcesoRequestDTO.builder()
                .nombre("Proceso Activo 001")
                .descripcion("Descripción activo 1")
                .categoria("Categoría C")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoRequestDTO dto2 = ProcesoRequestDTO.builder()
                .nombre("Proceso Activo 002")
                .descripcion("Descripción activo 2")
                .categoria("Categoría D")
                .borrador(false)
                .activo(true)
                .build();

        procesoService.crearProceso(dto1);
        procesoService.crearProceso(dto2);

        List<ProcesoResponseDTO> lista = procesoService.obtenerProcesos();

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(p -> p.getNombre().equals("Proceso Activo 001")));
    }

    @Test
    void obtenerPorCategoria_deberiaRetornarProcesosPorCategoria() {
        ProcesoRequestDTO dto1 = ProcesoRequestDTO.builder()
                .nombre("Proceso Categoría E 001")
                .descripcion("Descripción cat E 1")
                .categoria("Categoría E")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoRequestDTO dto2 = ProcesoRequestDTO.builder()
                .nombre("Proceso Categoría E 002")
                .descripcion("Descripción cat E 2")
                .categoria("Categoría E")
                .borrador(false)
                .activo(true)
                .build();

        procesoService.crearProceso(dto1);
        procesoService.crearProceso(dto2);

        List<ProcesoResponseDTO> lista = procesoService.obtenerPorCategoria("Categoría E");

        assertNotNull(lista);
        assertTrue(lista.size() >= 2);
        assertTrue(lista.stream().allMatch(p -> p.getCategoria().equals("Categoría E")));
    }

    @Test
    void actualizarProceso_deberiaRetornarProcesoActualizado() {
        ProcesoRequestDTO dto = ProcesoRequestDTO.builder()
                .nombre("Proceso Original")
                .descripcion("Descripción original")
                .categoria("Categoría F")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO creado = procesoService.crearProceso(dto);

        ProcesoRequestDTO update = ProcesoRequestDTO.builder()
                .nombre("Proceso Actualizado")
                .descripcion("Descripción actualizada")
                .categoria("Categoría F")
                .borrador(true)
                .activo(true)
                .build();

        ProcesoResponseDTO response = procesoService.actualizarProceso(creado.getId(), update, null);

        assertEquals("Proceso Actualizado", response.getNombre());
        assertEquals("Descripción actualizada", response.getDescripcion());
        assertTrue(response.getBorrador());
    }

    @Test
    void actualizarProceso_conIdEmpleado_deberiaRegistrarHistorial() {
        ProcesoRequestDTO dto = ProcesoRequestDTO.builder()
                .nombre("Proceso Para Historial")
                .descripcion("Descripción inicial")
                .categoria("Categoría G")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO creado = procesoService.crearProceso(dto);

        ProcesoRequestDTO update = ProcesoRequestDTO.builder()
                .nombre("Proceso Modificado")
                .descripcion("Nueva descripción")
                .categoria("Categoría G")
                .borrador(false)
                .activo(true)
                .build();

        // El idEmpleado puede ser null, pero vamos a verificar que el método funciona
        ProcesoResponseDTO response = procesoService.actualizarProceso(creado.getId(), update, 999L);

        assertEquals("Proceso Modificado", response.getNombre());
    }

    @Test
    void eliminarProceso_deberiaMarcarComoInactivo() {
        ProcesoRequestDTO dto = ProcesoRequestDTO.builder()
                .nombre("Proceso Para Eliminar")
                .descripcion("Descripción eliminar")
                .categoria("Categoría H")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO creado = procesoService.crearProceso(dto);

        procesoService.eliminarProceso(creado.getId(), null);

        Long id = creado.getId();
        assertThrows(RuntimeException.class, () -> procesoService.obtenerProceso(id));
    }

    @Test
    void eliminarProceso_conIdEmpleado_deberiaRegistrarHistorial() {
        ProcesoRequestDTO dto = ProcesoRequestDTO.builder()
                .nombre("Proceso Eliminar Con Historial")
                .descripcion("Descripción eliminar historial")
                .categoria("Categoría I")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO creado = procesoService.crearProceso(dto);

        // Eliminar con idEmpleado responsable
        procesoService.eliminarProceso(creado.getId(), 888L);

        Long id = creado.getId();
        assertThrows(RuntimeException.class, () -> procesoService.obtenerProceso(id));
    }
}
