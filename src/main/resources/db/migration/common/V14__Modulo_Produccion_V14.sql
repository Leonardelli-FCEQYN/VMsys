-- V14: Ajustes de esquema para módulo de producción (recetas, etapas y reservas)

-- 1. Ordenes de Producción: columnas alineadas al modelo Java
ALTER TABLE ordenes_produccion ADD COLUMN IF NOT EXISTS codigo VARCHAR(50);
ALTER TABLE ordenes_produccion ADD CONSTRAINT IF NOT EXISTS uk_op_codigo UNIQUE (codigo);
ALTER TABLE ordenes_produccion ADD COLUMN IF NOT EXISTS proceso_estandar_id UUID;
ALTER TABLE ordenes_produccion ADD CONSTRAINT IF NOT EXISTS fk_op_proceso_estandar FOREIGN KEY (proceso_estandar_id) REFERENCES procesos_estandar(id);
ALTER TABLE ordenes_produccion ADD COLUMN IF NOT EXISTS cantidad_planificada DECIMAL(12,3) DEFAULT 0;
ALTER TABLE ordenes_produccion ADD COLUMN IF NOT EXISTS cantidad_real_obtenida DECIMAL(12,3) DEFAULT 0;
ALTER TABLE ordenes_produccion ADD COLUMN IF NOT EXISTS fecha_emision TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE ordenes_produccion ADD COLUMN IF NOT EXISTS fecha_inicio_estimada TIMESTAMP;
ALTER TABLE ordenes_produccion ADD COLUMN IF NOT EXISTS fecha_fin_estimada TIMESTAMP;
ALTER TABLE ordenes_produccion ADD COLUMN IF NOT EXISTS fecha_inicio_real TIMESTAMP;
ALTER TABLE ordenes_produccion ADD COLUMN IF NOT EXISTS fecha_fin_real TIMESTAMP;
ALTER TABLE ordenes_produccion ADD COLUMN IF NOT EXISTS estado VARCHAR(20) DEFAULT 'PLANIFICADA';
ALTER TABLE ordenes_produccion ADD COLUMN IF NOT EXISTS prioridad VARCHAR(10) DEFAULT 'MEDIA';
ALTER TABLE ordenes_produccion ADD COLUMN IF NOT EXISTS observaciones TEXT;

-- Copiar valores existentes si vienen de esquemas previos
UPDATE ordenes_produccion SET codigo = COALESCE(codigo, codigo_orden, numero_orden) WHERE codigo IS NULL;

-- 2. Etapas del proceso estándar
CREATE TABLE IF NOT EXISTS etapas_proceso (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    proceso_estandar_id UUID NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    orden_secuencia INT NOT NULL,
    tiempo_estimado_minutos INT DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT uq_ep_proceso_orden UNIQUE (proceso_estandar_id, orden_secuencia),
    CONSTRAINT fk_ep_proceso FOREIGN KEY (proceso_estandar_id) REFERENCES procesos_estandar(id)
);

-- 3. Seguimiento de etapas de la orden
CREATE TABLE IF NOT EXISTS ordenes_produccion_etapas (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    orden_produccion_id UUID NOT NULL,
    etapa_proceso_id UUID NOT NULL,
    estado VARCHAR(20),
    operario_id UUID,
    fecha_inicio TIMESTAMP,
    fecha_fin TIMESTAMP,
    observaciones TEXT,
    CONSTRAINT fk_ope_orden FOREIGN KEY (orden_produccion_id) REFERENCES ordenes_produccion(id),
    CONSTRAINT fk_ope_etapa FOREIGN KEY (etapa_proceso_id) REFERENCES etapas_proceso(id),
    CONSTRAINT fk_ope_operario FOREIGN KEY (operario_id) REFERENCES personas(id)
);

-- 4. Reservas de stock para producción
CREATE TABLE IF NOT EXISTS reservas_stock_produccion (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    orden_produccion_id UUID NOT NULL,
    lote_id UUID NOT NULL,
    cantidad_reservada DECIMAL(12,3) NOT NULL,
    fecha_reserva TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_rsp_orden FOREIGN KEY (orden_produccion_id) REFERENCES ordenes_produccion(id),
    CONSTRAINT fk_rsp_lote FOREIGN KEY (lote_id) REFERENCES lotes(id)
);
