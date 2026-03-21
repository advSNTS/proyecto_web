package com.proyecto.web.repository;

 
import com.proyecto.web.entity.HistorialProceso;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 
public interface HistorialProcesoRepository extends JpaRepository<HistorialProceso, Long> {
 
    List<HistorialProceso> findAllByProceso_IdOrderByFechaCambioDesc(Long idProceso);
 
    // Cambios hechos por un empleado específico
    List<HistorialProceso> findAllByEmpleado_IdOrderByFechaCambioDesc(Long idEmpleado);
 
    List<HistorialProceso> findAllByProceso_IdAndTipoAccionOrderByFechaCambioDesc(Long idProceso, String tipoAccion);
}
 