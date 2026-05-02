package com.proyecto.web;

import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.GatewayRequestDTO;
import com.proyecto.web.dto.GatewayResponseDTO;
import com.proyecto.web.dto.NodoRequestDTO;
import com.proyecto.web.dto.NodoResponseDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.enums.TipoGateway;
import com.proyecto.web.enums.TipoNodo;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.GatewayService;
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
class GatewayServiceTest {

    private static final String NIT = "900GW-SVC-1";

    @Autowired
    private GatewayService gatewayService;

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
        emp.setNombre("Emp GW");
        emp.setCorreo("egw@test.com");
        empresaService.crearEmpresa(emp);

        ProcesoRequestDTO procesoDTO = ProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("Proceso Gateway Test")
                .descripcion("Descripción proceso gateway")
                .categoria("Categoría C")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO proceso = procesoService.crearProceso(procesoDTO);
        this.procesoId = proceso.getId();

        NodoRequestDTO nodoDTO = NodoRequestDTO.builder()
                .nitEmpresa(NIT)
                .idProceso(procesoId)
                .tipo(TipoNodo.GATEWAY)
                .nombre("Nodo Gateway")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build();

        NodoResponseDTO nodo = nodoService.crearNodo(nodoDTO);
        this.nodoId = nodo.getId();
    }

    @Test
    void crearGateway_deberiaRetornarGatewayCreado() {
        GatewayRequestDTO dto = GatewayRequestDTO.builder()
                .nodoId(nodoId)
                .tipoGateway(TipoGateway.AND)
                .build();

        GatewayResponseDTO response = gatewayService.crearGateway(dto);

        assertNotNull(response);
        assertEquals(nodoId, response.getNodoId());
        assertEquals(TipoGateway.AND, response.getTipoGateway());
        assertNotNull(response.getId());
    }

    @Test
    void crearGateway_noDebePermitirDuplicados() {
        GatewayRequestDTO dto = GatewayRequestDTO.builder()
                .nodoId(nodoId)
                .tipoGateway(TipoGateway.AND)
                .build();

        gatewayService.crearGateway(dto);

        assertThrows(Exception.class, () -> gatewayService.crearGateway(dto));
    }

    @Test
    void obtenerGateway_deberiaRetornarGatewayExistente() {
        GatewayRequestDTO dto = GatewayRequestDTO.builder()
                .nodoId(nodoId)
                .tipoGateway(TipoGateway.XOR)
                .build();

        GatewayResponseDTO creado = gatewayService.crearGateway(dto);

        GatewayResponseDTO response = gatewayService.obtenerGateway(creado.getId());

        assertNotNull(response);
        assertEquals(creado.getId(), response.getId());
        assertEquals(TipoGateway.XOR, response.getTipoGateway());
    }

    @Test
    void obtenerPorProceso_deberiaRetornarGatewaysDelProceso() {
        GatewayRequestDTO dto = GatewayRequestDTO.builder()
                .nodoId(nodoId)
                .tipoGateway(TipoGateway.AND)
                .build();

        gatewayService.crearGateway(dto);

        List<GatewayResponseDTO> lista = gatewayService.obtenerPorProceso(procesoId);

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(g -> g.getId() != null));
    }

    @Test
    void obtenerPorProcesoYTipo_deberiaRetornarGatewaysDelTipo() {
        GatewayRequestDTO dto = GatewayRequestDTO.builder()
                .nodoId(nodoId)
                .tipoGateway(TipoGateway.AND)
                .build();

        gatewayService.crearGateway(dto);

        List<GatewayResponseDTO> lista = gatewayService.obtenerPorProcesoYTipo(procesoId, TipoGateway.AND);

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().allMatch(g -> g.getTipoGateway().equals(TipoGateway.AND)));
    }

    @Test
    void actualizarGateway_deberiaActualizarTipo() {
        GatewayRequestDTO dtoCrear = GatewayRequestDTO.builder()
                .nodoId(nodoId)
                .tipoGateway(TipoGateway.AND)
                .build();

        GatewayResponseDTO creado = gatewayService.crearGateway(dtoCrear);

        GatewayRequestDTO dtoActualizar = GatewayRequestDTO.builder()
                .nodoId(nodoId)
                .tipoGateway(TipoGateway.XOR)
                .build();

        GatewayResponseDTO actualizado = gatewayService.actualizarGateway(creado.getId(), dtoActualizar);

        assertNotNull(actualizado);
        assertEquals(creado.getId(), actualizado.getId());
        assertEquals(TipoGateway.XOR, actualizado.getTipoGateway());
    }

    @Test
    void eliminarGateway_deberiaMarcarComoEliminado() {
        GatewayRequestDTO dto = GatewayRequestDTO.builder()
                .nodoId(nodoId)
                .tipoGateway(TipoGateway.AND)
                .build();

        GatewayResponseDTO creado = gatewayService.crearGateway(dto);

        gatewayService.eliminarGateway(creado.getId());

        Long id = creado.getId();
        assertThrows(RuntimeException.class, () -> gatewayService.obtenerGateway(id));
    }
}