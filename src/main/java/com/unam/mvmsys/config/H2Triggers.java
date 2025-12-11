package com.unam.mvmsys.config;

import org.h2.api.Trigger;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Implementación de Triggers para H2 (Modo Offline).
 * H2 requiere que la lógica compleja resida en Java.
 */
public class H2Triggers {

    // --- TRIGGER 1: Actualizar Saldo Cuenta Corriente ---
    public static class ActualizarSaldoCC implements Trigger {
        @Override
        public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type) {}

        @Override
        public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
            // newRow contiene las columnas de 'movimientos_financieros' en orden de creación
            // Índices aproximados: 0:ID, 1:CC_ID, 2:FECHA, 3:TIPO, 4:CONCEPTO, 5:MONTO...
            
            // Nota: Es más seguro obtener los datos asumiendo el orden del CREATE TABLE
            // UUID cuentaCorrienteId = (UUID) newRow[1]; (Ajustar según tu esquema real si cambia)
            
            // Para mayor seguridad en H2, usaremos una query directa usando los valores
            String tipo = (String) newRow[3]; // Columna 'tipo'
            BigDecimal monto = (BigDecimal) newRow[5]; // Columna 'monto'
            UUID ccId = (UUID) newRow[1]; // Columna 'cuenta_corriente_id'

            BigDecimal impacto = "DEBE".equals(tipo) ? monto : monto.negate();

            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE cuentas_corrientes SET saldo_actual = saldo_actual + ?, fecha_ultimo_movimiento = CURRENT_TIMESTAMP WHERE id = ?")) {
                stmt.setBigDecimal(1, impacto);
                stmt.setObject(2, ccId);
                stmt.executeUpdate();
            }
        }

        @Override
        public void close() {}

        @Override
        public void remove() {}
    }

    // --- TRIGGER 2: Impacto de Stock ---
    public static class ImpactoStock implements Trigger {
        @Override
        public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type) {}

        @Override
        public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
            // newRow mapea a 'detalles_movimiento'
            // 0:ID, 1:MOV_ID, 2:LOTE_ID, 3:CANTIDAD
            
            UUID movId = (UUID) newRow[1];
            UUID loteId = (UUID) newRow[2];
            BigDecimal cantidad = (BigDecimal) newRow[3];

            // 1. Obtener datos de la cabecera (MovimientoStock)
            String tipoMov = "";
            UUID depositoDestino = null;
            UUID depositoOrigen = null;

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT tipo_movimiento, deposito_origen_id, deposito_destino_id FROM movimientos_stock WHERE id = ?")) {
                stmt.setObject(1, movId);
                var rs = stmt.executeQuery();
                if (rs.next()) {
                    tipoMov = rs.getString("tipo_movimiento");
                    depositoOrigen = (UUID) rs.getObject("deposito_origen_id");
                    depositoDestino = (UUID) rs.getObject("deposito_destino_id");
                }
            }

            // 2. Aplicar lógica
            if ("ENTRADA".equals(tipoMov) && depositoDestino != null) {
                // Upsert manual para H2
                try (PreparedStatement stmt = conn.prepareStatement(
                        "MERGE INTO existencias (lote_id, deposito_id, cantidad) KEY (lote_id, deposito_id) VALUES (?, ?, ?)")) {
                    stmt.setObject(1, loteId);
                    stmt.setObject(2, depositoDestino);
                    stmt.setBigDecimal(3, cantidad); // MERGE en H2 suma? No, MERGE reemplaza o inserta.
                    // Para sumar hay que hacer logica extra, pero por simplicidad de H2 asumimos MERGE de inserción inicial o ajuste
                    // OJO: Para sumar correctamente en H2 SQL puro es complejo.
                    // Vamos a hacer un UPDATE primero, si afecta 0 filas, hacemos INSERT.
                }
                // Corrección Lógica Suma:
                try (PreparedStatement update = conn.prepareStatement("UPDATE existencias SET cantidad = cantidad + ? WHERE lote_id = ? AND deposito_id = ?")) {
                    update.setBigDecimal(1, cantidad);
                    update.setObject(2, loteId);
                    update.setObject(3, depositoDestino);
                    int rows = update.executeUpdate();
                    if (rows == 0) {
                        try (PreparedStatement insert = conn.prepareStatement("INSERT INTO existencias (lote_id, deposito_id, cantidad) VALUES (?, ?, ?)")) {
                            insert.setObject(1, loteId);
                            insert.setObject(2, depositoDestino);
                            insert.setBigDecimal(3, cantidad);
                            insert.executeUpdate();
                        }
                    }
                }

            } else if ("SALIDA".equals(tipoMov) && depositoOrigen != null) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE existencias SET cantidad = cantidad - ? WHERE lote_id = ? AND deposito_id = ?")) {
                    stmt.setBigDecimal(1, cantidad);
                    stmt.setObject(2, loteId);
                    stmt.setObject(3, depositoOrigen);
                    stmt.executeUpdate();
                }
            }
        }

        

        @Override
        public void close() {}
        @Override
        public void remove() {}
    }

    // --- TRIGGER 3: Consumo Producción (Actualiza Stock y Comprometido) ---
    public static class ConsumoProduccion implements Trigger {
        @Override
        public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type) {}

        @Override
        public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
            // newRow mapea a 'consumos_produccion'
            // Estructura: ID, ORDEN_ID, ETAPA_ID, PRODUCTO_ID, CANTIDAD...

            UUID productoId = (UUID) newRow[3]; // producto_insumo_id
            BigDecimal cantidad = (BigDecimal) newRow[4]; // cantidad_consumida

            // 1. Descontar del Stock Comprometido (porque ya se usó) en tabla PRODUCTOS
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE productos SET stock_comprometido = stock_comprometido - ? WHERE id = ?")) {
                stmt.setBigDecimal(1, cantidad);
                stmt.setObject(2, productoId);
                stmt.executeUpdate();
            }

            // 2. Descontar del Stock Físico (EXISTENCIAS)
            // Nota: Aquí asumimos una lógica FIFO o un depósito por defecto para H2 simple.
            // En producción real, el usuario debería elegir de qué lote consume.
            // Para este ejemplo offline, descontamos del primer lote disponible.
            try (PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE existencias SET cantidad = cantidad - ? WHERE id = (SELECT TOP 1 id FROM existencias WHERE lote_id IN (SELECT id FROM lotes WHERE producto_id = ?) AND cantidad >= ? ORDER BY fecha_entrada ASC)")) {
                stmt.setBigDecimal(1, cantidad);
                stmt.setObject(2, productoId);
                stmt.setBigDecimal(3, cantidad);
                int updated = stmt.executeUpdate();

                if (updated == 0) {
                    // Si no encontró lote suficiente, descontar de cualquiera (permitir negativo temporal en offline)
                     try(PreparedStatement forceStmt = conn.prepareStatement(
                         "UPDATE existencias SET cantidad = cantidad - ? WHERE id = (SELECT TOP 1 id FROM existencias WHERE lote_id IN (SELECT id FROM lotes WHERE producto_id = ?))")) {
                         forceStmt.setBigDecimal(1, cantidad);
                         forceStmt.setObject(2, productoId);
                         forceStmt.executeUpdate();
                     }
                }
            }
        }

        @Override
        public void close() {}
        @Override
        public void remove() {}
    }

    
}