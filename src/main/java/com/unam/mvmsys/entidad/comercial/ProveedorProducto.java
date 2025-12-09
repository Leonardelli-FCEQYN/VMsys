package com.unam.mvmsys.entidad.comercial;

import com.unam.mvmsys.entidad.base.BaseEntity;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.entidad.stock.Producto;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Vinculación Proveedor-Producto (mapea a catalogos_proveedor)
 * Representa qué productos ofrece cada proveedor con sus condiciones
 */
@Entity
@Table(name = "catalogos_proveedor", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"proveedor_id", "producto_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProveedorProducto extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Persona proveedor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "precio_compra", precision = 15, scale = 2, nullable = false)
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

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_alta", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaAlta = LocalDateTime.now();

    @Column(name = "fecha_ultima_compra")
    private LocalDateTime fechaUltimaCompra;

    @Column(name = "ultima_actualizacion_precio")
    private LocalDateTime ultimaActualizacionPrecio;
}
