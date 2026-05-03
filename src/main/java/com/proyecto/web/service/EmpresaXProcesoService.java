package com.proyecto.web.service;

import com.proyecto.web.dto.EmpresaXProcesoRequestDTO;
import com.proyecto.web.dto.EmpresaXProcesoResponseDTO;
import com.proyecto.web.entity.Empresa;
import com.proyecto.web.entity.EmpresaXProceso;
import com.proyecto.web.entity.Proceso;
import com.proyecto.web.entity.Requiere;
import com.proyecto.web.mapper.EmpresaXProcesoMapper;
import com.proyecto.web.repository.EmpresaRepository;
import com.proyecto.web.repository.EmpresaXProcesoRepository;
import com.proyecto.web.repository.ProcesoRepository;
import com.proyecto.web.repository.RequiereRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class EmpresaXProcesoService {
 
    private final EmpresaXProcesoRepository empresaXProcesoRepository;
    private final EmpresaRepository empresaRepository;
    private final ProcesoRepository procesoRepository;
    private final RequiereRepository requiereRepository;
 
    public EmpresaXProcesoResponseDTO asignarProceso(EmpresaXProcesoRequestDTO dto) {
        if (empresaXProcesoRepository.existsByEmpresa_NitAndProceso_IdAndDeletedFalse(dto.getNitEmpresa(), dto.getIdProceso())) {
            throw new RuntimeException("La empresa ya tiene asignado ese proceso");
        }
 
        Empresa empresa = empresaRepository.findByNitAndDeletedFalse(dto.getNitEmpresa())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
 
        Proceso proceso = procesoRepository.findByIdAndActivoTrue(dto.getIdProceso())
                .orElseThrow(() -> new RuntimeException("Proceso no encontrado"));
 
        Empresa owner = empresaRepository.findByNitAndDeletedFalse(dto.getNitOwner())
                .orElseThrow(() -> new RuntimeException("Empresa owner no encontrada"));
 
        EmpresaXProceso ep = EmpresaXProcesoMapper.toEntity(empresa, proceso, owner, dto.getPermiso());
        return EmpresaXProcesoMapper.toResponse(empresaXProcesoRepository.save(ep));
    }
 
    public List<EmpresaXProcesoResponseDTO> obtenerProcesosPorEmpresa(String nit) {
        return empresaXProcesoRepository.findAllByEmpresa_NitAndDeletedFalse(nit)
                .stream()
                .map(EmpresaXProcesoMapper::toResponse)
                .toList();
    }
 
    public List<EmpresaXProcesoResponseDTO> obtenerEmpresasPorProceso(Long idProceso) {
        return empresaXProcesoRepository.findAllByProceso_IdAndDeletedFalse(idProceso)
                .stream()
                .map(EmpresaXProcesoMapper::toResponse)
                .toList();
    }
 
    public List<EmpresaXProcesoResponseDTO> obtenerProcesosComoOwner(String nit) {
        return empresaXProcesoRepository.findAllByEmpresaOwner_NitAndDeletedFalse(nit)
                .stream()
                .map(EmpresaXProcesoMapper::toResponse)
                .toList();
    }
 
    public EmpresaXProcesoResponseDTO actualizarPermiso(String nit, Long idProceso, EmpresaXProcesoRequestDTO dto) {
        EmpresaXProceso ep = empresaXProcesoRepository
                .findByEmpresa_NitAndProceso_IdAndDeletedFalse(nit, idProceso)
                .orElseThrow(() -> new RuntimeException("Relación no encontrada"));
 
        ep.setPermiso(dto.getPermiso());
        return EmpresaXProcesoMapper.toResponse(empresaXProcesoRepository.save(ep));
    }
 
    /**
     * Soft delete de la relación empresa-proceso.
     * Cascada: desactiva todos los Requiere donde el rol pertenece a esta empresa
     * y la actividad pertenece a este proceso — ya que esa combinación deja de ser válida.
     */
    @Transactional
    public void quitarProceso(String nit, Long idProceso) {
        EmpresaXProceso ep = empresaXProcesoRepository
                .findByEmpresa_NitAndProceso_IdAndDeletedFalse(nit, idProceso)
                .orElseThrow(() -> new RuntimeException("Relación no encontrada"));
 
        ep.setDeleted(true);
        empresaXProcesoRepository.save(ep);
 
        // Cascada: invalidar Requiere huérfanos para esta combinación empresa-proceso
        List<Requiere> huerfanos = requiereRepository
                .findAllByRol_Empresa_NitAndActividad_Nodo_Proceso_IdAndDeletedFalse(nit, idProceso);
 
        huerfanos.forEach(r -> r.setDeleted(true));
        requiereRepository.saveAll(huerfanos);
    }
}