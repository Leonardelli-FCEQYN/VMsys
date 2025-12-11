-- V9__Vinculation_Observaciones.sql
-- Agregar columna observaciones a tabla cliente_producto para vinculation

ALTER TABLE cliente_producto ADD COLUMN observaciones TEXT;

-- Crear tabla proveedor_productos si no existe (para vinculation de proveedores)
CREATE TABLE IF NOT EXISTS proveedor_productos (
    id UUID PRIMARY KEY,
    proveedor_id UUID NOT NULL,
    producto_id UUID NOT NULL,
    precio_compra DECIMAL(15, 2) NOT NULL,
    observaciones TEXT,
    fecha_vinculacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT true,
    
    CONSTRAINT fk_prov_prod_proveedor FOREIGN KEY (proveedor_id) 
        REFERENCES personas(id) ON DELETE CASCADE,
    CONSTRAINT fk_prov_prod_producto FOREIGN KEY (producto_id) 
        REFERENCES productos(id) ON DELETE CASCADE,
    CONSTRAINT uk_prov_prod_unique UNIQUE (proveedor_id, producto_id)
);

-- Crear índices para mejor rendimiento
CREATE INDEX idx_prov_prod_proveedor ON proveedor_productos(proveedor_id);
CREATE INDEX idx_prov_prod_producto ON proveedor_productos(producto_id);
CREATE INDEX idx_prov_prod_activo ON proveedor_productos(activo);
CREATE INDEX idx_cli_prod_activo ON cliente_producto(activo);
