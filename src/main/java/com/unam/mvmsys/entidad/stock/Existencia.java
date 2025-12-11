package com.unam.mvmsys.entidad.stock;

import com.unam.mvmsys.entidad.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "existencias")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Existencia extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @ManyToOne(optional = false)
    @JoinColumn(name = "deposito_id", nullable = false)
    private Deposito deposito;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;
}