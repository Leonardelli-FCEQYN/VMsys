-- ================================================================
-- MVMsys V6 - Normalización de Rubro en Proveedores
-- Convierte rubro_proveedor de VARCHAR a FK hacia tabla rubros
-- Migra datos existentes sin pérdida de información
-- ================================================================

-- Paso 1: Insertar rubros únicos desde rubro_proveedor existente
-- (Solo inserta los que no estén vacíos y aún no existan en rubros)
INSERT INTO rubros (id, nombre)
SELECT DISTINCT random_uuid(), rubro_proveedor
FROM personas
WHERE rubro_proveedor IS NOT NULL 
  AND rubro_proveedor <> ''
  AND rubro_proveedor NOT IN (SELECT nombre FROM rubros);

-- Paso 2: Agregar nueva columna rubro_id como FK
ALTER TABLE personas ADD COLUMN rubro_id UUID;

-- Paso 3: Migrar datos: asignar rubro_id basándose en rubro_proveedor
UPDATE personas
SET rubro_id = (
    SELECT id FROM rubros WHERE rubros.nombre = personas.rubro_proveedor
)
WHERE rubro_proveedor IS NOT NULL AND rubro_proveedor <> '';

-- Paso 4: Crear FK constraint
ALTER TABLE personas ADD CONSTRAINT fk_personas_rubro 
    FOREIGN KEY (rubro_id) REFERENCES rubros(id);

-- Paso 5: Eliminar columna antigua (OPCIONAL - comentado por seguridad)
-- Si quieres mantener compatibilidad temporal, NO ejecutes esta línea:
-- ALTER TABLE personas DROP COLUMN rubro_proveedor;

-- NOTA: Dejamos rubro_proveedor por ahora para rollback seguro.
-- En V7 se puede eliminar después de validar en producción.
