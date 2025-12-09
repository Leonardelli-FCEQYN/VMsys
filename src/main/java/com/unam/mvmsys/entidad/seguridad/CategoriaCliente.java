package com.unam.mvmsys.entidad.seguridad;

/**
 * Categorías de cliente según IRQ-06
 */
public enum CategoriaCliente {
    MAYORISTA("Mayorista"),
    MINORISTA("Minorista"),
    EVENTUAL("Eventual"),
    FRECUENTE("Frecuente");

    private final String descripcion;

    CategoriaCliente(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }

    public static CategoriaCliente fromString(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        for (CategoriaCliente c : CategoriaCliente.values()) {
            if (c.name().equalsIgnoreCase(text) || c.descripcion.equalsIgnoreCase(text)) {
                return c;
            }
        }
        return null;
    }
}
