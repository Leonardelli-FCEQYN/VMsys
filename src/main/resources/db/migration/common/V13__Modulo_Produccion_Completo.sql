-- ================================================================
-- V13: Módulo de Producción (3FN + Integridad)
-- ================================================================

-- 1. MEJORAS EN PRODUCTOS Y CUENTAS CORRIENTES
-- Stock comprometido para producción/ventas
ALTER TABLE productos ADD COLUMN IF NOT EXISTS stock_comprometido DECIMAL(15,2) DEFAULT 0.00;
-- Campos adicionales de productos
ALTER TABLE productos ADD COLUMN IF NOT EXISTS marca VARCHAR(100);
ALTER TABLE productos ADD COLUMN IF NOT EXISTS margen_ganancia_sugerido DECIMAL(5,2) DEFAULT 30.00;
-- Límite de crédito en cuentas corrientes
ALTER TABLE cuentas_corrientes ADD COLUMN IF NOT EXISTS limite_credito DECIMAL(15,2) DEFAULT 0.00;
ALTER TABLE cuentas_corrientes ADD CONSTRAINT IF NOT EXISTS chk_limite_positivo CHECK (limite_credito >= 0);

-- 2. PROCESOS ESTANDAR (Recetas/BOM)
CREATE TABLE procesos_estandar (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    tiempo_estimado_minutos INT DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE
);

-- 3. ACTUALIZAR ORDENES_PRODUCCION EXISTENTE (de V2)
-- Añadir columnas faltantes a la tabla creada en V2
ALTER TABLE ordenes_produccion ADD COLUMN IF NOT EXISTS numero_orden VARCHAR(20);
ALTER TABLE ordenes_produccion ADD COLUMN IF NOT EXISTS proceso_id UUID;
ALTER TABLE ordenes_produccion ADD COLUMN IF NOT EXISTS cantidad_planificada DECIMAL(15,2) DEFAULT 0;
ALTER TABLE ordenes_produccion ADD COLUMN IF NOT EXISTS cantidad_producida DECIMAL(15,2) DEFAULT 0;
ALTER TABLE ordenes_produccion ADD COLUMN IF NOT EXISTS fecha_emision TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE ordenes_produccion ADD COLUMN IF NOT EXISTS fecha_inicio_estimada DATE;

-- Hacer que numero_orden sea UNIQUE si no lo es
ALTER TABLE ordenes_produccion ADD CONSTRAINT uk_numero_orden UNIQUE (numero_orden);

-- FK hacia procesos_estandar
ALTER TABLE ordenes_produccion ADD CONSTRAINT fk_op_proceso FOREIGN KEY (proceso_id) REFERENCES procesos_estandar(id);

-- 3.1 INSUMOS DEL PROCESO ESTÁNDAR (BOM Detail)
-- Define qué productos se necesitan para cada proceso
CREATE TABLE procesos_estandar_insumos (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    proceso_estandar_id UUID NOT NULL,
    producto_id UUID NOT NULL,
    cantidad_base DECIMAL(12,3) NOT NULL,
    CONSTRAINT fk_pei_proceso FOREIGN KEY (proceso_estandar_id) REFERENCES procesos_estandar(id),
    CONSTRAINT fk_pei_insumo FOREIGN KEY (producto_id) REFERENCES productos(id)
);

-- 3. SEGUIMIENTO DE ETAPAS (Ejecución Real)
-- Se copian las etapas del proceso estándar para tener historial propio (Snapshot)
CREATE TABLE ejecucion_etapas (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    orden_id UUID NOT NULL,
    
    -- Datos del momento (Snapshot del proceso estándar)
    nombre_etapa VARCHAR(100) NOT NULL,
    orden_secuencia INT NOT NULL,
    tiempo_estimado_minutos INT DEFAULT 0,
    
    -- Ejecución real
    estado VARCHAR(20) DEFAULT 'PENDIENTE',
    operador_id UUID, -- Quién la realizó
    fecha_inicio TIMESTAMP,
    fecha_fin TIMESTAMP,
    
    cantidad_resultante DECIMAL(15,2) DEFAULT 0.00,
    merma_registrada DECIMAL(15,2) DEFAULT 0.00,
    observaciones TEXT,

    CONSTRAINT fk_ee_orden FOREIGN KEY (orden_id) REFERENCES ordenes_produccion(id),
    CONSTRAINT fk_ee_operador FOREIGN KEY (operador_id) REFERENCES personas(id),
    CONSTRAINT chk_ee_estado CHECK (estado IN ('PENDIENTE', 'EN_PROCESO', 'FINALIZADA', 'PAUSADA'))
);

-- 4. CONSUMOS REALES (Materia Prima gastada)
-- Tabla transaccional para descontar stock real
CREATE TABLE consumos_produccion (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    orden_id UUID NOT NULL,
    etapa_id UUID NOT NULL,
    producto_insumo_id UUID NOT NULL, -- Qué se gastó
    
    cantidad_consumida DECIMAL(15,4) NOT NULL,
    fecha_consumo TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_cp_orden FOREIGN KEY (orden_id) REFERENCES ordenes_produccion(id),
    CONSTRAINT fk_cp_etapa FOREIGN KEY (etapa_id) REFERENCES ejecucion_etapas(id),
    CONSTRAINT fk_cp_prod FOREIGN KEY (producto_insumo_id) REFERENCES productos(id),
    CONSTRAINT chk_cp_cantidad CHECK (cantidad_consumida > 0)
);

-- 5. VISTA DE EFICIENCIA (Reporte IRQ-15 / RF-15)
-- Calcula automáticamente la diferencia entre lo planeado y lo real
CREATE VIEW v_eficiencia_produccion AS
SELECT 
    op.numero_orden,
    p.nombre as proceso,
    op.cantidad_planificada,
    op.cantidad_producida,
    (op.cantidad_producida / NULLIF(op.cantidad_planificada, 0)) * 100 as porcentaje_cumplimiento,
    SUM(ee.merma_registrada) as total_mermas
FROM ordenes_produccion op
JOIN procesos_estandar p ON op.proceso_id = p.id
LEFT JOIN ejecucion_etapas ee ON op.id = ee.orden_id
GROUP BY op.id, op.numero_orden, p.nombre, op.cantidad_planificada, op.cantidad_producida;

-- 6. AUDITORÍA (Registro de cambios)
CREATE TABLE auditoria_log (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    entidad_nombre VARCHAR(50) NOT NULL,
    entidad_id UUID NOT NULL,
    accion VARCHAR(20) NOT NULL,
    usuario_id UUID,
    cambios_json TEXT,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
