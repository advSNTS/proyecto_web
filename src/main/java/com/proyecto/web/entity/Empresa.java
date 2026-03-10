package com.proyecto.web.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "empresa")
public class Empresa {
    
    @Id
    @Column(name = "nit", nullable = false, unique = true)
    private String nit;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "correo", nullable = false)
    private String correo;

    public Empresa() {
    }

    public Empresa(String nit, String nombre, String correo) {
        this.nit = nit;
        this.nombre = nombre;
        this.correo = correo;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
