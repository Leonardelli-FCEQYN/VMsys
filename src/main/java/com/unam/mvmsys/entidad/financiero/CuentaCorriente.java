package com.unam.mvmsys.entidad.financiero;

import com.unam.mvmsys.entidad.base.BaseEntity;
import com.unam.mvmsys.entidad.seguridad.Persona;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cuentas_corrientes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CuentaCorriente extends BaseEntity {

    // Relación 1 a 1: Una persona tiene una única cuenta corriente
    @OneToOne(optional = false)
    @JoinColumn(name = "persona_id", unique = true, nullable = false)
    private Persona persona;

    @Column(name = "saldo_actual", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal saldoActual = BigDecimal.ZERO;

    @Column(name = "fecha_ultimo_movimiento")
    private LocalDateTime fechaUltimoMovimiento;
}