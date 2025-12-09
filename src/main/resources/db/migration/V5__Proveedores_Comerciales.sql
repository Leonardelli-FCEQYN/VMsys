-- ================================================================
-- MVMsys V5 - Datos comerciales de Proveedores
-- Agrega campos en personas para rubro y condiciones comerciales
-- No rompe datos existentes (valores por defecto)
-- ================================================================

-- Rubro / categoría del proveedor
ALTER TABLE personas ADD COLUMN rubro_proveedor VARCHAR(100);

-- Condiciones comerciales básicas
ALTER TABLE personas ADD COLUMN plazo_pago_dias INT DEFAULT 0 NOT NULL;
ALTER TABLE personas ADD COLUMN bonificacion_porcentaje DECIMAL(5,2) DEFAULT 0 NOT NULL;
ALTER TABLE personas ADD COLUMN interes_mora_porcentaje DECIMAL(5,2) DEFAULT 0 NOT NULL;
