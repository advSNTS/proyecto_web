package com.proyecto.web;

import com.proyecto.web.dto.CredencialRequestDTO;
import com.proyecto.web.dto.EmpleadoRequestDTO;
import com.proyecto.web.dto.EmpleadoResponseDTO;
import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.enums.TipoDocumento;
import com.proyecto.web.service.EmpleadoService;
import com.proyecto.web.service.EmpresaService;
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
public class EmpleadoServiceTest {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private EmpresaService empresaService;

    @BeforeEach
    void setUp() {
        // Crear empresa de prueba para que exista antes de crear empleados
        EmpresaRequestDTO empresaDTO = new EmpresaRequestDTO();
        empresaDTO.setNit("NIT-EMPRESA-001");
        empresaDTO.setNombre("Empresa Base Test");
        empresaDTO.setCorreo("empresa@test.com");
        empresaService.crearEmpresa(empresaDTO);
    }

    @Test
    void crearEmpleado_deberiaRetornarEmpleadoCreado() {
        EmpleadoRequestDTO dto = EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("Juan Pérez")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("123456789")
                .credencial(CredencialRequestDTO.builder()
                        .correo("juan@test.com")
                        .contrasena("password123")
                        .build())
                .build();

        EmpleadoResponseDTO response = empleadoService.crearEmpleado(dto);

        assertNotNull(response);
        assertEquals("Juan Pérez", response.getNombre());
        assertEquals("123456789", response.getNumeroDocumento());
        assertNotNull(response.getId());
    }

    @Test
    void obtenerEmpleado_deberiaRetornarEmpleadoExistente() {
        // Crear primero
        EmpleadoRequestDTO dto = EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("Carlos López")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("987654321")
                .credencial(CredencialRequestDTO.builder()
                        .correo("carlos@test.com")
                        .contrasena("password123")
                        .build())
                .build();

        EmpleadoResponseDTO creado = empleadoService.crearEmpleado(dto);

        // Luego buscar
        EmpleadoResponseDTO response = empleadoService.obtenerEmpleado(creado.getId());

        assertNotNull(response);
        assertEquals("Carlos López", response.getNombre());
        assertEquals(creado.getId(), response.getId());
    }

    @Test
    void obtenerEmpleados_deberiaRetornarLista() {
        EmpleadoRequestDTO dto = EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("María García")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("555666777")
                .credencial(CredencialRequestDTO.builder()
                        .correo("maria@test.com")
                        .contrasena("password123")
                        .build())
                .build();

        empleadoService.crearEmpleado(dto);

        List<EmpleadoResponseDTO> lista = empleadoService.obtenerEmpleados();

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(e -> e.getNombre().equals("María García")));
    }

    @Test
    void obtenerEmpleadosPorEmpresa_deberiaRetornarEmpleadosDeLaEmpresa() {
        EmpleadoRequestDTO dto1 = EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("Empleado Uno")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("111222333")
                .credencial(CredencialRequestDTO.builder()
                        .correo("empleado1@test.com")
                        .contrasena("password123")
                        .build())
                .build();

        EmpleadoRequestDTO dto2 = EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("Empleado Dos")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("444555666")
                .credencial(CredencialRequestDTO.builder()
                        .correo("empleado2@test.com")
                        .contrasena("password123")
                        .build())
                .build();

        empleadoService.crearEmpleado(dto1);
        empleadoService.crearEmpleado(dto2);

        List<EmpleadoResponseDTO> lista = empleadoService.obtenerEmpleadosPorEmpresa("NIT-EMPRESA-001");

        assertNotNull(lista);
        assertTrue(lista.size() >= 2);
    }

    @Test
    void actualizarEmpleado_deberiaRetornarEmpleadoActualizado() {
        EmpleadoRequestDTO dto = EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("Original")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("777888999")
                .credencial(CredencialRequestDTO.builder()
                        .correo("original@test.com")
                        .contrasena("password123")
                        .build())
                .build();

        EmpleadoResponseDTO creado = empleadoService.crearEmpleado(dto);

        EmpleadoRequestDTO update = EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("Actualizado")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("777888999")
                .credencial(CredencialRequestDTO.builder()
                        .correo("actualizado@test.com")
                        .contrasena("newpass")
                        .build())
                .build();

        EmpleadoResponseDTO response = empleadoService.actualizarEmpleado(creado.getId(), update);

        assertEquals("Actualizado", response.getNombre());
    }

    @Test
    void eliminarEmpleado_deberiaMarcarComoEliminado() {
        EmpleadoRequestDTO dto = EmpleadoRequestDTO.builder()
                .nitEmpresa("NIT-EMPRESA-001")
                .nombre("Para Eliminar")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("999888777")
                .credencial(CredencialRequestDTO.builder()
                        .correo("eliminar@test.com")
                        .contrasena("password123")
                        .build())
                .build();

        EmpleadoResponseDTO creado = empleadoService.crearEmpleado(dto);

        empleadoService.eliminarEmpleado(creado.getId());

        assertThrows(RuntimeException.class, () -> empleadoService.obtenerEmpleado(creado.getId()));
    }
}
