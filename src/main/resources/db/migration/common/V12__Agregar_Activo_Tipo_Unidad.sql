-- Agrega columna activo a tablas maestras usadas en productos
ALTER TABLE unidades_medida ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT TRUE NOT NULL;
ALTER TABLE tipos_producto ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT TRUE NOT NULL;
