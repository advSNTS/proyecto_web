package com.proyecto.web;

import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.EmpresaResponseDTO;
import com.proyecto.web.service.EmpresaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional  // ← cada test revierte sus cambios al terminar
class EmpresaServiceTest {

    @Autowired
    private EmpresaService empresaService;

    @Test
    void crearEmpresa_deberiaRetornarEmpresaCreada() {
        EmpresaRequestDTO dto = new EmpresaRequestDTO();
        dto.setNit("TEST-001");
        dto.setNombre("Empresa Test");
        dto.setCorreo("test@empresa.com");

        EmpresaResponseDTO response = empresaService.crearEmpresa(dto);

        assertNotNull(response);
        assertEquals("TEST-001", response.getNit());
        assertEquals("Empresa Test", response.getNombre());
    }

    @Test
    void obtenerEmpresa_deberiaRetornarEmpresaExistente() {
        // Crear primero
        EmpresaRequestDTO dto = new EmpresaRequestDTO();
        dto.setNit("TEST-002");
        dto.setNombre("Empresa Busqueda");
        dto.setCorreo("busqueda@empresa.com");
        empresaService.crearEmpresa(dto);

        // Luego buscar
        EmpresaResponseDTO response = empresaService.obtenerEmpresa("TEST-002");

        assertNotNull(response);
        assertEquals("Empresa Busqueda", response.getNombre());
    }

    @Test
    void obtenerEmpresas_deberiaRetornarLista() {
        EmpresaRequestDTO dto = new EmpresaRequestDTO();
        dto.setNit("TEST-003");
        dto.setNombre("Empresa Lista");
        dto.setCorreo("lista@empresa.com");
        empresaService.crearEmpresa(dto);

        List<EmpresaResponseDTO> lista = empresaService.obtenerEmpresas();

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
    }

    @Test
    void actualizarEmpresa_deberiaRetornarEmpresaActualizada() {
        EmpresaRequestDTO dto = new EmpresaRequestDTO();
        dto.setNit("TEST-004");
        dto.setNombre("Original");
        dto.setCorreo("original@empresa.com");
        empresaService.crearEmpresa(dto);

        EmpresaRequestDTO update = new EmpresaRequestDTO();
        update.setNombre("Actualizada");
        update.setCorreo("nueva@empresa.com");

        EmpresaResponseDTO response = empresaService.actualizarEmpresa("TEST-004", update);

        assertEquals("Actualizada", response.getNombre());
        assertEquals("nueva@empresa.com", response.getCorreo());
    }

    @Test
    void eliminarEmpresa_deberiaMarcarComoEliminada() {
        EmpresaRequestDTO dto = new EmpresaRequestDTO();
        dto.setNit("TEST-005");
        dto.setNombre("Para Eliminar");
        dto.setCorreo("eliminar@empresa.com");
        empresaService.crearEmpresa(dto);

        empresaService.eliminarEmpresa("TEST-005");

        assertThrows(RuntimeException.class, () -> empresaService.obtenerEmpresa("TEST-005"));
    }
}