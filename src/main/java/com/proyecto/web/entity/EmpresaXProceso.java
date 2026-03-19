package com.proyecto.web.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import com.proyecto.web.enums.Permiso;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "empresa_x_proceso")
@IdClass(EmpresaXProcesoId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaXProceso {

    @Id
    private String nit;

    @Id
    private Long idProceso;

    private String nitEmpresaOwner;

    @Enumerated(EnumType.STRING)
    private Permiso permiso;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}