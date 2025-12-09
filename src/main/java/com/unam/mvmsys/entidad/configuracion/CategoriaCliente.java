package com.unam.mvmsys.entidad.configuracion;

import com.unam.mvmsys.entidad.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Categorías de cliente configurables dinámicamente (IRQ-06)
 * Permite crear, modificar y desactivar categorías sin hardcoding
 */
@Entity
@Table(name = "categorias_cliente")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoriaCliente extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "color_hex", length = 7)
    @Builder.Default
    private String colorHex = "#6366F1";

    @Column(length = 50)
    private String icono; // Emoji o símbolo

    @Column(nullable = false)
    @Builder.Default
    private boolean activa = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public String getDisplay() {
        return (icono != null && !icono.isEmpty() ? icono + " " : "") + nombre;
    }

    @Override
    public String toString() {
        return getDisplay();
    }
}
