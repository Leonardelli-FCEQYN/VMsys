package com.unam.mvmsys.entidad.produccion;

import java.time.LocalDateTime;

import com.unam.mvmsys.entidad.base.BaseEntity;
import com.unam.mvmsys.entidad.configuracion.Estado;
import com.unam.mvmsys.entidad.seguridad.Persona;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ordenes_produccion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrdenProduccion extends BaseEntity {

    @Column(name = "codigo_orden", nullable = false, unique = true, length = 50)
    private String codigoOrden;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin_estimada")
    private LocalDateTime fechaFinEstimada;

    @Column(name = "fecha_fin_real")
    private LocalDateTime fechaFinReal;

    @ManyToOne(optional = false)
    @JoinColumn(name = "estado_id", nullable = false)
    private Estado estado;

    @Column(length = 20)
    @Builder.Default
    private String prioridad = "NORMAL";

    @ManyToOne
    @JoinColumn(name = "responsable_id")
    private Persona responsable;

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}