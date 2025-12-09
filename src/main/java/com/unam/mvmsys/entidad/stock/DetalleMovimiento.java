package com.unam.mvmsys.entidad.stock;

import com.unam.mvmsys.entidad.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_movimiento")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DetalleMovimiento extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "movimiento_id", nullable = false)
    private MovimientoStock movimiento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal cantidad;
}
