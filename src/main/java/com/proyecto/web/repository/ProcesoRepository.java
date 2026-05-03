package com.proyecto.web.repository;
 
import com.proyecto.web.entity.Proceso;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
import java.util.Optional;
 
public interface ProcesoRepository extends JpaRepository<Proceso, Long> {
 
    // Activos (borrador=false, activo=true)
    List<Proceso> findAllByActivoTrue();
 
    Optional<Proceso> findByIdAndActivoTrue(Long id);
 
    List<Proceso> findAllByCategoria(String categoria);
}
 