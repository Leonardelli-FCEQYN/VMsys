package com.unam.mvmsys.entidad.comercial;

import com.unam.mvmsys.entidad.base.BaseEntity;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.entidad.stock.Producto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Catálogo de productos que ofrece un proveedor con su precio de compra
 */
@Entity
@Table(name = "catalogos_proveedor")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CatalogoProveedor extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Persona proveedor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "precio_compra", nullable = false, precision = 15, scale = 2)
    private BigDecimal precioCompra;

    @Column(nullable = false)
    @Builder.Default
    private boolean disponible = true;

    @Column(name = "tiempo_entrega_dias")
    @Builder.Default
    private int tiempoEntregaDias = 0;

    @Column(name = "es_favorito")
    @Builder.Default
    private boolean esFavorito = false;

    @Column(name = "cantidad_minima", precision = 12, scale = 3)
    @Builder.Default
    private BigDecimal cantidadMinima = BigDecimal.ONE;

    @Column(name = "fecha_alta", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaAlta = LocalDateTime.now();

    @Column(name = "fecha_ultima_compra")
    private LocalDateTime fechaUltimaCompra;

    @Column(name = "ultima_actualizacion_precio")
    private LocalDateTime ultimaActualizacionPrecio;
}
