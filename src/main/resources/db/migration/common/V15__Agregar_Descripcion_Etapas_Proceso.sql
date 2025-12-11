-- V15: Reconstruir tabla etapas_proceso con todas las columnas correctas
-- Primero eliminar las FK dependientes
ALTER TABLE ordenes_produccion_etapas DROP CONSTRAINT IF EXISTS fk_ope_etapa;

-- Luego eliminar la tabla
DROP TABLE IF EXISTS etapas_proceso;

-- Recrear con todas las columnas
CREATE TABLE etapas_proceso (
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

-- Restaurar la FK
ALTER TABLE ordenes_produccion_etapas 
ADD CONSTRAINT fk_ope_etapa FOREIGN KEY (etapa_proceso_id) REFERENCES etapas_proceso(id);
