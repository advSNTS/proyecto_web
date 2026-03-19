package com.proyecto.web.entity;

import com.proyecto.web.enums.TipoGateway;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gateways")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gateway {

    @Id
    private Long nodoId;

    @Enumerated(EnumType.STRING)
    private TipoGateway tipoGateway;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}