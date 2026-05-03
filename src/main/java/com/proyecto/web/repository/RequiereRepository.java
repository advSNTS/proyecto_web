package com.proyecto.web.repository;

 
import com.proyecto.web.entity.Requiere;
import com.proyecto.web.entity.RequiereId;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
import java.util.Optional;
 
public interface RequiereRepository extends JpaRepository<Requiere, RequiereId> {
 
    // Roles activos requeridos por una actividad
    List<Requiere> findAllByActividad_IdAndDeletedFalse(Long actividadId);
 
    // Actividades activas que requieren un rol
    List<Requiere> findAllByRol_IdAndDeletedFalse(Long rolId);
 
    // Buscar relación puntual activa
    Optional<Requiere> findByActividad_IdAndRol_IdAndDeletedFalse(Long actividadId, Long rolId);
 
    // Validar duplicado antes de asignar
    boolean existsByActividad_IdAndRol_IdAndDeletedFalse(Long actividadId, Long rolId);
 
    // Cascada desde EmpresaXProceso: todos los Requiere donde el rol es de
    // una empresa concreta y la actividad pertenece a un proceso concreto
    List<Requiere> findAllByRol_Empresa_NitAndActividad_Nodo_Proceso_IdAndDeletedFalse(
            String nitEmpresa, Long idProceso);
}