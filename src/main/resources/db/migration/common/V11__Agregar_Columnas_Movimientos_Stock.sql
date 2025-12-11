-- ================================================================
-- MVMsys V11 - Agregar columnas ACTIVO y OBSERVACIONES a movimientos_stock
-- ================================================================

ALTER TABLE movimientos_stock ADD COLUMN activo BOOLEAN DEFAULT TRUE NOT NULL;
ALTER TABLE movimientos_stock ADD COLUMN observaciones TEXT;
