package com.proyecto.web.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "rol_empleado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolXEmpleado {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;
 
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}
 