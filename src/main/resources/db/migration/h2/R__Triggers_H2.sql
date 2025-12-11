-- H2 Triggers: Enlaces a clases Java para triggers complejos
-- Script repetible - solo definiciones de triggers (las tablas se crean en migraciones versionadas)

-- Trigger: Actualizar saldo de cuenta corriente
CREATE TRIGGER IF NOT EXISTS trg_actualizar_saldo_cc
AFTER INSERT ON movimientos_financieros
FOR EACH ROW
CALL "com.unam.mvmsys.config.H2Triggers$ActualizarSaldoCC";

-- Trigger: Impacto en stock
CREATE TRIGGER IF NOT EXISTS trg_impacto_stock
AFTER INSERT ON detalles_movimiento
FOR EACH ROW
CALL "com.unam.mvmsys.config.H2Triggers$ImpactoStock";

-- Trigger: Consumo de producción
CREATE TRIGGER IF NOT EXISTS trg_consumo_produccion
AFTER INSERT ON consumos_produccion
FOR EACH ROW
CALL "com.unam.mvmsys.config.H2Triggers$ConsumoProduccion";