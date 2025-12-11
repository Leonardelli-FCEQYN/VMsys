package com.unam.mvmsys.entidad.produccion;

import com.unam.mvmsys.entidad.base.BaseEntity;
import com.unam.mvmsys.entidad.seguridad.Persona; // <--- VALIDAR PAQUETE
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordenes_produccion_etapas")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrdenProduccionEtapa extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "orden_produccion_id", nullable = false)
    private OrdenProduccion ordenProduccion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "etapa_proceso_id", nullable = false)
    private EtapaProceso etapaProceso;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private EstadoEtapa estado = EstadoEtapa.PENDIENTE;

    @ManyToOne
    @JoinColumn(name = "operario_id")
    private Persona operario; 

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    public enum EstadoEtapa {
        PENDIENTE, EN_CURSO, COMPLETADO, OMITIDO
    }
}