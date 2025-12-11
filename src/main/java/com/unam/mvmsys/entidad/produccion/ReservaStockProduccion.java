package com.unam.mvmsys.entidad.produccion;

import com.unam.mvmsys.entidad.base.BaseEntity;
import com.unam.mvmsys.entidad.stock.Lote; // Import corregido según tu archivo
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservas_stock_produccion")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReservaStockProduccion extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "orden_produccion_id", nullable = false)
    private OrdenProduccion ordenProduccion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(name = "cantidad_reservada", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadReservada;

    @Column(name = "fecha_reserva", nullable = false)
    @Builder.Default
    private LocalDateTime fechaReserva = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;
}