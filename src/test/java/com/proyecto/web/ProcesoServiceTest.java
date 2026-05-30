package com.proyecto.web;

import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.HistorialProcesoResumenDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.service.EmpresaService;
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
class ProcesoServiceTest {

    private static final String NIT = "900PROC-SVC-01";

    @Autowired
    private ProcesoService procesoService;

    @Autowired
    private EmpresaService empresaService;

    @BeforeEach
    void crearEmpresa() {
        EmpresaRequestDTO emp = new EmpresaRequestDTO();
        emp.setNit(NIT);
        emp.setNombre("Empresa Proc Test");
        emp.setCorreo("proc@test.com");
        empresaService.crearEmpresa(emp);
    }

    private ProcesoRequestDTO nuevo(String nombre, String categoria) {
        return ProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre(nombre)
                .descripcion("D")
                .categoria(categoria)
                .borrador(false)
                .activo(true)
                .build();
    }

    @Test
    void crearProceso_deberiaRetornarProcesoCreado() {
        ProcesoRequestDTO dto = ProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
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
        assertNotNull(response.getPoolId());
        assertEquals(NIT, response.getNitEmpresa());
    }

    @Test
    void obtenerProceso_deberiaRetornarProcesoExistente() {
        ProcesoRequestDTO dto = nuevo("Proceso Test 002", "Categoría B");

        ProcesoResponseDTO creado = procesoService.crearProceso(dto);

        ProcesoResponseDTO response = procesoService.obtenerProceso(creado.getId(), NIT);

        assertNotNull(response);
        assertEquals("Proceso Test 002", response.getNombre());
        assertEquals(creado.getId(), response.getId());
    }

    @Test
    void obtenerProcesos_deberiaRetornarListaDeActivos() {
        procesoService.crearProceso(nuevo("P1", "C1"));
        procesoService.crearProceso(nuevo("P2", "C2"));

        List<ProcesoResponseDTO> lista = procesoService.obtenerProcesos(NIT, null);

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(p -> p.getNombre().equals("P1")));
    }

    @Test
    void obtenerPorCategoria_deberiaRetornarProcesosPorCategoria() {
        procesoService.crearProceso(nuevo("E1", "Categoría E"));
        procesoService.crearProceso(nuevo("E2", "Categoría E"));

        List<ProcesoResponseDTO> lista = procesoService.obtenerPorCategoria("Categoría E", NIT);

        assertNotNull(lista);
        assertTrue(lista.size() >= 2);
        assertTrue(lista.stream().allMatch(p -> p.getCategoria().equals("Categoría E")));
    }

    @Test
    void actualizarProceso_deberiaRetornarProcesoActualizado() {
        ProcesoResponseDTO creado = procesoService.crearProceso(nuevo("Proceso Original", "Categoría F"));

        ProcesoRequestDTO update = ProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("Proceso Actualizado")
                .descripcion("Descripción actualizada")
                .categoria("Categoría F")
                .borrador(true)
                .activo(true)
                .build();

        ProcesoResponseDTO response = procesoService.actualizarProceso(creado.getId(), update, null, NIT);

        assertEquals("Proceso Actualizado", response.getNombre());
        assertEquals("Descripción actualizada", response.getDescripcion());
        assertTrue(response.getBorrador());
    }

    @Test
    void actualizarProceso_conIdEmpleado_deberiaRegistrarHistorial() {
        ProcesoResponseDTO creado = procesoService.crearProceso(nuevo("Proceso Para Historial", "Categoría G"));

        ProcesoRequestDTO update = nuevo("Proceso Modificado", "Categoría G");

        ProcesoResponseDTO response = procesoService.actualizarProceso(creado.getId(), update, 999L, NIT);

        assertEquals("Proceso Modificado", response.getNombre());
    }

    @Test
    void eliminarProceso_deberiaMarcarComoInactivo() {
        ProcesoResponseDTO creado = procesoService.crearProceso(nuevo("Proceso Para Eliminar", "Categoría H"));

        procesoService.eliminarProceso(creado.getId(), null, NIT);

        Long id = creado.getId();
        assertThrows(BusinessException.class, () -> procesoService.obtenerProceso(id, NIT));
    }

    @Test
    void eliminarProceso_conIdEmpleado_deberiaRegistrarHistorial() {
        ProcesoResponseDTO creado = procesoService.crearProceso(nuevo("Proceso Eliminar Con Historial", "Categoría I"));

        procesoService.eliminarProceso(creado.getId(), 888L, NIT);

        Long id = creado.getId();
        assertThrows(BusinessException.class, () -> procesoService.obtenerProceso(id, NIT));
    }

    @Test
    void obtenerDetalleProcesoRapido_deberiaRetornarMismoProceso() {
        ProcesoResponseDTO creado = procesoService.crearProceso(nuevo("Proceso Rapido", "Cat-R"));

        ProcesoResponseDTO rapido = procesoService.obtenerDetalleProcesoRapido(creado.getId(), NIT);

        assertEquals(creado.getId(), rapido.getId());
        assertEquals("Proceso Rapido", rapido.getNombre());
    }

    @Test
    void buscarVigente_sinNitEmpresa_deberiaLanzarBadRequest() {
        ProcesoResponseDTO creado = procesoService.crearProceso(nuevo("Proceso Nit", "Cat-N"));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> procesoService.buscarVigente(creado.getId(), " "));
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void obtenerHistorialProcesoParaEmpresa_deberiaRegistrarCambios() {
        ProcesoResponseDTO creado = procesoService.crearProceso(nuevo("Proceso Historial API", "Cat-H"));

        procesoService.actualizarProceso(
                creado.getId(),
                nuevo("Proceso Historial Modificado", "Cat-H"),
                null,
                NIT);

        var historial = procesoService.obtenerHistorialProcesoParaEmpresa(creado.getId(), NIT, 10);

        assertFalse(historial.isEmpty());
    }

    @Test
    void obtenerResumenHistorialProceso_deberiaIndicarTotal() {
        ProcesoResponseDTO creado = procesoService.crearProceso(nuevo("Proceso Resumen", "Cat-S"));

        procesoService.actualizarProceso(
                creado.getId(),
                nuevo("Proceso Resumen Actualizado", "Cat-S"),
                null,
                NIT);

        HistorialProcesoResumenDTO resumen =
                procesoService.obtenerResumenHistorialProceso(creado.getId(), NIT, 5);

        assertEquals(creado.getId(), resumen.getIdProceso());
        assertTrue(resumen.getTotalCambios() >= 1);
    }

    @Test
    void crearProceso_empresaInexistente_deberiaLanzarNotFound() {
        ProcesoRequestDTO dto = ProcesoRequestDTO.builder()
                .nitEmpresa("NIT-INEXISTENTE")
                .nombre("Proceso Fallido")
                .descripcion("D")
                .categoria("C")
                .borrador(false)
                .activo(true)
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> procesoService.crearProceso(dto));
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, ex.getStatus());
    }
}
