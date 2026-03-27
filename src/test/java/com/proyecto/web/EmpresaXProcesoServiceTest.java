package com.proyecto.web;

import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.EmpresaXProcesoRequestDTO;
import com.proyecto.web.dto.EmpresaXProcesoResponseDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.enums.Permiso;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.EmpresaXProcesoService;
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
public class EmpresaXProcesoServiceTest {

    @Autowired
    private EmpresaXProcesoService empresaXProcesoService;

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private ProcesoService procesoService;

    private String nitEmpresa;
    private String nitOwner;
    private Long procesoId;

    @BeforeEach
    void setUp() {
        // Crear empresa principal
        EmpresaRequestDTO empresaDTO = new EmpresaRequestDTO();
        empresaDTO.setNit("NIT-EXPROC-001");
        empresaDTO.setNombre("Empresa Principal");
        empresaDTO.setCorreo("principal@test.com");
        empresaService.crearEmpresa(empresaDTO);
        this.nitEmpresa = "NIT-EXPROC-001";

        // Crear empresa owner
        EmpresaRequestDTO ownerDTO = new EmpresaRequestDTO();
        ownerDTO.setNit("NIT-OWNER-001");
        ownerDTO.setNombre("Empresa Owner");
        ownerDTO.setCorreo("owner@test.com");
        empresaService.crearEmpresa(ownerDTO);
        this.nitOwner = "NIT-OWNER-001";

        // Crear proceso
        ProcesoRequestDTO procesoDTO = ProcesoRequestDTO.builder()
                .nombre("Proceso Test")
                .descripcion("Descripción proceso test")
                .categoria("Categoría C")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO proceso = procesoService.crearProceso(procesoDTO);
        this.procesoId = proceso.getId();
    }

    @Test
    void asignarProceso_deberiaRetornarAsignacionCreada() {
        EmpresaXProcesoRequestDTO dto = EmpresaXProcesoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .idProceso(procesoId)
                .nitOwner(nitOwner)
                .permiso(Permiso.EDITAR)
                .build();

        EmpresaXProcesoResponseDTO response = empresaXProcesoService.asignarProceso(dto);

        assertNotNull(response);
        assertEquals(nitEmpresa, response.getNitEmpresa());
        assertEquals(procesoId, response.getIdProceso());
        assertEquals(Permiso.EDITAR, response.getPermiso());
    }

    @Test
    void asignarProceso_noDebePermitirDuplicados() {
        EmpresaXProcesoRequestDTO dto = EmpresaXProcesoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .idProceso(procesoId)
                .nitOwner(nitOwner)
                .permiso(Permiso.VER)
                .build();

        empresaXProcesoService.asignarProceso(dto);

        // Intentar asignar el mismo proceso nuevamente
        assertThrows(RuntimeException.class, () -> empresaXProcesoService.asignarProceso(dto));
    }

    @Test
    void obtenerProcesosPorEmpresa_deberiaRetornarProcesosDeEmpresa() {
        EmpresaXProcesoRequestDTO dto = EmpresaXProcesoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .idProceso(procesoId)
                .nitOwner(nitOwner)
                .permiso(Permiso.ADMINISTRAR)
                .build();

        empresaXProcesoService.asignarProceso(dto);

        List<EmpresaXProcesoResponseDTO> lista = empresaXProcesoService.obtenerProcesosPorEmpresa(nitEmpresa);

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(ep -> ep.getIdProceso().equals(procesoId)));
    }

    @Test
    void obtenerEmpresasPorProceso_deberiaRetornarEmpresasDelProceso() {
        EmpresaXProcesoRequestDTO dto = EmpresaXProcesoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .idProceso(procesoId)
                .nitOwner(nitOwner)
                .permiso(Permiso.VER)
                .build();

        empresaXProcesoService.asignarProceso(dto);

        List<EmpresaXProcesoResponseDTO> lista = empresaXProcesoService.obtenerEmpresasPorProceso(procesoId);

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(ep -> ep.getNitEmpresa().equals(nitEmpresa)));
    }

    @Test
    void obtenerProcesosComoOwner_deberiaRetornarProcesosDelOwner() {
        EmpresaXProcesoRequestDTO dto = EmpresaXProcesoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .idProceso(procesoId)
                .nitOwner(nitOwner)
                .permiso(Permiso.ADMINISTRAR)
                .build();

        empresaXProcesoService.asignarProceso(dto);

        List<EmpresaXProcesoResponseDTO> lista = empresaXProcesoService.obtenerProcesosComoOwner(nitOwner);

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
    }

    @Test
    void actualizarPermiso_deberiaActualizarPermisoDeAsignacion() {
        EmpresaXProcesoRequestDTO dto = EmpresaXProcesoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .idProceso(procesoId)
                .nitOwner(nitOwner)
                .permiso(Permiso.VER)
                .build();

        empresaXProcesoService.asignarProceso(dto);

        EmpresaXProcesoRequestDTO update = EmpresaXProcesoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .idProceso(procesoId)
                .nitOwner(nitOwner)
                .permiso(Permiso.ADMINISTRAR)
                .build();

        EmpresaXProcesoResponseDTO response = empresaXProcesoService.actualizarPermiso(nitEmpresa, procesoId, update);

        assertEquals(Permiso.ADMINISTRAR, response.getPermiso());
    }

    @Test
    void quitarProceso_deberiaMarcarComoEliminado() {
        EmpresaXProcesoRequestDTO dto = EmpresaXProcesoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .idProceso(procesoId)
                .nitOwner(nitOwner)
                .permiso(Permiso.EDITAR)
                .build();

        empresaXProcesoService.asignarProceso(dto);

        empresaXProcesoService.quitarProceso(nitEmpresa, procesoId);

        // Verificar que se removió
        List<EmpresaXProcesoResponseDTO> lista = empresaXProcesoService.obtenerProcesosPorEmpresa(nitEmpresa);
        assertTrue(lista.isEmpty());
    }
}
