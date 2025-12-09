-- ================================================================
-- MVMsys V2 - Núcleo de Stock, Trazabilidad y Producción
-- ================================================================

-- 1. DEPÓSITOS (Físicos y Lógicos)
-- ----------------------------------------------------------------
CREATE TABLE depositos (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE, -- Ej: 'Planta Central', 'Secadero Externo'
    direccion VARCHAR(200),
    es_propio BOOLEAN DEFAULT TRUE NOT NULL, -- FALSE = Ubicación en proveedor externo (Servicios Tercerizados)
    activo BOOLEAN DEFAULT TRUE NOT NULL
);

-- 2. LOTES (La unidad de trazabilidad)
-- ----------------------------------------------------------------
CREATE TABLE lotes (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE, -- Ej: 'L-202512-001' (Generado por sistema)
    producto_id UUID NOT NULL,
    estado_id UUID NOT NULL, -- 'En Cuarentena', 'Disponible', 'Agotado'
    
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_vencimiento TIMESTAMP, -- Para insumos químicos
    costo_unitario_promedio DECIMAL(15, 2) DEFAULT 0, -- Valuación
    observaciones TEXT,
    
    CONSTRAINT fk_lotes_producto FOREIGN KEY (producto_id) REFERENCES productos(id),
    CONSTRAINT fk_lotes_estado FOREIGN KEY (estado_id) REFERENCES estados(id)
);

-- 3. EXISTENCIAS (Stock Físico por Depósito)
-- ----------------------------------------------------------------
CREATE TABLE existencias (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    lote_id UUID NOT NULL,
    deposito_id UUID NOT NULL,
    cantidad DECIMAL(15, 3) NOT NULL, -- Soporta m2, m3, unidades
    
    CONSTRAINT uk_existencia_lote_deposito UNIQUE (lote_id, deposito_id),
    CONSTRAINT fk_existencias_lote FOREIGN KEY (lote_id) REFERENCES lotes(id),
    CONSTRAINT fk_existencias_deposito FOREIGN KEY (deposito_id) REFERENCES depositos(id),
    
    -- INTEGRIDAD DURA: La BD impide tener stock negativo
    CONSTRAINT chk_stock_no_negativo CHECK (cantidad >= 0)
);

-- 4. MOVIMIENTOS DE STOCK (Kardex / Auditoría)
-- ----------------------------------------------------------------
CREATE TABLE movimientos_stock (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tipo_movimiento VARCHAR(20) NOT NULL, -- 'ENTRADA', 'SALIDA', 'TRANSFERENCIA'
    concepto VARCHAR(100) NOT NULL, -- 'COMPRA', 'VENTA', 'PRODUCCION', 'MERMA'
    
    deposito_origen_id UUID,
    deposito_destino_id UUID,
    usuario_id UUID, -- Auditoría de quién movió
    referencia_comprobante VARCHAR(100), -- Nro Remito/Orden
    
    CONSTRAINT fk_mov_origen FOREIGN KEY (deposito_origen_id) REFERENCES depositos(id),
    CONSTRAINT fk_mov_destino FOREIGN KEY (deposito_destino_id) REFERENCES depositos(id)
);

CREATE TABLE detalles_movimiento (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    movimiento_id UUID NOT NULL,
    lote_id UUID NOT NULL,
    cantidad DECIMAL(15, 3) NOT NULL,
    
    CONSTRAINT fk_detmov_movimiento FOREIGN KEY (movimiento_id) REFERENCES movimientos_stock(id),
    CONSTRAINT fk_detmov_lote FOREIGN KEY (lote_id) REFERENCES lotes(id),
    CONSTRAINT chk_cantidad_positiva CHECK (cantidad > 0)
);

-- 5. PRODUCCIÓN (Cabecera de Transformación)
-- ----------------------------------------------------------------
CREATE TABLE ordenes_produccion (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    codigo_orden VARCHAR(50) NOT NULL UNIQUE, -- Ej: 'OP-001'
    fecha_inicio TIMESTAMP,
    fecha_fin_estimada TIMESTAMP,
    fecha_fin_real TIMESTAMP,
    
    estado_id UUID NOT NULL, -- 'Planificada', 'En Proceso', 'Finalizada'
    prioridad VARCHAR(20) DEFAULT 'NORMAL',
    responsable_id UUID, -- Capataz/Encargado
    observaciones TEXT,
    
    CONSTRAINT fk_op_estado FOREIGN KEY (estado_id) REFERENCES estados(id),
    CONSTRAINT fk_op_responsable FOREIGN KEY (responsable_id) REFERENCES personas(id)
);

-- Índices Clave
CREATE INDEX idx_lotes_producto ON lotes(producto_id);
CREATE INDEX idx_existencias_lote ON existencias(lote_id);
CREATE INDEX idx_movimientos_fecha ON movimientos_stock(fecha);