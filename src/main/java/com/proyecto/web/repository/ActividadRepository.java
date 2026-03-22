package com.proyecto.web.repository;

import com.proyecto.web.entity.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
import java.util.Optional;
 
public interface ActividadRepository extends JpaRepository<Actividad, Long> {
 
    Optional<Actividad> findByIdAndDeletedFalse(Long id);
 
    List<Actividad> findAllByDeletedFalse();
 
    // Todas las actividades activas de un proceso (navegando por el nodo)
    List<Actividad> findAllByNodo_Proceso_IdAndDeletedFalse(Long procesoId);
 
    // Validar que el nodo no tenga ya una actividad asignada
    boolean existsByNodo_IdAndDeletedFalse(Long nodoId);
}
 