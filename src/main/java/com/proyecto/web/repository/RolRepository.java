package com.proyecto.web.repository;
 
import com.proyecto.web.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
import java.util.Optional;
 
public interface RolRepository extends JpaRepository<Rol, Long> {
 
    List<Rol> findAllByDeletedFalse();
 
    Optional<Rol> findByIdAndDeletedFalse(Long id);
 
    List<Rol> findAllByEmpresa_NitAndDeletedFalse(String nit);
}
 