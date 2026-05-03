package com.proyecto.web.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "arcos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Arco {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proceso", nullable = false)
    private Proceso proceso;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nodo_origen", nullable = false)
    private Nodo nodoOrigen;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nodo_destino", nullable = false)
    private Nodo nodoDestino;
}
 