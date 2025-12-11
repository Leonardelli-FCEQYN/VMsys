package com.unam.mvmsys.datatransferobj; // <--- PAQUETE ACTUALIZADO

import com.unam.mvmsys.entidad.produccion.ProcesoEstandarInsumo;
import com.unam.mvmsys.entidad.stock.Producto;
import javafx.beans.property.*;

import java.math.BigDecimal;

public class InsumoFX {
    private ProcesoEstandarInsumo entidadOriginal;
    
    private final ObjectProperty<Producto> producto = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> cantidadBase = new SimpleObjectProperty<>(BigDecimal.ZERO);

    public InsumoFX() {
        this.entidadOriginal = new ProcesoEstandarInsumo();
    }

    public InsumoFX(ProcesoEstandarInsumo entidad) {
        this.entidadOriginal = entidad;
        this.producto.set(entidad.getProducto());
        this.cantidadBase.set(entidad.getCantidadBase());
    }

    public ObjectProperty<Producto> productoProperty() { return producto; }
    public ObjectProperty<BigDecimal> cantidadBaseProperty() { return cantidadBase; }

    public Producto getProducto() { return producto.get(); }
    public void setProducto(Producto p) { this.producto.set(p); }
    public BigDecimal getCantidadBase() { return cantidadBase.get(); }
    public void setCantidadBase(BigDecimal c) { this.cantidadBase.set(c); }

    public ProcesoEstandarInsumo toEntity() {
        entidadOriginal.setProducto(getProducto());
        entidadOriginal.setCantidadBase(getCantidadBase());
        return entidadOriginal;
    }
}