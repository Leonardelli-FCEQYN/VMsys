package com.unam.mvmsys.entidad.stock;

import com.unam.mvmsys.entidad.base.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rubros")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rubro extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;
}