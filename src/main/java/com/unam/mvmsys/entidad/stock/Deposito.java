package com.unam.mvmsys.entidad.stock;

import com.unam.mvmsys.entidad.base.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "depositos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Deposito extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 200)
    private String direccion;

    @Column(name = "es_propio", nullable = false)
    @Builder.Default
    private boolean esPropio = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;
}