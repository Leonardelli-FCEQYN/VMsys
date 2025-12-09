package com.unam.mvmsys.entidad.configuracion;

import com.unam.mvmsys.entidad.base.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "etapas_proceso")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EtapaProceso extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "orden_sugerido")
    private Integer ordenSugerido;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;
}