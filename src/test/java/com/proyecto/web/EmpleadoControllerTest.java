package com.proyecto.web;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // Compila correctamente con Maven. El error rojo es solo del IDE de VSCode, NO eliminar.

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EmpleadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private EmpresaService empresaService;

    private Long empleadoId;
    private String nitEmpresa = "NIT-EMP-CTRL-001";

    @BeforeEach
    void setUp() {
        // Crear empresa
        EmpresaRequestDTO empresaDTO = new EmpresaRequestDTO();
        empresaDTO.setNit(nitEmpresa);
        empresaDTO.setNombre("Empresa Empleados");
        empresaDTO.setCorreo("emp@test.com");
        empresaService.crearEmpresa(empresaDTO);

        // Crear empleado de prueba
        EmpleadoRequestDTO empleadoDTO = EmpleadoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .nombre("Juan Test")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("123456789")
                .credencial(CredencialRequestDTO.builder()
                        .correo("juan@test.com")
                        .contrasena("password123")
                        .build())
                .build();

        EmpleadoResponseDTO empleado = empleadoService.crearEmpleado(empleadoDTO);
        this.empleadoId = empleado.getId();
    }

    @Test
    void crearEmpleado_retorna200() throws Exception {
        EmpleadoRequestDTO dto = EmpleadoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .nombre("Nuevo Empleado")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("987654321")
                .credencial(CredencialRequestDTO.builder()
                        .correo("nuevo@test.com")
                        .contrasena("pass123")
                        .build())
                .build();

        mockMvc.perform(post("/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo Empleado"))
                .andExpect(jsonPath("$.numeroDocumento").value("987654321"))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void listarEmpleados_retorna200ConContenido() throws Exception {
        mockMvc.perform(get("/empleados")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0]", notNullValue()));
    }

    @Test
    void listarPorEmpresa_retorna200() throws Exception {
        mockMvc.perform(get("/empleados/empresa/" + nitEmpresa)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void obtenerEmpleadoPorId_retorna200() throws Exception {
        mockMvc.perform(get("/empleados/" + empleadoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan Test"))
                .andExpect(jsonPath("$.id").value(empleadoId.intValue()))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void obtenerEmpleadoInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/empleados/99999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarEmpleado_retorna200() throws Exception {
        EmpleadoRequestDTO updateDTO = EmpleadoRequestDTO.builder()
                .nitEmpresa(nitEmpresa)
                .nombre("Nombre Actualizado")
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento("123456789")
                .credencial(CredencialRequestDTO.builder()
                        .correo("actualizado@test.com")
                        .contrasena("newpass")
                        .build())
                .build();

        mockMvc.perform(put("/empleados/" + empleadoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre Actualizado"));
    }

    @Test
    void eliminarEmpleado_retorna200() throws Exception {
        mockMvc.perform(delete("/empleados/" + empleadoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verificar que ya no existe
        mockMvc.perform(get("/empleados/" + empleadoId))
                .andExpect(status().isNotFound());
    }
}
