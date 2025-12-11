package com.unam.mvmsys.entidad.produccion;

import com.unam.mvmsys.entidad.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "procesos_estandar")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProcesoEstandar extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tiempo_estimado_minutos")
    @Builder.Default
    private Integer tiempoEstimadoMinutos = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    // Insumos (Ingredientes)
    @OneToMany(mappedBy = "procesoEstandar", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    @Builder.Default
    private List<ProcesoEstandarInsumo> insumos = new ArrayList<>();

    // Etapas (Pasos)
    @OneToMany(mappedBy = "procesoEstandar", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordenSecuencia ASC")
    @Fetch(FetchMode.SUBSELECT)
    @Builder.Default
    private List<EtapaProceso> etapas = new ArrayList<>();

    // Helpers
    public void agregarInsumo(ProcesoEstandarInsumo insumo) {
        insumos.add(insumo);
        insumo.setProcesoEstandar(this);
    }
    
    public void agregarEtapa(EtapaProceso etapa) {
        etapas.add(etapa);
        etapa.setProcesoEstandar(this);
    }
}