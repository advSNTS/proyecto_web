package com.proyecto.web;

import com.proyecto.web.dto.CredencialRequestDTO;
import com.proyecto.web.dto.EmpleadoRequestDTO;
import com.proyecto.web.dto.EmpleadoResponseDTO;
import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.RolRequestDTO;
import com.proyecto.web.dto.RolResponseDTO;
import com.proyecto.web.dto.RolXEmpleadoRequestDTO;
import com.proyecto.web.dto.RolXEmpleadoResponseDTO;
import com.proyecto.web.enums.Permiso;
import com.proyecto.web.enums.TipoDocumento;
import com.proyecto.web.service.EmpleadoService;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.RolService;
import com.proyecto.web.service.RolXEmpleadoService;
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
class RolXEmpleadoServiceTest {

    @Autowired
    private RolXEmpleadoService rolXEmpleadoService;

    @Autowired
    private RolService rolService;

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private EmpresaService empresaService;

    private Long empleadoId;
    private Long rolId;

    @BeforeEach
    void setUp() {
        // Crear empresa
        EmpresaRequestDTO empresaDTO = new EmpresaRequestDTO();
        empresaDTO.setNit("NIT-RXEM-001");
        empresaDTO.setNombre("Empresa RxEmpleado");
        empresaDTO.setCorreo("rxempleado@test.com");
        empresaService.crearEmpresa(empresaDTO);

        // Crear empleado
        EmpleadoRequestDTO empleadoDTO = EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-RXEM-001")
                .nombre("Empleado Test")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("111222333")
                .credencial(CredencialRequestDTO.builder()
                        .correo("empleado@test.com")
                        .contrasena("password123")
                        .build())
                .build();

        EmpleadoResponseDTO empleado = empleadoService.crearEmpleado(empleadoDTO);
        this.empleadoId = empleado.getId();

        // Crear rol
        RolRequestDTO rolDTO = RolRequestDTO.builder()
                .nitEmpresa("NIT-RXEM-001")
                .nombre("Rol Test")
                .permiso(Permiso.ADMINISTRAR)
                .build();

        RolResponseDTO rol = rolService.crearRol(rolDTO);
        this.rolId = rol.getId();
    }

    @Test
    void asignarRol_deberiaRetornarAsignacionCreada() {
        RolXEmpleadoRequestDTO dto = RolXEmpleadoRequestDTO.builder()
                .empleadoId(empleadoId)
                .rolId(rolId)
                .build();

        RolXEmpleadoResponseDTO response = rolXEmpleadoService.asignarRol(dto);

        assertNotNull(response);
        assertEquals(empleadoId, response.getEmpleadoId());
        assertEquals(rolId, response.getRolId());
        assertNotNull(response.getId());
    }

    @Test
    void asignarRol_noDebePermitirDuplicados() {
        RolXEmpleadoRequestDTO dto = RolXEmpleadoRequestDTO.builder()
                .empleadoId(empleadoId)
                .rolId(rolId)
                .build();

        rolXEmpleadoService.asignarRol(dto);

        // Intentar asignar el mismo rol nuevamente
        assertThrows(RuntimeException.class, () -> rolXEmpleadoService.asignarRol(dto));
    }

    @Test
    void obtenerRolesPorEmpleado_deberiaRetornarRolesDelEmpleado() {
        RolXEmpleadoRequestDTO dto = RolXEmpleadoRequestDTO.builder()
                .empleadoId(empleadoId)
                .rolId(rolId)
                .build();

        rolXEmpleadoService.asignarRol(dto);

        List<RolXEmpleadoResponseDTO> lista = rolXEmpleadoService.obtenerRolesPorEmpleado(empleadoId);

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(r -> r.getRolId().equals(rolId)));
    }

    @Test
    void obtenerEmpleadosPorRol_deberiaRetornarEmpleadosDelRol() {
        RolXEmpleadoRequestDTO dto = RolXEmpleadoRequestDTO.builder()
                .empleadoId(empleadoId)
                .rolId(rolId)
                .build();

        rolXEmpleadoService.asignarRol(dto);

        List<RolXEmpleadoResponseDTO> lista = rolXEmpleadoService.obtenerEmpleadosPorRol(rolId);

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(e -> e.getEmpleadoId().equals(empleadoId)));
    }

    @Test
    void quitarRol_deberiaMarcarComoElimina() {
        RolXEmpleadoRequestDTO dto = RolXEmpleadoRequestDTO.builder()
                .empleadoId(empleadoId)
                .rolId(rolId)
                .build();

        RolXEmpleadoResponseDTO asignacion = rolXEmpleadoService.asignarRol(dto);

        rolXEmpleadoService.quitarRol(asignacion.getId());

        // Verificar que se removió
        List<RolXEmpleadoResponseDTO> lista = rolXEmpleadoService.obtenerRolesPorEmpleado(empleadoId);
        assertTrue(lista.isEmpty());
    }
}
