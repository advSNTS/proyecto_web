package com.proyecto.web.service;

import com.proyecto.web.dto.DashboardMetricsDTO;
import com.proyecto.web.enums.EstadoProceso;
import com.proyecto.web.repository.ActividadRepository;
import com.proyecto.web.repository.ArcoRepository;
import com.proyecto.web.repository.EmpleadoRepository;
import com.proyecto.web.repository.GatewayRepository;
import com.proyecto.web.repository.LaneRepository;
import com.proyecto.web.repository.PoolRepository;
import com.proyecto.web.repository.ProcesoRepository;
import com.proyecto.web.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstadisticasService {

    private final ProcesoRepository procesoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ActividadRepository actividadRepository;
    private final GatewayRepository gatewayRepository;
    private final PoolRepository poolRepository;
    private final LaneRepository laneRepository;
    private final ArcoRepository arcoRepository;

    @Transactional(readOnly = true)
    public DashboardMetricsDTO obtenerDashboard() {
        String nit = SecurityUtils.requireAuthenticatedNitEmpresa();
        log.debug("Obteniendo metricas para empresa: {}", nit);

        long totalProcesos = procesoRepository.countByEmpresa_Nit(nit);
        long procesosBorrador = procesoRepository.countByEmpresa_NitAndEstado(nit, EstadoProceso.BORRADOR);
        long procesosPublicado = procesoRepository.countByEmpresa_NitAndEstado(nit, EstadoProceso.PUBLICADO);
        long procesosInactivo = procesoRepository.countByEmpresa_NitAndEstado(nit, EstadoProceso.INACTIVO);

        long totalEmpleados = empleadoRepository.countByEmpresa_NitAndDeletedFalse(nit);

        long totalActividades = actividadRepository.findAllByNodo_Proceso_Empresa_NitAndDeletedFalse(nit).size();
        long totalGateways = gatewayRepository.findAllByNodo_Proceso_Empresa_NitAndDeletedFalse(nit).size();
        long totalArcos = arcoRepository.findAllByProceso_Empresa_NitAndEliminadoFalse(nit).size();

        long totalPools = poolRepository.findAllByEmpresa_NitAndEliminadoFalse(nit).size();
        long totalLanes = laneRepository.findAllByPool_Empresa_NitAndEliminadoFalse(nit).size();

        return DashboardMetricsDTO.builder()
                .totalProcesos(totalProcesos)
                .procesosBorrador(procesosBorrador)
                .procesosPublicado(procesosPublicado)
                .procesosInactivo(procesosInactivo)
                .totalEmpleados(totalEmpleados)
                .totalActividades(totalActividades)
                .totalGateways(totalGateways)
                .totalPools(totalPools)
                .totalLanes(totalLanes)
                .totalArcos(totalArcos)
                .cambiosRecientes(Collections.emptyList())
                .build();
    }
}
