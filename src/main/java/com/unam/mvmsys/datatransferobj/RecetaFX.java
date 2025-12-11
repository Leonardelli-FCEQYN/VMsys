package com.unam.mvmsys.datatransferobj; // <--- PAQUETE ACTUALIZADO

import com.unam.mvmsys.entidad.produccion.ProcesoEstandar;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.UUID;

public class RecetaFX {
    private UUID id;
    private ProcesoEstandar entidadOriginal;

    private final StringProperty nombre = new SimpleStringProperty("");
    private final StringProperty descripcion = new SimpleStringProperty("");
    private final BooleanProperty activo = new SimpleBooleanProperty(true);
    
    private final ObservableList<InsumoFX> insumos = FXCollections.observableArrayList();
    private final ObservableList<EtapaFX> etapas = FXCollections.observableArrayList();

    public RecetaFX() {
        this.entidadOriginal = new ProcesoEstandar();
    }

    public RecetaFX(ProcesoEstandar entidad) {
        this.id = entidad.getId();
        this.entidadOriginal = entidad;
        this.nombre.set(entidad.getNombre());
        this.descripcion.set(entidad.getDescripcion());
        this.activo.set(entidad.isActivo());
        // No cargamos colecciones aquí - se cargan con cargarColecciones()
    }

    public void cargarColecciones(ProcesoEstandar entidad) {
        if (entidad.getInsumos() != null) {
            entidad.getInsumos().forEach(i -> this.insumos.add(new InsumoFX(i)));
        }
        if (entidad.getEtapas() != null) {
            entidad.getEtapas().forEach(e -> this.etapas.add(new EtapaFX(e)));
        }
    }

    public StringProperty nombreProperty() { return nombre; }
    public StringProperty descripcionProperty() { return descripcion; }
    public BooleanProperty activoProperty() { return activo; }
    public ObservableList<InsumoFX> getInsumos() { return insumos; }
    public ObservableList<EtapaFX> getEtapas() { return etapas; }

    public String getNombre() { return nombre.get(); }
    public void setNombre(String n) { this.nombre.set(n); }
    public String getDescripcion() { return descripcion.get(); }
    public void setDescripcion(String d) { this.descripcion.set(d); }
    public boolean isActivo() { return activo.get(); }
    public void setActivo(boolean a) { this.activo.set(a); }
    public UUID getId() { return id; }

    public ProcesoEstandar toEntity() {
        entidadOriginal.setNombre(getNombre());
        entidadOriginal.setDescripcion(getDescripcion());
        entidadOriginal.setActivo(isActivo());
        
        entidadOriginal.getInsumos().clear();
        insumos.forEach(fx -> entidadOriginal.agregarInsumo(fx.toEntity()));

        entidadOriginal.getEtapas().clear();
        etapas.forEach(fx -> entidadOriginal.agregarEtapa(fx.toEntity()));

        return entidadOriginal;
    }
}