package com.proyecto.web;

import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.HistorialProcesoResponseDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.ProcesoService;
import com.proyecto.web.support.TestSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProcesoHistorialServiceTest {

    private static final String NIT = "900PROC-HIST-01";

    @Autowired
    private ProcesoService procesoService;

    @Autowired
    private EmpresaService empresaService;

    @BeforeEach
    void crearEmpresa() {
        EmpresaRequestDTO emp = new EmpresaRequestDTO();
        emp.setNit(NIT);
        emp.setNombre("Empresa Historial Test");
        emp.setCorreo("hist@test.com");
        empresaService.crearEmpresa(emp);
        TestSecurityContext.authenticate(NIT);
    }

    @Test
    void obtenerHistorialProcesoParaEmpresa_deberiaRetornarDetalleDelProceso() {
        ProcesoRequestDTO crear = ProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("Proceso Historial")
                .descripcion("Base")
                .categoria("Categoria H")
                .borrador(false)
                .activo(true)
                .build();

        ProcesoResponseDTO creado = procesoService.crearProceso(crear);

        ProcesoRequestDTO update = ProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("Proceso Historial Editado")
                .descripcion("Base editada")
                .categoria("Categoria H")
                .borrador(false)
                .activo(true)
                .build();

        procesoService.actualizarProceso(creado.getId(), update, null, NIT);

        List<HistorialProcesoResponseDTO> historial =
                procesoService.obtenerHistorialProcesoParaEmpresa(creado.getId(), NIT);

        assertNotNull(historial);
        assertFalse(historial.isEmpty());
        assertEquals(creado.getId(), historial.get(0).getIdProceso());
        assertEquals("EDICION", historial.get(0).getTipoAccion());
    }
}
