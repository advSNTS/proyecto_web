package com.proyecto.web.entity;
 
import com.proyecto.web.enums.Permiso;
import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nit", nullable = false)
    private Empresa empresa;
 
    private String nombre;

    @Column(length = 500)
    private String descripcion;
 
    @Enumerated(EnumType.STRING)
    private Permiso permiso;
 
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}
 