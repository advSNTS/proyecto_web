package com.proyecto.web;

import com.proyecto.web.dto.ActividadRequestDTO;
import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.EmpresaXProcesoRequestDTO;
import com.proyecto.web.dto.NodoRequestDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.RequiereRequestDTO;
import com.proyecto.web.dto.RolRequestDTO;
import com.proyecto.web.dto.RolResponseDTO;
import com.proyecto.web.enums.Permiso;
import com.proyecto.web.enums.TipoNodo;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.service.ActividadService;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.EmpresaXProcesoService;
import com.proyecto.web.service.NodoService;
import com.proyecto.web.service.ProcesoService;
import com.proyecto.web.service.RequiereService;
import com.proyecto.web.service.RolService;
import com.proyecto.web.support.TestSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RolServiceEliminacionConflictTest {

    private static final String NIT = "NIT-ROL-DEL-CONFLICT";

    @Autowired
    private RolService rolService;
    @Autowired
    private EmpresaService empresaService;
    @Autowired
    private ProcesoService procesoService;
    @Autowired
    private EmpresaXProcesoService empresaXProcesoService;
    @Autowired
    private NodoService nodoService;
    @Autowired
    private ActividadService actividadService;
    @Autowired
    private RequiereService requiereService;

    private Long rolId;

    @BeforeEach
    void setUp() {
        TestSecurityContext.authenticate(NIT);
        
        EmpresaRequestDTO empresaDTO = new EmpresaRequestDTO();
        empresaDTO.setNit(NIT);
        empresaDTO.setNombre("Empresa");
        empresaDTO.setCorreo("e@test.com");
        empresaService.crearEmpresa(empresaDTO);

        Long procesoId = procesoService.crearProceso(ProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("P")
                .categoria("C")
                .borrador(false)
                .activo(true)
                .build()).getId();

        empresaXProcesoService.asignarProceso(EmpresaXProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
                .idProceso(procesoId)
                .nitOwner(NIT)
                .permiso(Permiso.ADMINISTRAR)
                .build());

        Long nodoId = nodoService.crearNodo(NodoRequestDTO.builder()
                .nitEmpresa(NIT)
                .idProceso(procesoId)
                .tipo(TipoNodo.ACTIVIDAD)
                .nombre("N1")
                .coordenadaX(0L)
                .coordenadaY(0L)
                .build()).getId();

        Long actividadId = actividadService.crearActividad(ActividadRequestDTO.builder()
                .nodoId(nodoId)
                .descripcion("A1")
                .build()).getId();

        RolResponseDTO rol = rolService.crearRol(RolRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("R1")
                .permiso(Permiso.VER)
                .build());
        this.rolId = rol.getId();

        requiereService.asignarRol(RequiereRequestDTO.builder()
                .actividadId(actividadId)
                .rolId(rolId)
                .build());
    }

    @Test
    void eliminarRol_conRequiereActivo_lanzaConflicto() {
        assertThrows(BusinessException.class, () -> rolService.eliminarRol(rolId));
    }
}
