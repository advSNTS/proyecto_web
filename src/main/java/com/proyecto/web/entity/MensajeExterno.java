package com.proyecto.web.entity;

import com.proyecto.web.enums.TipoDestinoMensajeExterno;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mensajes_externos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeExterno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "destino_tipo", nullable = false, length = 30)
    private TipoDestinoMensajeExterno destinoTipo;

    @Column(columnDefinition = "TEXT")
    private String configuracion;

    @Column(columnDefinition = "TEXT")
    private String credenciales;

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;
}
