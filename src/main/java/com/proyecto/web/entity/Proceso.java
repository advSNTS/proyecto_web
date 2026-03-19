package com.proyecto.web.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "procesos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(length = 100)
    private String categoria;

    @Column(nullable = false)
    private Boolean borrador;

    @Column(nullable = false)
    private Boolean activo;

    @OneToMany(mappedBy = "proceso")
    private List<Nodo> nodos;
}