package com.unam.mvmsys.entidad.financiero;

import com.unam.mvmsys.entidad.base.BaseEntity;
import com.unam.mvmsys.entidad.comercial.Pedido;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "movimientos_financieros")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MovimientoFinanciero extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "cuenta_corriente_id", nullable = false)
    private CuentaCorriente cuentaCorriente;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false, length = 20)
    private String tipo; // 'DEBE', 'HABER'

    @Column(nullable = false, length = 100)
    private String concepto;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @ManyToOne
    @JoinColumn(name = "referencia_pedido_id")
    private Pedido referenciaPedido;

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}