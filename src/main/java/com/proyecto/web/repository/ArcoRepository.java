package com.proyecto.web.repository;
 
import com.proyecto.web.entity.Arco;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 
public interface ArcoRepository extends JpaRepository<Arco, Long> {
 
    // Todos los arcos de un proceso
    List<Arco> findAllByProceso_Id(Long idProceso);
 
    // Arcos que salen de un nodo origen
    List<Arco> findAllByNodoOrigen_Id(Long nodoOrigenId);
 
    // Arcos que llegan a un nodo destino
    List<Arco> findAllByNodoDestino_Id(Long nodoDestinoId);
 
    // Validar que no exista ya ese arco en el mismo proceso
    boolean existsByProceso_IdAndNodoOrigen_IdAndNodoDestino_Id(
            Long idProceso, Long nodoOrigenId, Long nodoDestinoId);
}
 