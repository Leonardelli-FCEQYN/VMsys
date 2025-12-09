-- ================================================================
-- MVMsys V4 - Validaciones de Integridad para Personas
-- ================================================================

-- 1. VALIDACIÓN DE CUIT / DNI
-- Acepta:
--   - DNI puro: 7 u 8 dígitos (ej: 12345678)
--   - CUIT puro: 11 dígitos (ej: 20123456789)
--   - CUIT con guiones: XX-XXXXXXXX-X (ej: 20-12345678-9)
ALTER TABLE personas
ADD CONSTRAINT chk_persona_cuit_format 
CHECK (
    cuit_dni ~ '^\d{7,8}$' OR          -- DNI
    cuit_dni ~ '^\d{11}$' OR           -- CUIT sin guiones
    cuit_dni ~ '^\d{2}-\d{8}-\d{1}$'   -- CUIT con guiones
);

-- 2. VALIDACIÓN DE TELÉFONO
-- Solo permite números, espacios, +, - y paréntesis. No permite letras.
-- Ejemplos válidos: "3758 123456", "+54 9 11...", "(011) 456-7890"
ALTER TABLE personas
ADD CONSTRAINT chk_persona_telefono_format 
CHECK (
    telefono IS NULL OR  -- Permite nulos si no es obligatorio
    telefono ~ '^[0-9+\-\s()]+$'
);