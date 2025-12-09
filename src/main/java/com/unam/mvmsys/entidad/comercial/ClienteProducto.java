package com.unam.mvmsys.entidad.comercial;

import com.unam.mvmsys.entidad.base.BaseEntity;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.entidad.stock.Producto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Historial y relación de productos comprados por un cliente
 * Permite llevar estadísticas y aplicar precios especiales
 */
@Entity
@Table(name = "cliente_producto")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClienteProducto extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Persona cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "precio_especial", precision = 15, scale = 2)
    private BigDecimal precioEspecial;

    @Column(name = "cantidad_total_comprada", precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal cantidadTotalComprada = BigDecimal.ZERO;

    @Column(name = "cantidad_compras")
    @Builder.Default
    private int cantidadCompras = 0;

    @Column(name = "fecha_primera_compra")
    private LocalDateTime fechaPrimeraCompra;

    @Column(name = "fecha_ultima_compra")
    private LocalDateTime fechaUltimaCompra;

    @Column(name = "es_favorito")
    @Builder.Default
    private boolean esFavorito = false;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;
}
