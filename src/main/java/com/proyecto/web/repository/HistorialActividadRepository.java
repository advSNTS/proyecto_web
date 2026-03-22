package com.proyecto.web.repository;

import com.proyecto.web.entity.HistorialActividad;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 
public interface HistorialActividadRepository extends JpaRepository<HistorialActividad, Long> {
 
    List<HistorialActividad> findAllByActividad_IdOrderByFechaCambioDesc(Long idActividad);
 
    List<HistorialActividad> findAllByEmpleado_IdOrderByFechaCambioDesc(Long idEmpleado);
 
    List<HistorialActividad> findAllByActividad_IdAndTipoAccionOrderByFechaCambioDesc(Long idActividad, String tipoAccion);
}
 