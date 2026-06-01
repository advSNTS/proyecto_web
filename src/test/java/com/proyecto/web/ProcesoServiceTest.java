package com.proyecto.web;

import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.HistorialProcesoResumenDTO;
import com.proyecto.web.dto.PoolRequestDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.PoolService;
import com.proyecto.web.service.ProcesoService;
import com.proyecto.web.support.TestSecurityContext;
import org.springframework.http.HttpStatus;
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

    @Autowired
    private PoolService poolService;

    @BeforeEach
    void crearEmpresa() {
        EmpresaRequestDTO emp = new EmpresaRequestDTO();
        emp.setNit(NIT);
        emp.setNombre("Empresa Proc Test");
        emp.setCorreo("proc@test.com");
        empresaService.crearEmpresa(emp);
        TestSecurityContext.authenticate(NIT);
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

        ProcesoResponseDTO response = procesoService.obtenerProceso(creado.getId());

        assertNotNull(response);
        assertEquals("Proceso Test 002", response.getNombre());
        assertEquals(creado.getId(), response.getId());
    }

    @Test
    void obtenerProcesos_deberiaRetornarListaDeActivos() {
        procesoService.crearProceso(nuevo("P1", "C1"));
        procesoService.crearProceso(nuevo("P2", "C2"));

        List<ProcesoResponseDTO> lista = procesoService.obtenerProcesos(null);

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(p -> p.getNombre().equals("P1")));
    }

    @Test
    void obtenerPorCategoria_deberiaRetornarProcesosPorCategoria() {
        procesoService.crearProceso(nuevo("E1", "Categoría E"));
        procesoService.crearProceso(nuevo("E2", "Categoría E"));

        List<ProcesoResponseDTO> lista = procesoService.obtenerPorCategoria("Categoría E");

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

        ProcesoResponseDTO response = procesoService.actualizarProceso(creado.getId(), update);

        assertEquals("Proceso Actualizado", response.getNombre());
        assertEquals("Descripción actualizada", response.getDescripcion());
        assertTrue(response.getBorrador());
    }

    @Test
    void actualizarProceso_conIdEmpleado_deberiaRegistrarHistorial() {
        ProcesoResponseDTO creado = procesoService.crearProceso(nuevo("Proceso Para Historial", "Categoría G"));

        ProcesoRequestDTO update = nuevo("Proceso Modificado", "Categoría G");

        ProcesoResponseDTO response = procesoService.actualizarProceso(creado.getId(), update);

        assertEquals("Proceso Modificado", response.getNombre());
    }

    @Test
    void eliminarProceso_deberiaMarcarComoInactivo() {
        ProcesoResponseDTO creado = procesoService.crearProceso(nuevo("Proceso Para Eliminar", "Categoría H"));

        procesoService.eliminarProceso(creado.getId());

        Long id = creado.getId();
        assertThrows(BusinessException.class, () -> procesoService.obtenerProceso(id));
    }

    @Test
    void eliminarProceso_conIdEmpleado_deberiaRegistrarHistorial() {
        ProcesoResponseDTO creado = procesoService.crearProceso(nuevo("Proceso Eliminar Con Historial", "Categoría I"));

        procesoService.eliminarProceso(creado.getId());

        Long id = creado.getId();
        assertThrows(BusinessException.class, () -> procesoService.obtenerProceso(id));
    }

    @Test
    void obtenerDetalleProcesoRapido_deberiaRetornarMismoProceso() {
        ProcesoResponseDTO creado = procesoService.crearProceso(nuevo("Proceso Rapido", "Cat-R"));

        ProcesoResponseDTO rapido = procesoService.obtenerDetalleProcesoRapido(creado.getId());

        assertEquals(creado.getId(), rapido.getId());
        assertEquals("Proceso Rapido", rapido.getNombre());
    }

    @Test
    void buscarVigente_sinAutenticacion_deberiaLanzar() {
        ProcesoResponseDTO creado = procesoService.crearProceso(nuevo("Proceso Nit", "Cat-N"));
        TestSecurityContext.clear();
        Long procesoId = creado.getId();
        assertThrows(com.proyecto.web.exception.AuthenticationException.class,
                () -> procesoService.buscarVigente(procesoId));
    }

    @Test
    void obtenerHistorialProcesoParaEmpresa_deberiaRegistrarCambios() {
        ProcesoResponseDTO creado = procesoService.crearProceso(nuevo("Proceso Historial API", "Cat-H"));

        procesoService.actualizarProceso(
                creado.getId(),
                nuevo("Proceso Historial Modificado", "Cat-H"));

        var historial = procesoService.obtenerHistorialProcesoParaEmpresa(creado.getId(), 10);

        assertFalse(historial.isEmpty());
    }

    @Test
    void obtenerResumenHistorialProceso_deberiaIndicarTotal() {
        ProcesoResponseDTO creado = procesoService.crearProceso(nuevo("Proceso Resumen", "Cat-S"));

        procesoService.actualizarProceso(
                creado.getId(),
                nuevo("Proceso Resumen Actualizado", "Cat-S"));

        HistorialProcesoResumenDTO resumen =
                procesoService.obtenerResumenHistorialProceso(creado.getId(), 5);

        assertEquals(creado.getId(), resumen.getIdProceso());
        assertTrue(resumen.getTotalCambios() >= 1);
    }

    @Test
    void crearProceso_empresaInexistente_deberiaLanzarNotFound() {
        TestSecurityContext.authenticate("NIT-INEXISTENTE");
        ProcesoRequestDTO dto = ProcesoRequestDTO.builder()
                .nitEmpresa("NIT-INEXISTENTE")
                .nombre("Proceso Fallido")
                .descripcion("D")
                .categoria("C")
                .borrador(false)
                .activo(true)
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> procesoService.crearProceso(dto));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void crearProceso_sinNitEmpresa_deberiaLanzarBadRequest() {
        ProcesoRequestDTO dto = ProcesoRequestDTO.builder()
                .nombre("Sin Nit")
                .descripcion("D")
                .categoria("C")
                .build();

        ProcesoResponseDTO response = procesoService.crearProceso(dto);
        assertEquals(NIT, response.getNitEmpresa());
    }

    @Test
    void obtenerProcesos_conPoolIdValido_deberiaFiltrarPorPool() {
        ProcesoResponseDTO enPool = procesoService.crearProceso(nuevo("Proceso Pool A", "Cat-P"));
        Long poolId = enPool.getPoolId();

        procesoService.crearProceso(ProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("Proceso Pool B")
                .descripcion("D")
                .categoria("Cat-P")
                .poolId(poolService.crear(PoolRequestDTO.builder()
                        .nombre("Pool Alterno")
                        .build()).getId())
                .borrador(false)
                .activo(true)
                .build());

        List<ProcesoResponseDTO> lista = procesoService.obtenerProcesos(poolId);

        assertTrue(lista.stream().anyMatch(p -> p.getNombre().equals("Proceso Pool A")));
        assertTrue(lista.stream().allMatch(p -> poolId.equals(p.getPoolId())));
    }

    @Test
    void obtenerPorCategoria_sinNitEmpresa_deberiaLanzarBadRequest() {
        List<ProcesoResponseDTO> response = procesoService.obtenerPorCategoria("Categoria");
        assertNotNull(response);
    }

    @Test
    void actualizarProceso_conPoolId_deberiaCambiarPool() {
        ProcesoResponseDTO creado = procesoService.crearProceso(nuevo("Proceso Pool Orig", "Cat-U"));
        Long nuevoPoolId = poolService.crear(PoolRequestDTO.builder()
                .nombre("Pool Destino Update")
                .build()).getId();

        ProcesoRequestDTO update = ProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("Proceso Pool Orig")
                .descripcion("D")
                .categoria("Cat-U")
                .poolId(nuevoPoolId)
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO actualizado =
                procesoService.actualizarProceso(creado.getId(), update);

        assertEquals(nuevoPoolId, actualizado.getPoolId());
    }

    @Test
    void obtenerHistorialDeProceso_deberiaRetornarRegistros() {
        ProcesoResponseDTO creado = procesoService.crearProceso(nuevo("Proceso Hist De Proceso", "Cat-HP"));
        procesoService.actualizarProceso(
                creado.getId(),
                nuevo("Proceso Hist De Proceso Mod", "Cat-HP"));

        var historial = procesoService.obtenerHistorialDeProceso(creado.getId());

        assertFalse(historial.isEmpty());
    }

    @Test
    void buscarVigenteGlobal_inexistente_deberiaLanzarNotFound() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> procesoService.buscarVigenteGlobal(999_999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }
}
