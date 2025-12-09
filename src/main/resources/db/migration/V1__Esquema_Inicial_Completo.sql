-- ================================================================
-- MVMsys - Esquema de Base de Datos Inicial (V1)
-- Módulos: Configuración, Actores, Productos Base y Seguridad
-- ================================================================

-- Habilitar extensión para UUIDs (Si usamos Postgres)
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp"; 
-- En H2 la función random_uuid() ya existe.

-- ----------------------------------------------------------------
-- 1. MÓDULO DE CONFIGURACIÓN Y PARAMETRIZACIÓN (Tablas Maestras)
-- ----------------------------------------------------------------

CREATE TABLE entidades_sistema (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE, -- Ej: 'PEDIDO', 'LOTE'
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE estados (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    entidad_sistema_id UUID NOT NULL,
    nombre VARCHAR(50) NOT NULL, -- Ej: 'Pendiente', 'Aprobado'
    color_hex VARCHAR(7) DEFAULT '#FFFFFF', -- Para la UI JavaFX
    es_inicial BOOLEAN DEFAULT FALSE NOT NULL,
    es_final BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT fk_estados_entidad FOREIGN KEY (entidad_sistema_id) REFERENCES entidades_sistema(id)
);

CREATE TABLE unidades_medida (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL UNIQUE, -- Ej: 'm2', 'un'
    nombre VARCHAR(50) NOT NULL,
    permite_decimales BOOLEAN DEFAULT TRUE NOT NULL
);

CREATE TABLE tipos_producto (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE, -- Ej: 'Materia Prima'
    descripcion VARCHAR(255)
);

CREATE TABLE etapas_proceso (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE, -- Ej: 'Secado Horno'
    orden_sugerido INT,
    activo BOOLEAN DEFAULT TRUE NOT NULL
);

-- ----------------------------------------------------------------
-- 2. MÓDULO DE ACTORES Y CUENTAS CORRIENTES (Unificado)
-- ----------------------------------------------------------------

CREATE TABLE localidades (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    codigo_postal VARCHAR(10),
    provincia_nombre VARCHAR(100) -- Simplificado por ahora
);

CREATE TABLE personas (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    cuit_dni VARCHAR(13) NOT NULL, -- Con guiones o sin ellos
    razon_social VARCHAR(200) NOT NULL, -- Nombre completo o Razón Social
    email VARCHAR(100),
    telefono VARCHAR(50),
    direccion_calle VARCHAR(200),
    direccion_numero VARCHAR(20),
    localidad_id UUID,
    
    -- Roles (Flags booleanos para simplificar)
    es_cliente BOOLEAN DEFAULT FALSE NOT NULL,
    es_proveedor BOOLEAN DEFAULT FALSE NOT NULL,
    es_empleado BOOLEAN DEFAULT FALSE NOT NULL,
    
    fecha_alta TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    
    CONSTRAINT uk_personas_cuit UNIQUE (cuit_dni),
    CONSTRAINT fk_personas_localidad FOREIGN KEY (localidad_id) REFERENCES localidades(id)
);

-- Cuenta Corriente Única por Persona
CREATE TABLE cuentas_corrientes (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    persona_id UUID NOT NULL,
    saldo_actual DECIMAL(15, 2) DEFAULT 0.00 NOT NULL,
    fecha_ultimo_movimiento TIMESTAMP,
    
    CONSTRAINT uk_cc_persona UNIQUE (persona_id), -- Relación 1 a 1 estricta
    CONSTRAINT fk_cc_persona FOREIGN KEY (persona_id) REFERENCES personas(id)
);

-- ----------------------------------------------------------------
-- 3. MÓDULO DE SEGURIDAD BÁSICA
-- ----------------------------------------------------------------

CREATE TABLE roles_usuario (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE -- Ej: 'ADMIN', 'VENTAS'
);

CREATE TABLE usuarios (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL, -- BCrypt
    persona_id UUID NOT NULL, -- Vínculo con el empleado real
    rol_id UUID NOT NULL,
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    
    CONSTRAINT fk_usuarios_persona FOREIGN KEY (persona_id) REFERENCES personas(id),
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol_id) REFERENCES roles_usuario(id)
);

-- ----------------------------------------------------------------
-- 4. MÓDULO DE PRODUCTOS (Definición Base)
-- ----------------------------------------------------------------

CREATE TABLE rubros (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE productos (
    id UUID DEFAULT random_uuid() PRIMARY KEY,
    codigo_sku VARCHAR(50) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    
    unidad_medida_id UUID NOT NULL,
    tipo_producto_id UUID NOT NULL,
    rubro_id UUID,
    
    stock_minimo DECIMAL(12, 3) DEFAULT 0,
    costo_reposicion DECIMAL(15, 2) DEFAULT 0, -- Último costo conocido
    precio_venta_base DECIMAL(15, 2) DEFAULT 0,
    
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    activo BOOLEAN DEFAULT TRUE NOT NULL,

    CONSTRAINT uk_productos_sku UNIQUE (codigo_sku),
    CONSTRAINT fk_productos_unidad FOREIGN KEY (unidad_medida_id) REFERENCES unidades_medida(id),
    CONSTRAINT fk_productos_tipo FOREIGN KEY (tipo_producto_id) REFERENCES tipos_producto(id),
    CONSTRAINT fk_productos_rubro FOREIGN KEY (rubro_id) REFERENCES rubros(id)
);

-- ================================================================
-- FIN DEL ESQUEMA INICIAL
-- Las tablas de Lotes, Movimientos y Producción irán en la V2
-- ================================================================