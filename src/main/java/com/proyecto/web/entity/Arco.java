package com.proyecto.web.entity;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;




@Entity
@Table(name = "Arcos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Arco {

    @Id
    private Long id;
    private Long ID_proceso;
    private Long Nodo_origen;
    private Long Nodo_destino;
    
}
