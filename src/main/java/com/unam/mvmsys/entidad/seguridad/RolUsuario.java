package com.unam.mvmsys.entidad.seguridad;

import com.unam.mvmsys.entidad.base.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles_usuario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RolUsuario extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;
}