package com.unam.mvmsys.entidad.stock;

import com.unam.mvmsys.entidad.seguridad.Usuario;
import com.unam.mvmsys.entidad.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.unam.mvmsys.entidad.produccion.OrdenProduccion;

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

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private String tipoMovimiento;

    @Column(name = "concepto", nullable = false, length = 100)
    private String concepto;

    @ManyToOne
    @JoinColumn(name = "deposito_origen_id")
    private Deposito depositoOrigen;

    @ManyToOne
    @JoinColumn(name = "deposito_destino_id")
    private Deposito depositoDestino;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "referencia_comprobante", length = 100)
    private String referenciaComprobante;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;

    // --- NUEVO CAMPO V14 (Vinculación con Producción) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_produccion_id")
    private OrdenProduccion ordenProduccion;
}
