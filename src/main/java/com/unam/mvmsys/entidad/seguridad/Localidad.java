package com.unam.mvmsys.entidad.seguridad;

import com.unam.mvmsys.entidad.base.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "localidades")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Localidad extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Column(name = "provincia_nombre", length = 100)
    private String provinciaNombre;
}