package com.proyecto.web.repository;
 
import com.proyecto.web.entity.RolXEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
import java.util.Optional;
 
public interface RolXEmpleadoRepository extends JpaRepository<RolXEmpleado, Long> {
 
    List<RolXEmpleado> findAllByDeletedFalse();
 
    Optional<RolXEmpleado> findByIdAndDeletedFalse(Long id);
 
    // Roles activos de un empleado
    List<RolXEmpleado> findAllByEmpleado_IdAndDeletedFalse(Long empleadoId);
 
    // Empleados activos de un rol
    List<RolXEmpleado> findAllByRol_IdAndDeletedFalse(Long rolId);
 
    // Validar duplicado antes de asignar
    boolean existsByEmpleado_IdAndRol_IdAndDeletedFalse(Long empleadoId, Long rolId);
}