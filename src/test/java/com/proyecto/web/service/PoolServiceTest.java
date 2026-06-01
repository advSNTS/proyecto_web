package com.proyecto.web.service;

import com.proyecto.web.dto.PoolRequestDTO;
import com.proyecto.web.dto.PoolResponseDTO;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.entity.Pool;
import com.proyecto.web.exception.BusinessException;
import com.proyecto.web.repository.EmpresaRepository;
import com.proyecto.web.repository.PoolRepository;
import com.proyecto.web.support.TestSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("PoolService Tests")
class PoolServiceTest {

    @Autowired
    private PoolService poolService;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PoolRepository poolRepository;

    private Empresa empresa;
    private String nitEmpresa = "987654321";

    @BeforeEach
    void setUp() {
        // Crear empresa
        empresa = new Empresa();
        empresa.setNit(nitEmpresa);
        empresa.setNombre("Empresa Test");
        empresa.setDeleted(false);
        empresaRepository.save(empresa);
        TestSecurityContext.authenticate(nitEmpresa);
    }

    @Test
    @DisplayName("Listar pools por empresa exitosamente")
    void testListarPorEmpresa_Success() {
        // Crear varios pools
        Pool pool1 = new Pool();
        pool1.setEmpresa(empresa);
        pool1.setNombre("Pool 1");
        pool1.setDescripcion("Descripción 1");
        pool1.setEsDefault(true);
        pool1.setEliminado(false);
        poolRepository.save(pool1);

        Pool pool2 = new Pool();
        pool2.setEmpresa(empresa);
        pool2.setNombre("Pool 2");
        pool2.setDescripcion("Descripción 2");
        pool2.setEsDefault(false);
        pool2.setEliminado(false);
        poolRepository.save(pool2);

        List<PoolResponseDTO> result = poolService.listarPorEmpresa();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Listar pools retorna vacío si no hay")
    void testListarPorEmpresa_Empty() {
        List<PoolResponseDTO> result = poolService.listarPorEmpresa();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("No lista pools eliminados")
    void testListarPorEmpresa_NoEliminados() {
        // Crear pool activo
        Pool activo = new Pool();
        activo.setEmpresa(empresa);
        activo.setNombre("Pool Activo");
        activo.setDescripcion("Descripción");
        activo.setEsDefault(false);
        activo.setEliminado(false);
        poolRepository.save(activo);

        // Crear pool eliminado
        Pool eliminado = new Pool();
        eliminado.setEmpresa(empresa);
        eliminado.setNombre("Pool Eliminado");
        eliminado.setDescripcion("Descripción");
        eliminado.setEsDefault(false);
        eliminado.setEliminado(true);
        poolRepository.save(eliminado);

        List<PoolResponseDTO> result = poolService.listarPorEmpresa();

        assertEquals(1, result.size());
        assertEquals("Pool Activo", result.get(0).getNombre());
    }

    @Test
    @DisplayName("Obtener pool por ID exitosamente")
    void testObtener_Success() {
        Pool pool = new Pool();
        pool.setEmpresa(empresa);
        pool.setNombre("Pool Test");
        pool.setDescripcion("Descripción test");
        pool.setEsDefault(false);
        pool.setEliminado(false);
        pool = poolRepository.save(pool);

        PoolResponseDTO result = poolService.obtener(pool.getId());

        assertNotNull(result);
        assertEquals(pool.getId(), result.getId());
        assertEquals("Pool Test", result.getNombre());
    }

    @Test
    @DisplayName("Fallar si pool no existe")
    void testObtener_NotFound() {
        assertThrows(BusinessException.class, () -> poolService.obtener(9999L));
    }

    @Test
    @DisplayName("Fallar si pool está eliminado")
    void testObtener_Eliminado() {
        Pool poolEliminado = new Pool();
        poolEliminado.setEmpresa(empresa);
        poolEliminado.setNombre("Pool Eliminado");
        poolEliminado.setDescripcion("Descripción");
        poolEliminado.setEsDefault(false);
        poolEliminado.setEliminado(true);
        poolEliminado = poolRepository.save(poolEliminado);
        Long poolId = poolEliminado.getId();

        assertThrows(BusinessException.class, () -> poolService.obtener(poolId));
    }

    @Test
    @DisplayName("Crear pool exitosamente")
    void testCrear_Success() {
        PoolRequestDTO dto = PoolRequestDTO.builder()
                .nombre("Pool Nuevo")
                .descripcion("Descripción nueva")
                .esDefault(false)
                .build();

        PoolResponseDTO result = poolService.crear( dto);

        assertNotNull(result.getId());
        assertEquals("Pool Nuevo", result.getNombre());
        assertEquals("Descripción nueva", result.getDescripcion());
        assertFalse(result.isEsDefault());
    }

    @Test
    @DisplayName("Crear pool como default exitosamente")
    void testCrear_AsDefault() {
        PoolRequestDTO dto = PoolRequestDTO.builder()
                .nombre("Pool Default")
                .descripcion("Pool por defecto")
                .esDefault(true)
                .build();

        PoolResponseDTO result = poolService.crear( dto);

        assertTrue(result.isEsDefault());
    }

    @Test
    @DisplayName("Fallar si nombre es nulo")
    void testCrear_NombreNulo() {
        PoolRequestDTO dto = PoolRequestDTO.builder()
                .nombre(null)
                .descripcion("Descripción")
                .esDefault(false)
                .build();

        assertThrows(BusinessException.class, () -> poolService.crear( dto));
    }

    @Test
    @DisplayName("Fallar si nombre está vacío")
    void testCrear_NombreVacio() {
        PoolRequestDTO dto = PoolRequestDTO.builder()
                .nombre("   ")
                .descripcion("Descripción")
                .esDefault(false)
                .build();

        assertThrows(BusinessException.class, () -> poolService.crear( dto));
    }

    @Test
    @DisplayName("Fallar si nombre es duplicado")
    void testCrear_NombreDuplicado() {
        // Crear primer pool
        PoolRequestDTO dto1 = PoolRequestDTO.builder()
                .nombre("Pool Duplicado")
                .descripcion("Descripción 1")
                .esDefault(false)
                .build();

        poolService.crear( dto1);

        // Intentar crear otro con mismo nombre
        PoolRequestDTO dto2 = PoolRequestDTO.builder()
                .nombre("Pool Duplicado")
                .descripcion("Descripción 2")
                .esDefault(false)
                .build();

        assertThrows(BusinessException.class, () -> poolService.crear( dto2));
    }

    @Test
    @DisplayName("Cambiar default anterior cuando se crea nuevo default")
    void testCrear_CambiarDefault() {
        // Crear primer pool default
        PoolRequestDTO dto1 = PoolRequestDTO.builder()
                .nombre("Pool Default 1")
                .descripcion("Primer default")
                .esDefault(true)
                .build();

        PoolResponseDTO result1 = poolService.crear( dto1);
        assertTrue(result1.isEsDefault());

        // Crear segundo pool como default
        PoolRequestDTO dto2 = PoolRequestDTO.builder()
                .nombre("Pool Default 2")
                .descripcion("Segundo default")
                .esDefault(true)
                .build();

        PoolResponseDTO result2 = poolService.crear( dto2);
        assertTrue(result2.isEsDefault());

        // Verificar que el primero ya no es default
        PoolResponseDTO verificacion1 = poolService.obtener(result1.getId());
        assertFalse(verificacion1.isEsDefault());
    }

    @Test
    @DisplayName("Crear pool con descripción nula")
    void testCrear_DescripcionNula() {
        PoolRequestDTO dto = PoolRequestDTO.builder()
                .nombre("Pool Test")
                .descripcion(null)
                .esDefault(false)
                .build();

        PoolResponseDTO result = poolService.crear( dto);

        assertNotNull(result.getId());
        assertNull(result.getDescripcion());
    }

    @Test
    @DisplayName("Crear pool con esDefault nulo (debe ser false)")
    void testCrear_EsDefaultNulo() {
        PoolRequestDTO dto = PoolRequestDTO.builder()
                .nombre("Pool Test")
                .descripcion("Descripción")
                .esDefault(null)
                .build();

        PoolResponseDTO result = poolService.crear( dto);

        assertFalse(result.isEsDefault());
    }

    @Test
    @DisplayName("Trim nombre al crear pool")
    void testCrear_TrimNombre() {
        PoolRequestDTO dto = PoolRequestDTO.builder()
                .nombre("  Pool Test  ")
                .descripcion("Descripción")
                .esDefault(false)
                .build();

        PoolResponseDTO result = poolService.crear( dto);

        assertEquals("Pool Test", result.getNombre());
    }
}
