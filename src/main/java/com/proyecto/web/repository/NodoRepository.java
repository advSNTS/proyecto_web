package com.proyecto.web.repository;

import com.proyecto.web.entity.Nodo;
import com.proyecto.web.enums.TipoNodo;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 
public interface NodoRepository extends JpaRepository<Nodo, Long> {
 
    // Todos los nodos de un proceso
    List<Nodo> findAllByProceso_Id(Long idProceso);
 
    // Nodos de un proceso filtrados por tipo
    List<Nodo> findAllByProceso_IdAndTipo(Long idProceso, TipoNodo tipo);
}
 