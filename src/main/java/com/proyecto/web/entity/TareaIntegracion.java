package com.proyecto.web.entity;

import jakarta.persistence.Column;
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
@Table(name = "tareas_integracion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TareaIntegracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mensaje_externo_id", nullable = false)
    private MensajeExterno mensajeExterno;

    @Column(name = "payload_mapping", columnDefinition = "TEXT")
    private String payloadMapping;

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;
}
