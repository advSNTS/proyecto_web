package com.proyecto.web.repository;
 
import com.proyecto.web.entity.Gateway;
import com.proyecto.web.enums.TipoGateway;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
import java.util.Optional;
 
public interface GatewayRepository extends JpaRepository<Gateway, Long> {
 
    Optional<Gateway> findByIdAndDeletedFalse(Long id);
 
    // Todos los gateways activos de un proceso (navegando por el nodo)
    List<Gateway> findAllByNodo_Proceso_IdAndDeletedFalse(Long procesoId);
 
    // Filtrar por tipo dentro de un proceso
    List<Gateway> findAllByNodo_Proceso_IdAndTipoGatewayAndDeletedFalse(Long procesoId, TipoGateway tipoGateway);
 
    // Validar que el nodo no tenga ya un gateway asignado
    boolean existsByNodo_IdAndDeletedFalse(Long nodoId);
}