package com.proyecto.web.support;

import com.proyecto.web.dto.EmpresaRequestDTO;
import com.proyecto.web.dto.ProcesoRequestDTO;
import com.proyecto.web.dto.ProcesoResponseDTO;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.entity.Nodo;
import com.proyecto.web.entity.Pool;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.enums.EstadoProceso;
import com.proyecto.web.enums.TipoNodo;
import com.proyecto.web.repository.EmpresaRepository;
import com.proyecto.web.repository.NodoRepository;
import com.proyecto.web.repository.PoolRepository;
import com.proyecto.web.repository.ProcesoRepository;
import com.proyecto.web.service.EmpresaService;
import com.proyecto.web.service.ProcesoService;

/**
 * Utilidades compartidas para tests de integración con H2.
 */
public final class IntegrationTestData {

    private IntegrationTestData() {
    }

    public static ProcesoResponseDTO crearEmpresaYProceso(
            EmpresaService empresaService,
            ProcesoService procesoService,
            String nit) {
        EmpresaRequestDTO empresa = new EmpresaRequestDTO();
        empresa.setNit(nit);
        empresa.setNombre("Empresa Test " + nit);
        empresa.setCorreo("contacto@" + nit.replaceAll("[^a-zA-Z0-9]", "") + ".test");
        empresaService.crearEmpresa(empresa);

        TestSecurityContext.authenticate(nit);
        return procesoService.crearProceso(ProcesoRequestDTO.builder()
                .nitEmpresa(nit)
                .nombre("Proceso Test")
                .descripcion("Descripción de prueba")
                .categoria("General")
                .borrador(false)
                .activo(true)
                .build());
    }

    public static Proceso cargarProceso(ProcesoRepository procesoRepository, Long procesoId) {
        return procesoRepository.findById(procesoId).orElseThrow();
    }

    public static Proceso crearProcesoInactivo(
            ProcesoRepository procesoRepository,
            EmpresaRepository empresaRepository,
            PoolRepository poolRepository,
            String nit,
            String nombre) {
        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(nit).orElseThrow();
        Pool pool = poolRepository.findByEmpresa_NitAndEsDefaultTrueAndEliminadoFalse(nit).orElseThrow();

        Proceso inactivo = Proceso.builder()
                .nombre(nombre)
                .descripcion("Proceso inactivo")
                .empresa(empresa)
                .pool(pool)
                .estado(EstadoProceso.INACTIVO)
                .build();

        return procesoRepository.save(inactivo);
    }

    public static Nodo crearNodo(
            NodoRepository nodoRepository,
            Proceso proceso,
            TipoNodo tipo,
            String nombre,
            long x,
            long y) {
        Nodo nodo = Nodo.builder()
                .proceso(proceso)
                .tipo(tipo)
                .nombre(nombre)
                .coordenadaX(x)
                .coordenadaY(y)
                .eliminado(false)
                .build();
        return nodoRepository.save(nodo);
    }
}
