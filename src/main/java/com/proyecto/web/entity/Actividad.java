package com.proyecto.web.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "actividades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Actividad {
 
    // La PK es la misma FK al nodo — relación 1 a 1
    @Id
    private Long id;
 
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "nodo_id")
    private Nodo nodo;
 
    @Column(length = 255)
    private String descripcion;
 
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}
 