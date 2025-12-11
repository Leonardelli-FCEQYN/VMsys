package com.unam.mvmsys.datatransferobj; // <--- PAQUETE ACTUALIZADO

import com.unam.mvmsys.entidad.produccion.EtapaProceso;
import javafx.beans.property.*;

public class EtapaFX {
    private EtapaProceso entidadOriginal;

    private final StringProperty nombre = new SimpleStringProperty("");
    private final IntegerProperty orden = new SimpleIntegerProperty(0);
    private final IntegerProperty tiempoMinutos = new SimpleIntegerProperty(0);

    public EtapaFX() {
        this.entidadOriginal = new EtapaProceso();
    }

    public EtapaFX(EtapaProceso entidad) {
        this.entidadOriginal = entidad;
        this.nombre.set(entidad.getNombre());
        this.orden.set(entidad.getOrdenSecuencia());
        this.tiempoMinutos.set(entidad.getTiempoEstimadoMinutos());
    }

    public StringProperty nombreProperty() { return nombre; }
    public IntegerProperty ordenProperty() { return orden; }
    public IntegerProperty tiempoMinutosProperty() { return tiempoMinutos; }

    public String getNombre() { return nombre.get(); }
    public void setNombre(String n) { this.nombre.set(n); }
    public int getOrden() { return orden.get(); }
    public void setOrden(int o) { this.orden.set(o); }
    public int getTiempoMinutos() { return tiempoMinutos.get(); }
    public void setTiempoMinutos(int t) { this.tiempoMinutos.set(t); }

    public EtapaProceso toEntity() {
        entidadOriginal.setNombre(getNombre());
        entidadOriginal.setOrdenSecuencia(getOrden());
        entidadOriginal.setTiempoEstimadoMinutos(getTiempoMinutos());
        entidadOriginal.setActivo(true);
        return entidadOriginal;
    }
}