package com.proyecto.web.service;

import com.proyecto.web.dto.CredencialRequestDTO;
import com.proyecto.web.dto.EmpleadoRequestDTO;
import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.PoolRequestDTO;
import com.proyecto.web.dto.ProcesoCompartidoRequestDTO;
import com.proyecto.web.dto.ProcesoCompartidoResponseDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.enums.PermisoProcesoCompartido;
import com.proyecto.web.enums.TipoDocumento;
import com.proyecto.web.exception.AuthenticationException;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.CredencialRepository;
import com.proyecto.web.repository.HistorialProcesoRepository;
import com.proyecto.web.support.TestSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ProcesoCompartidoService Tests")
class ProcesoCompartidoServiceTest {

    private static final String NIT = "900-SHARE-SVC";

    @Autowired
    private ProcesoCompartidoService procesoCompartidoService;

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private ProcesoService procesoService;

    @Autowired
    private PoolService poolService;

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private CredencialRepository credencialRepository;

    @Autowired
    private HistorialProcesoRepository historialProcesoRepository;

    private Long adminEmpleadoId;
    private Long procesoId;
    private Long poolDestinoId;

    @BeforeEach
    void setUp() {
        EmpresaRequestDTO empresa = new EmpresaRequestDTO();
        empresa.setNit(NIT);
        empresa.setNombre("Empresa Compartir");
        empresa.setCorreo("admin.share@test.com");
        empresa.setContrasenaAdministrador("AdminShare1!");
        empresaService.crearEmpresa(empresa);

        adminEmpleadoId = credencialRepository.findByCorreo("admin.share@test.com")
                .orElseThrow()
                .getEmpleado()
                .getId();
        
        TestSecurityContext.authenticate(adminEmpleadoId, NIT, "ROLE_ADMIN", "ROLE_EDITOR", "ROLE_ADMIN_SISTEMA");

        ProcesoResponseDTO proceso = procesoService.crearProceso(ProcesoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("Proceso Compartido")
                .descripcion("Proceso para pruebas de compartir")
                .categoria("General")
                .borrador(false)
                .activo(true)
                .build());
        procesoId = proceso.getId();

        var poolExtra = poolService.crear(PoolRequestDTO.builder()
                .nombre("Pool Colaboracion")
                .descripcion("Pool para compartir")
                .build());
        poolDestinoId = poolExtra.getId();
    }

    @Test
    @DisplayName("compartir asocia proceso con pool y registra historial")
    void compartir_exito() {
        ProcesoCompartidoRequestDTO dto = ProcesoCompartidoRequestDTO.builder()
                .poolId(poolDestinoId)
                .permiso(PermisoProcesoCompartido.LECTURA)
                .build();

        ProcesoCompartidoResponseDTO response = procesoCompartidoService.compartir(procesoId, dto);

        assertNotNull(response.getId());
        assertEquals(procesoId, response.getProcesoId());
        assertEquals(poolDestinoId, response.getPoolId());
        assertEquals(PermisoProcesoCompartido.LECTURA, response.getPermiso());
        assertFalse(historialProcesoRepository.findAllByProceso_IdOrderByFechaCambioDesc(procesoId).isEmpty());
    }

    @Test
    @DisplayName("listarPorProceso devuelve compartidos activos")
    void listarPorProceso_exito() {
        compartir(PermisoProcesoCompartido.EDICION);

        List<ProcesoCompartidoResponseDTO> lista =
                procesoCompartidoService.listarPorProceso(procesoId);

        assertEquals(1, lista.size());
        assertEquals(poolDestinoId, lista.get(0).getPoolId());
    }

    @Test
    @DisplayName("compartir sin empleado autenticado devuelve 401")
    void compartir_sinEmpleado() {
        TestSecurityContext.clear();
        
        ProcesoCompartidoRequestDTO dto = ProcesoCompartidoRequestDTO.builder()
                .poolId(poolDestinoId)
                .permiso(PermisoProcesoCompartido.LECTURA)
                .build();

        assertThrows(
                AuthenticationException.class,
                () -> procesoCompartidoService.compartir(procesoId, dto));
    }

    @Test
    @DisplayName("compartir sin rol ADMIN devuelve 403")
    void compartir_sinRolAdmin() {
        Long lectorId = empleadoService.crearEmpleado(EmpleadoRequestDTO.builder()
                .nitEmpresa(NIT)
                .nombre("Lector Proceso")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("lector-share-01")
                .credencial(CredencialRequestDTO.builder()
                        .correo("lector.share@test.com")
                        .contrasena("password123")
                        .build())
                .build()).getId();

        // Autenticar como lector (sin ADMIN role)
        TestSecurityContext.authenticate(lectorId, NIT, "ROLE_EDITOR");

        ProcesoCompartidoRequestDTO dto = ProcesoCompartidoRequestDTO.builder()
                .poolId(poolDestinoId)
                .permiso(PermisoProcesoCompartido.LECTURA)
                .build();

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> procesoCompartidoService.compartir(procesoId, dto));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    @DisplayName("compartir con pool inexistente devuelve 404")
    void compartir_poolNoEncontrado() {
        ProcesoCompartidoRequestDTO dto = ProcesoCompartidoRequestDTO.builder()
                .poolId(999_999L)
                .permiso(PermisoProcesoCompartido.LECTURA)
                .build();

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> procesoCompartidoService.compartir(procesoId, dto));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("compartir duplicado devuelve 409")
    void compartir_duplicado() {
        compartir(PermisoProcesoCompartido.LECTURA);

        ProcesoCompartidoRequestDTO dto = ProcesoCompartidoRequestDTO.builder()
                .poolId(poolDestinoId)
                .permiso(PermisoProcesoCompartido.EDICION)
                .build();

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> procesoCompartidoService.compartir(procesoId, dto));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    @DisplayName("listarPorProceso falla si proceso no existe en empresa")
    void listarPorProceso_procesoNoEncontrado() {
        assertThrows(
                BusinessException.class,
                () -> procesoCompartidoService.listarPorProceso(999_999L));
    }

    private void compartir(PermisoProcesoCompartido permiso) {
        procesoCompartidoService.compartir(
                procesoId,
                ProcesoCompartidoRequestDTO.builder()
                        .poolId(poolDestinoId)
                        .permiso(permiso)
                        .build());
    }
}
