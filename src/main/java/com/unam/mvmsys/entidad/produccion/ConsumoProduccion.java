package com.unam.mvmsys.entidad.produccion;

import com.unam.mvmsys.entidad.base.BaseEntity;
import com.unam.mvmsys.entidad.stock.Producto;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "consumos_produccion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumoProduccion extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "orden_id", nullable = false)
    private OrdenProduccion orden;

    @ManyToOne(optional = false)
    @JoinColumn(name = "etapa_id", nullable = false)
    private EjecucionEtapa etapa;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_insumo_id", nullable = false)
    private Producto productoInsumo;

    @Column(name = "cantidad_consumida", nullable = false, precision = 15, scale = 4)
    private BigDecimal cantidadConsumida;

    @Column(name = "fecha_consumo")
    @Builder.Default
    private LocalDateTime fechaConsumo = LocalDateTime.now();
}
