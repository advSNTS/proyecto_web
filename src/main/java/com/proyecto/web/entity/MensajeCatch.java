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
@Table(name = "mensajes_catch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeCatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @Column(name = "nombre_mensaje", nullable = false, length = 200)
    private String nombreMensaje;

    @Column(name = "correlacion_expr", length = 500)
    private String correlacionExpr;

    @Column(name = "iniciar_nueva_instancia", nullable = false)
    @Builder.Default
    private boolean iniciarNuevaInstancia = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;
}
