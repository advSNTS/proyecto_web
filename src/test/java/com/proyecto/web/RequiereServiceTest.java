package com.proyecto.web;

import com.proyecto.web.dto.ActividadRequestDTO;
import com.proyecto.web.dto.ActividadResponseDTO;
import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.EmpresaXProcesoRequestDTO;
import com.proyecto.web.dto.NodoRequestDTO;
import com.proyecto.web.dto.NodoResponseDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.dto.RequiereRequestDTO;
import com.proyecto.web.dto.RequiereResponseDTO;
import com.proyecto.web.dto.RolRequestDTO;
import com.proyecto.web.dto.RolResponseDTO;
import com.proyecto.web.enums.Permiso;
import com.proyecto.web.enums.TipoNodo;
import com.proyecto.web.service.ActividadService;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.EmpresaXProcesoService;
import com.proyecto.web.service.NodoService;
import com.proyecto.web.service.ProcesoService;
import com.proyecto.web.service.RequiereService;
import com.proyecto.web.service.RolService;
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
class RequiereServiceTest {

    @Autowired
    private RequiereService requiereService;

    @Autowired
    private ActividadService actividadService;

    @Autowired
    private RolService rolService;

    @Autowired
    private NodoService nodoService;

    @Autowired
    private ProcesoService procesoService;

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private EmpresaXProcesoService empresaXProcesoService;

    private Long actividadId;
    private Long rolId;
    private Long nodoId;

    @BeforeEach
    void setUp() {
        // Crear empresa
        EmpresaRequestDTO empresaDTO = new EmpresaRequestDTO();
        empresaDTO.setNit("NIT-REQ-001");
        empresaDTO.setNombre("Empresa Requiere Test");
        empresaDTO.setCorreo("requiere@test.com");
        empresaService.crearEmpresa(empresaDTO);

        // Crear proceso
        ProcesoRequestDTO procesoDTO = ProcesoRequestDTO.builder()
                .nombre("Proceso Requiere Test")
                .descripcion("Descripción proceso requiere")
                .categoria("Categoría D")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO proceso = procesoService.crearProceso(procesoDTO);
        Long procesoId = proceso.getId();

        // Asociar empresa con proceso
        EmpresaXProcesoRequestDTO empresaXProcesoDTO = EmpresaXProcesoRequestDTO.builder()
                .nitEmpresa("NIT-REQ-001")
                .idProceso(procesoId)
                .nitOwner("NIT-REQ-001")
                .permiso(Permiso.ADMINISTRAR)
                .build();

        empresaXProcesoService.asignarProceso(empresaXProcesoDTO);

        // Crear nodo actividad
        NodoRequestDTO nodoDTO = NodoRequestDTO.builder()
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("Nodo Requiere")
                .build();

        NodoResponseDTO nodo = nodoService.crearNodo(nodoDTO);
        this.nodoId = nodo.getId();

        // Crear actividad
        ActividadRequestDTO actividadDTO = ActividadRequestDTO.builder()
                .nodoId(nodoId)
                .descripcion("Actividad Requiere Test")
                .build();

        ActividadResponseDTO actividad = actividadService.crearActividad(actividadDTO);
        this.actividadId = actividad.getId();

        // Crear rol
        RolRequestDTO rolDTO = RolRequestDTO.builder()
                .nitEmpresa("NIT-REQ-001")
                .nombre("Rol Requiere Test")
                .permiso(Permiso.ADMINISTRAR)
                .build();

        RolResponseDTO rol = rolService.crearRol(rolDTO);
        this.rolId = rol.getId();
    }

    @Test
    void asignarRol_deberiaRetornarAsignacionCreada() {
        RequiereRequestDTO dto = RequiereRequestDTO.builder()
                .actividadId(actividadId)
                .rolId(rolId)
                .build();

        RequiereResponseDTO response = requiereService.asignarRol(dto);

        assertNotNull(response);
        assertEquals(actividadId, response.getActividadId());
        assertEquals(rolId, response.getRolId());
        assertNotNull(response.getActividadId());
    }

    @Test
    void asignarRol_noDebePermitirDuplicados() {
        RequiereRequestDTO dto = RequiereRequestDTO.builder()
                .actividadId(actividadId)
                .rolId(rolId)
                .build();

        requiereService.asignarRol(dto);

        // Intentar asignar el mismo rol
        assertThrows(Exception.class, () -> requiereService.asignarRol(dto));
    }

    @Test
    void obtenerRolesPorActividad_deberiaRetornarRolesAsignados() {
        RequiereRequestDTO dto = RequiereRequestDTO.builder()
                .actividadId(actividadId)
                .rolId(rolId)
                .build();

        requiereService.asignarRol(dto);

        List<RequiereResponseDTO> lista = requiereService.obtenerRolesPorActividad(actividadId);

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(r -> r.getRolId().equals(rolId)));
    }

    @Test
    void obtenerActividadesPorRol_deberiaRetornarActividadesDelRol() {
        RequiereRequestDTO dto = RequiereRequestDTO.builder()
                .actividadId(actividadId)
                .rolId(rolId)
                .build();

        requiereService.asignarRol(dto);

        List<RequiereResponseDTO> lista = requiereService.obtenerActividadesPorRol(rolId);

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(a -> a.getActividadId().equals(actividadId)));
    }

    @Test
    void quitarRol_deberiaMarcarComoEliminado() {
        RequiereRequestDTO dto = RequiereRequestDTO.builder()
                .actividadId(actividadId)
                .rolId(rolId)
                .build();

        requiereService.asignarRol(dto);

        requiereService.quitarRol(actividadId, rolId);

        List<RequiereResponseDTO> lista = requiereService.obtenerRolesPorActividad(actividadId);

        assertNotNull(lista);
        assertTrue(lista.isEmpty());
    }
}
