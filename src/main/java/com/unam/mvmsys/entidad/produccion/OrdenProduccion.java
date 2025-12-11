package com.unam.mvmsys.entidad.produccion;

import com.unam.mvmsys.entidad.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordenes_produccion")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrdenProduccion extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proceso_estandar_id", nullable = false)
    private ProcesoEstandar procesoEstandar;

    @Column(name = "cantidad_planificada", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadPlanificada;

    @Column(name = "cantidad_real_obtenida", precision = 12, scale = 3)
    @Builder.Default
    private BigDecimal cantidadRealObtenida = BigDecimal.ZERO;

    @Column(name = "fecha_emision", nullable = false)
    @Builder.Default
    private LocalDateTime fechaEmision = LocalDateTime.now();

    @Column(name = "fecha_inicio_estimada")
    private LocalDateTime fechaInicioEstimada;

    @Column(name = "fecha_fin_estimada")
    private LocalDateTime fechaFinEstimada;

    @Column(name = "fecha_inicio_real")
    private LocalDateTime fechaInicioReal;

    @Column(name = "fecha_fin_real")
    private LocalDateTime fechaFinReal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoOrden estado = EstadoOrden.PLANIFICADA;

    @Column(length = 10)
    @Builder.Default
    private String prioridad = "MEDIA"; 

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    // Seguimiento de ejecución
    @OneToMany(mappedBy = "ordenProduccion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaInicio ASC")
    @Builder.Default
    private List<OrdenProduccionEtapa> etapasSeguimiento = new ArrayList<>();

    // Reservas de stock asociadas
    @OneToMany(mappedBy = "ordenProduccion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReservaStockProduccion> reservas = new ArrayList<>();

    public enum EstadoOrden {
        PLANIFICADA, EN_PROCESO, PAUSADA, FINALIZADA, CANCELADA
    }
}