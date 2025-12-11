package com.unam.mvmsys.entidad.produccion;

import com.unam.mvmsys.entidad.base.BaseEntity;
import com.unam.mvmsys.entidad.stock.Producto;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "procesos_estandar_insumos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProcesoEstandarInsumo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_estandar_id", nullable = false)
    private ProcesoEstandar procesoEstandar;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "cantidad_base", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadBase;
}