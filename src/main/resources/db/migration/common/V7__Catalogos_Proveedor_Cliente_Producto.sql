-- ================================================================
-- MVMsys V7 - Catálogos Proveedor-Producto y Cliente-Producto
-- Permite vincular qué productos ofrece cada proveedor
-- y llevar historial de productos que compra cada cliente
-- Agrega categoría de cliente (IRQ-06)
-- ================================================================

-- 0. CATEGORÍA DE CLIENTE (según requisitos IRQ-06)
-- Agregar columna categoría_cliente a tabla personas
ALTER TABLE personas ADD COLUMN categoria_cliente VARCHAR(20);
-- Valores permitidos: 'MAYORISTA', 'MINORISTA', 'EVENTUAL', 'FRECUENTE'

-- 1. CATÁLOGO DE PROVEEDOR (Productos que ofrece/vende cada proveedor)
CREATE TABLE catalogos_proveedor (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    proveedor_id UUID NOT NULL,
    producto_id UUID NOT NULL,
    
    -- Precio y condiciones específicas del proveedor para este producto
    precio_compra DECIMAL(15, 2) NOT NULL,
    
    -- Disponibilidad y lead time
    disponible BOOLEAN DEFAULT TRUE NOT NULL,
    tiempo_entrega_dias INT DEFAULT 0,
    
    -- Producto preferido/favorito de este proveedor
    es_favorito BOOLEAN DEFAULT FALSE,
    
    -- Cantidad mínima de compra
    cantidad_minima DECIMAL(12, 3) DEFAULT 1,
    
    -- Auditoría
    fecha_alta TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_ultima_compra TIMESTAMP,
    ultima_actualizacion_precio TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_catprov_proveedor FOREIGN KEY (proveedor_id) REFERENCES personas(id),
    CONSTRAINT fk_catprov_producto FOREIGN KEY (producto_id) REFERENCES productos(id),
    CONSTRAINT uk_catprov_proveedor_producto UNIQUE (proveedor_id, producto_id),
    CONSTRAINT chk_catprov_precio_positivo CHECK (precio_compra >= 0),
    CONSTRAINT chk_catprov_cantidad_positiva CHECK (cantidad_minima > 0)
);

-- 2. HISTORIAL CLIENTE-PRODUCTO (Productos que suele comprar cada cliente)
CREATE TABLE cliente_producto (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    cliente_id UUID NOT NULL,
    producto_id UUID NOT NULL,
    
    -- Precio especial para este cliente (puede diferir del precio base)
    precio_especial DECIMAL(15, 2),
    
    -- Estadísticas de compras
    cantidad_total_comprada DECIMAL(15, 3) DEFAULT 0,
    cantidad_compras INT DEFAULT 0,
    fecha_primera_compra TIMESTAMP,
    fecha_ultima_compra TIMESTAMP,
    
    -- Producto favorito del cliente
    es_favorito BOOLEAN DEFAULT FALSE,
    
    -- Estado
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    
    -- Constraints
    CONSTRAINT fk_cliprod_cliente FOREIGN KEY (cliente_id) REFERENCES personas(id),
    CONSTRAINT fk_cliprod_producto FOREIGN KEY (producto_id) REFERENCES productos(id),
    CONSTRAINT uk_cliprod_cliente_producto UNIQUE (cliente_id, producto_id),
    CONSTRAINT chk_cliprod_precio_positivo CHECK (precio_especial IS NULL OR precio_especial >= 0),
    CONSTRAINT chk_cliprod_cantidad_positiva CHECK (cantidad_total_comprada >= 0)
);

-- 3. ÍNDICES para optimizar búsquedas frecuentes
CREATE INDEX idx_catprov_proveedor ON catalogos_proveedor(proveedor_id);
CREATE INDEX idx_catprov_producto ON catalogos_proveedor(producto_id);
CREATE INDEX idx_catprov_disponible ON catalogos_proveedor(disponible);

CREATE INDEX idx_cliprod_cliente ON cliente_producto(cliente_id);
CREATE INDEX idx_cliprod_producto ON cliente_producto(producto_id);
CREATE INDEX idx_cliprod_favoritos ON cliente_producto(es_favorito);

-- ================================================================
-- COMENTARIOS Y CASOS DE USO
-- ================================================================

-- CATÁLOGO PROVEEDOR:
-- - Al crear orden de compra, mostrar solo productos disponibles del proveedor
-- - Comparar precios entre proveedores para el mismo producto
-- - Alertas: "Proveedor X tiene mejor precio para producto Y"
-- - Historial: "Última vez que compramos producto Y fue hace Z días"

-- CLIENTE-PRODUCTO:
-- - Sugerencias al crear pedido: "Este cliente suele comprar estos productos"
-- - Aplicar precios especiales automáticamente
-- - Análisis: "Productos más vendidos a cliente X"
-- - Marketing: "Clientes que compraron X hace +60 días"
