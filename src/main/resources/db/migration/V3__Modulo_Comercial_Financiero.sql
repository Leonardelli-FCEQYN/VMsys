-- ================================================================
-- MVMsys V3 - Módulo Comercial y Financiero
-- ================================================================

-- 1. PEDIDOS (Intención de Compra / Venta)
CREATE TABLE pedidos (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL, -- 'COMPRA', 'VENTA'
    codigo VARCHAR(50) NOT NULL UNIQUE, -- Ej: 'PED-0001'
    fecha_emision TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_entrega_promesa TIMESTAMP,
    
    persona_id UUID NOT NULL, -- Cliente o Proveedor
    estado_id UUID NOT NULL, -- 'Pendiente', 'Aprobado'
    
    total_estimado DECIMAL(15, 2) DEFAULT 0,
    observaciones TEXT, 
    
    CONSTRAINT fk_pedidos_persona FOREIGN KEY (persona_id) REFERENCES personas(id),
    CONSTRAINT fk_pedidos_estado FOREIGN KEY (estado_id) REFERENCES estados(id)
);

CREATE TABLE detalles_pedido (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    pedido_id UUID NOT NULL,
    producto_id UUID NOT NULL,
    cantidad DECIMAL(15, 3) NOT NULL,
    precio_unitario DECIMAL(15, 2) NOT NULL,
    
    CONSTRAINT fk_detped_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
    CONSTRAINT fk_detped_producto FOREIGN KEY (producto_id) REFERENCES productos(id),
    CONSTRAINT chk_detped_cant_pos CHECK (cantidad > 0),
    CONSTRAINT chk_detped_precio_pos CHECK (precio_unitario >= 0)
);

-- 2. MOVIMIENTOS FINANCIEROS (Cuenta Corriente)
CREATE TABLE movimientos_financieros (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    cuenta_corriente_id UUID NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    tipo VARCHAR(20) NOT NULL, -- 'DEBE', 'HABER'
    concepto VARCHAR(100) NOT NULL, -- 'PAGO', 'VENTA'
    monto DECIMAL(15, 2) NOT NULL,
    
    referencia_pedido_id UUID,
    observaciones TEXT,
    
    CONSTRAINT fk_movfin_cc FOREIGN KEY (cuenta_corriente_id) REFERENCES cuentas_corrientes(id),
    CONSTRAINT fk_movfin_pedido FOREIGN KEY (referencia_pedido_id) REFERENCES pedidos(id),
    CONSTRAINT chk_movfin_monto_pos CHECK (monto >= 0)
);