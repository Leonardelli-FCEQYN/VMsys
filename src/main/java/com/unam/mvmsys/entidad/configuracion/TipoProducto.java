package com.unam.mvmsys.entidad.configuracion;

import com.unam.mvmsys.entidad.base.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipos_producto")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TipoProducto extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    private String descripcion;
}