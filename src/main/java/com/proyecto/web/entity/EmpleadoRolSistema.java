package com.proyecto.web.entity;

import com.proyecto.web.enums.TipoRolSistema;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "empleado_rol_sistema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpleadoRolSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    /**
     * Empresa del alcance del rol; null si aplica solo vía {@link Empleado#getEmpresa()} o administración global.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nit")
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_rol", nullable = false, length = 20)
    private TipoRolSistema tipoRol;

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;
}
