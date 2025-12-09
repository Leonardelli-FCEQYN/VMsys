package com.unam.mvmsys.entidad.comercial;

import com.unam.mvmsys.entidad.base.BaseEntity; // Ajusta si tu paquete base es distinto
import com.unam.mvmsys.entidad.configuracion.Estado;
import com.unam.mvmsys.entidad.seguridad.Persona;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "pedidos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pedido extends BaseEntity {

    @Column(nullable = false, length = 20)
    private String tipo; // 'COMPRA', 'VENTA'

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Builder.Default
    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();

    @Column(name = "fecha_entrega_promesa")
    private LocalDateTime fechaEntregaPromesa;

    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @ManyToOne(optional = false)
    @JoinColumn(name = "estado_id", nullable = false)
    private Estado estado;

    @Column(name = "total_estimado", precision = 15, scale = 2)
    private BigDecimal totalEstimado;

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}