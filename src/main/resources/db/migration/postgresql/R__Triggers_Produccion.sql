-- Función Nativa para Consumo de Producción
CREATE OR REPLACE FUNCTION fn_consumo_produccion() RETURNS TRIGGER AS $$
BEGIN
    -- 1. Descontar del Comprometido (Reserva)
    UPDATE productos 
    SET stock_comprometido = stock_comprometido - NEW.cantidad_consumida
    WHERE id = NEW.producto_insumo_id;
    
    -- 2. Descontar del Stock Físico (Lógica FIFO automática)
    -- Esto es una simplificación, idealmente se pasa el ID de lote.
    UPDATE existencias
    SET cantidad = cantidad - NEW.cantidad_consumida
    WHERE id = (
        SELECT e.id FROM existencias e
        JOIN lotes l ON e.lote_id = l.id
        WHERE l.producto_id = NEW.producto_insumo_id
        AND e.cantidad >= NEW.cantidad_consumida
        ORDER BY l.fecha_vencimiento ASC NULLS LAST
        LIMIT 1
    );
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_consumo_produccion
AFTER INSERT ON consumos_produccion
FOR EACH ROW EXECUTE FUNCTION fn_consumo_produccion();
