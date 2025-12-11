package com.unam.mvmsys.entidad.auditoria;

import com.unam.mvmsys.entidad.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auditoria_log")
@Getter @Setter 
@NoArgsConstructor @AllArgsConstructor @Builder
public class AuditoriaLog extends BaseEntity {

    @Column(name = "entidad_nombre", nullable = false, length = 50)
    private String entidadNombre; // Ej: "PEDIDO", "PRODUCTO"

    @Column(name = "entidad_id", nullable = false)
    private UUID entidadId;

    @Column(nullable = false, length = 20)
    private String accion; // 'INSERT', 'UPDATE', 'DELETE'

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "cambios_json", columnDefinition = "TEXT")
    private String cambiosJson;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;
}
