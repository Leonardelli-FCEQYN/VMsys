package com.unam.mvmsys.entidad.stock;

import com.unam.mvmsys.entidad.base.BaseEntity;
import com.unam.mvmsys.entidad.seguridad.Usuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movimientos_stock")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MovimientoStock extends BaseEntity {

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private String tipoMovimiento; // 'ENTRADA', 'SALIDA', 'TRANSFERENCIA'

    @Column(name = "concepto", nullable = false, length = 100)
    private String concepto; // 'COMPRA', 'VENTA', 'PRODUCCION', 'MERMA', 'AJUSTE'

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
    private String referenciaComprobante; // Número de Remito/Orden/Factura

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "movimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleMovimiento> detalles = new ArrayList<>();

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}

