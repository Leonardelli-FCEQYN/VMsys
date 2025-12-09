-- ================================================================
-- MVMsys V8 - Sistema de Catálogos Maestros Dinámicos
-- Convierte valores hardcodeados en tablas maestras configurables
-- Categorías de Cliente, Rubros, etc. ahora son 100% dinámicos
-- ================================================================

-- 1. TABLA DE CATEGORÍAS DE CLIENTE (IRQ-06)
CREATE TABLE categorias_cliente (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    color_hex VARCHAR(7) DEFAULT '#6366F1', -- Para UI
    icono VARCHAR(50), -- Ej: '👥', 'M', 'F'
    activa BOOLEAN DEFAULT TRUE NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    CONSTRAINT chk_nombre_no_vacio CHECK (CHAR_LENGTH(TRIM(nombre)) > 0)
);

-- 2. MIGRAR DATOS EXISTENTES DE categoriaCliente (string) a FK
-- Paso 1: Crear categorías por defecto
INSERT INTO categorias_cliente (nombre, descripcion, icono, activa) VALUES
('MAYORISTA', 'Compra en grandes volúmenes', '📦', TRUE),
('MINORISTA', 'Compra en menor cantidad', '🛒', TRUE),
('EVENTUAL', 'Compras ocasionales', '⏰', TRUE),
('FRECUENTE', 'Cliente habitual', '⭐', TRUE);

-- Paso 2: Agregar columna categoriaCliente_id como FK en personas
ALTER TABLE personas ADD COLUMN categoria_cliente_id UUID;

-- Paso 3: Migrar valores string -> IDs
UPDATE personas SET categoria_cliente_id = (
    SELECT id FROM categorias_cliente 
    WHERE UPPER(categorias_cliente.nombre) = UPPER(personas.categoria_cliente)
)
WHERE categoria_cliente IS NOT NULL;

-- Paso 4: Agregar constraint FK
ALTER TABLE personas ADD CONSTRAINT fk_personas_categoria_cliente 
    FOREIGN KEY (categoria_cliente_id) REFERENCES categorias_cliente(id);

-- Paso 5: Mantener columna vieja como deprecated para rollback seguro
ALTER TABLE personas RENAME COLUMN categoria_cliente TO categoria_cliente_deprecated;

-- 3. ÍNDICES PARA PERFORMANCE
CREATE INDEX idx_categorias_cliente_activa ON categorias_cliente(activa);
CREATE INDEX idx_personas_categoria_cliente ON personas(categoria_cliente_id);

-- ================================================================
-- NOTAS:
-- - La columna categoria_cliente_deprecated se puede eliminar en V9
-- - Todas las categorías son modificables, activables/desactivables
-- - El sistema está listo para más tablas maestras en el futuro
-- ================================================================
