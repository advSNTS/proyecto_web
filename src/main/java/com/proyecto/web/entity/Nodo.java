package com.proyecto.web.entity;

import com.proyecto.web.enums.TipoNodo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "nodos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nodo {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private TipoNodo tipo;
 
    @Column(nullable = false, length = 100)
    private String nombre;
}
 