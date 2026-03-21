package com.proyecto.web.entity;

import com.proyecto.web.enums.Permiso;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "empresa_proceso")
@IdClass(EmpresaProcesoId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaXProceso {
 
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nit", nullable = false)
    private Empresa empresa;
 
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proceso", nullable = false)
    private Proceso proceso;
 
    // Empresa dueña del proceso (owner)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nit_owner", nullable = false)
    private Empresa empresaOwner;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Permiso permiso;
 
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}