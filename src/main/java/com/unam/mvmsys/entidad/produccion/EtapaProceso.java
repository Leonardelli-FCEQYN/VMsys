package com.unam.mvmsys.entidad.produccion;

import com.unam.mvmsys.entidad.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "EtapaProcesoProduccion")
@Table(name = "etapas_proceso", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"proceso_estandar_id", "orden_secuencia"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EtapaProceso extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_estandar_id", nullable = false)
    private ProcesoEstandar procesoEstandar;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "orden_secuencia", nullable = false)
    private Integer ordenSecuencia;

    @Column(name = "tiempo_estimado_minutos")
    @Builder.Default
    private Integer tiempoEstimadoMinutos = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;
}