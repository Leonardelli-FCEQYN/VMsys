package com.unam.mvmsys.entidad.stock;

import com.unam.mvmsys.entidad.base.BaseEntity;
import com.unam.mvmsys.entidad.configuracion.Estado;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lotes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Lote extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "estado_id", nullable = false)
    private Estado estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @Column(name = "costo_unitario_promedio", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal costoUnitarioPromedio = BigDecimal.ZERO;

    // CORRECCIÓN: Usamos columnDefinition="TEXT" y quitamos @Lob
    @Column(columnDefinition = "TEXT")
    private String observaciones;
}