package com.unam.mvmsys.entidad.configuracion;

import com.unam.mvmsys.entidad.base.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "unidades_medida")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UnidadMedida extends BaseEntity {

    @Column(nullable = false, unique = true, length = 10)
    private String codigo; // m2, un, kg

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(name = "permite_decimales", nullable = false)
    private boolean permiteDecimales;
}