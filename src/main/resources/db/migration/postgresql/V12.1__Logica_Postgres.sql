-- V12 Postgres: Campos nuevos y Lógica PL/pgSQL Nativa

-- 1. Campos
ALTER TABLE productos ADD COLUMN IF NOT EXISTS marca VARCHAR(100);
ALTER TABLE productos ADD COLUMN IF NOT EXISTS margen_ganancia_sugerido DECIMAL(5,2) DEFAULT 30.00;
ALTER TABLE cuentas_corrientes ADD COLUMN IF NOT EXISTS limite_credito DECIMAL(15, 2) DEFAULT 0.00;
ALTER TABLE cuentas_corrientes ADD CONSTRAINT chk_limite_positivo CHECK (limite_credito >= 0);

-- 2. Tablas Nuevas
CREATE TABLE procesos_estandar (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    producto_final_id UUID NOT NULL,
    tiempo_total_estimado INT DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_proc_std_prod FOREIGN KEY (producto_final_id) REFERENCES productos(id)
);

CREATE TABLE procesos_estandar_insumos (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    proceso_id UUID NOT NULL,
    producto_insumo_id UUID NOT NULL,
    cantidad_requerida DECIMAL(15, 4) NOT NULL,
    CONSTRAINT fk_pei_proceso FOREIGN KEY (proceso_id) REFERENCES procesos_estandar(id),
    CONSTRAINT fk_pei_insumo FOREIGN KEY (producto_insumo_id) REFERENCES productos(id)
);

CREATE TABLE auditoria_log (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    tabla_afectada VARCHAR(50) NOT NULL,
    id_registro UUID NOT NULL,
    operacion VARCHAR(10) NOT NULL,
    usuario_sistema VARCHAR(100),
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    datos_antiguos TEXT,
    datos_nuevos TEXT
);

-- 3. FUNCIONES Y TRIGGERS (Nativo Postgres)

-- Función Saldo
CREATE OR REPLACE FUNCTION fn_actualizar_saldo_cc() RETURNS TRIGGER AS $$
BEGIN
    UPDATE cuentas_corrientes
    SET saldo_actual = saldo_actual + (CASE WHEN NEW.tipo = 'DEBE' THEN NEW.monto ELSE -NEW.monto END),
        fecha_ultimo_movimiento = CURRENT_TIMESTAMP
    WHERE id = NEW.cuenta_corriente_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_actualizar_saldo_cc
AFTER INSERT ON movimientos_financieros
FOR EACH ROW EXECUTE FUNCTION fn_actualizar_saldo_cc();

-- Función Stock
CREATE OR REPLACE FUNCTION fn_impacto_stock() RETURNS TRIGGER AS $$
DECLARE 
    v_tipo_mov VARCHAR;
    v_dep_origen UUID;
    v_dep_destino UUID;
BEGIN
    SELECT tipo_movimiento, deposito_origen_id, deposito_destino_id 
    INTO v_tipo_mov, v_dep_origen, v_dep_destino
    FROM movimientos_stock WHERE id = NEW.movimiento_id;

    IF v_tipo_mov = 'ENTRADA' THEN
        INSERT INTO existencias (id, lote_id, deposito_id, cantidad)
        VALUES (gen_random_uuid(), NEW.lote_id, v_dep_destino, NEW.cantidad)
        ON CONFLICT (lote_id, deposito_id) 
        DO UPDATE SET cantidad = existencias.cantidad + NEW.cantidad;
            
    ELSIF v_tipo_mov = 'SALIDA' THEN
        UPDATE existencias 
        SET cantidad = cantidad - NEW.cantidad
        WHERE lote_id = NEW.lote_id AND deposito_id = v_dep_origen;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_impacto_stock
AFTER INSERT ON detalles_movimiento
FOR EACH ROW EXECUTE FUNCTION fn_impacto_stock();