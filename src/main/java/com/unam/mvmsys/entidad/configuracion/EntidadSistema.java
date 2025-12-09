package com.unam.mvmsys.entidad.configuracion;

import com.unam.mvmsys.entidad.base.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "entidades_sistema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EntidadSistema extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String codigo; // Ej: 'PEDIDO', 'LOTE'

    @Column(nullable = false, length = 100)
    private String nombre;

    private String descripcion;
}