package com.unam.mvmsys.entidad.produccion;

import com.unam.mvmsys.entidad.base.BaseEntity;
import com.unam.mvmsys.entidad.seguridad.Persona;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ejecucion_etapas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EjecucionEtapa extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "orden_id", nullable = false)
    private OrdenProduccion orden;

    @Column(name = "nombre_etapa", nullable = false)
    private String nombreEtapa;

    @Column(name = "orden_secuencia", nullable = false)
    private Integer ordenSecuencia;
    
    @Column(name = "tiempo_estimado_minutos")
    private Integer tiempoEstimadoMinutos;

    @Column(length = 20)
    @Builder.Default
    private String estado = "PENDIENTE";

    @ManyToOne
    @JoinColumn(name = "operador_id")
    private Persona operador;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;
    
    @Column(name = "cantidad_resultante", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal cantidadResultante = BigDecimal.ZERO;
    
    @Column(name = "merma_registrada", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal mermaRegistrada = BigDecimal.ZERO;
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;
}
