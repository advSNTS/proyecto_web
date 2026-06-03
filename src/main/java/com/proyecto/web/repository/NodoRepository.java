package com.proyecto.web.repository;

import com.proyecto.web.entity.Nodo;
import com.proyecto.web.enums.TipoNodo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
 
public interface NodoRepository extends JpaRepository<Nodo, Long> {
 
    List<Nodo> findAllByProceso_IdAndEliminadoFalse(Long idProceso);

    List<Nodo> findAllByProceso_IdAndTipoAndEliminadoFalse(Long idProceso, TipoNodo tipo);

    List<Nodo> findAllByProceso_Empresa_NitAndEliminadoFalse(String nit);

    Optional<Nodo> findByIdAndEliminadoFalse(Long id);
}
 