package com.proyecto.web.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "empleado_x_rol")
@IdClass(EmpleadoXRolId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpleadoxRol {

    @Id
    private Long empleadoId;

    @Id
    private Long rolId;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}