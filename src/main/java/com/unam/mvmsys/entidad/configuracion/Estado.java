package com.unam.mvmsys.entidad.configuracion;

import com.unam.mvmsys.entidad.base.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estados")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Estado extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "entidad_sistema_id", nullable = false)
    private EntidadSistema entidadSistema;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @Column(name = "es_inicial", nullable = false)
    private boolean esInicial;

    @Column(name = "es_final", nullable = false)
    private boolean esFinal;
}