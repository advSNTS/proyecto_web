package com.proyecto.web.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//para llave compuesta

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EmpresaProcesoId implements Serializable {
    private String empresa;      // mapea al campo nit en EmpresaXProceso
    private Long proceso;        // mapea al campo id en EmpresaXProceso
}
 