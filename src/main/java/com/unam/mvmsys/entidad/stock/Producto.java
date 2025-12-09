package com.unam.mvmsys.entidad.stock;

import com.unam.mvmsys.entidad.base.BaseEntity;
import com.unam.mvmsys.entidad.configuracion.TipoProducto;
import com.unam.mvmsys.entidad.configuracion.UnidadMedida;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Producto extends BaseEntity {

    @Column(name = "codigo_sku", nullable = false, unique = true, length = 50)
    private String codigoSku;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "unidad_medida_id", nullable = false)
    private UnidadMedida unidadMedida;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tipo_producto_id", nullable = false)
    private TipoProducto tipoProducto;

    @ManyToOne
    @JoinColumn(name = "rubro_id")
    private Rubro rubro;

    @Column(name = "stock_minimo", precision = 12, scale = 3)
    @Builder.Default
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    @Column(name = "costo_reposicion", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal costoReposicion = BigDecimal.ZERO;

    @Column(name = "precio_venta_base", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal precioVentaBase = BigDecimal.ZERO;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;
}