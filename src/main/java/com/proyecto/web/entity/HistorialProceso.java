package com.proyecto.web.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "historial_proceso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialProceso {

    @Id
    private Long id;

    private Long idEmpleado;

    private Long idProceso;

    private String valorAnterior;

    private String valorNuevo;

    private Date fechaCambio;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}