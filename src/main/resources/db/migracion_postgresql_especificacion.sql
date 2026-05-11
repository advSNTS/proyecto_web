-- Revisión DBA: migración hacia el modelo multiempresa/pools/mensajes.
-- Ejecutar en orden sobre PostgreSQL existente (ajustar nombres de esquema si aplica).

-- Empleados: administrador global
ALTER TABLE empleados ADD COLUMN IF NOT EXISTS admin_global BOOLEAN NOT NULL DEFAULT false;

-- Procesos: empresa, pool, estado (sustituye borrador/activo en dominio JPA)
ALTER TABLE procesos ADD COLUMN IF NOT EXISTS nit VARCHAR(255);
ALTER TABLE procesos ADD COLUMN IF NOT EXISTS pool_id BIGINT;
ALTER TABLE procesos ADD COLUMN IF NOT EXISTS estado VARCHAR(20);

-- Backfill estado desde columnas legadas (si existían)
UPDATE procesos SET estado = CASE
    WHEN activo = false THEN 'INACTIVO'
    WHEN borrador = true THEN 'BORRADOR'
    ELSE 'PUBLICADO'
END WHERE estado IS NULL;

-- Pools por empresa existente sin pool (ejemplo: un pool default por NIT)
CREATE TABLE IF NOT EXISTS pools (
    id BIGSERIAL PRIMARY KEY,
    nit VARCHAR(255) NOT NULL REFERENCES empresas(nit),
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500),
    es_default BOOLEAN NOT NULL DEFAULT false,
    eliminado BOOLEAN NOT NULL DEFAULT false
);

INSERT INTO pools (nit, nombre, descripcion, es_default, eliminado)
SELECT e.nit, 'Pool por defecto', 'Migración automática', true, false
FROM empresas e
WHERE e.deleted = false
  AND NOT EXISTS (SELECT 1 FROM pools p WHERE p.nit = e.nit AND p.es_default = true AND p.eliminado = false);

UPDATE procesos p SET nit = (
    SELECT ep.nit_owner FROM empresa_proceso ep WHERE ep.id_proceso = p.id AND ep.deleted = false LIMIT 1
) WHERE p.nit IS NULL;

UPDATE procesos p SET pool_id = (
    SELECT po.id FROM pools po WHERE po.nit = p.nit AND po.es_default = true AND po.eliminado = false LIMIT 1
) WHERE p.pool_id IS NULL AND p.nit IS NOT NULL;

-- Tras verificar datos, aplicar NOT NULL (comentar hasta validar):
-- ALTER TABLE procesos ALTER COLUMN nit SET NOT NULL;
-- ALTER TABLE procesos ALTER COLUMN pool_id SET NOT NULL;
-- ALTER TABLE procesos ALTER COLUMN estado SET NOT NULL;

-- Roles funcionales: descripción
ALTER TABLE roles ADD COLUMN IF NOT EXISTS descripcion VARCHAR(500);

-- Nodos y arcos: eliminación lógica
ALTER TABLE nodos ADD COLUMN IF NOT EXISTS eliminado BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE arcos ADD COLUMN IF NOT EXISTS eliminado BOOLEAN NOT NULL DEFAULT false;

-- Actividades: lane y tipo
ALTER TABLE actividades ADD COLUMN IF NOT EXISTS tipo_actividad VARCHAR(100);
ALTER TABLE actividades ADD COLUMN IF NOT EXISTS lane_id BIGINT;

CREATE TABLE IF NOT EXISTS lanes (
    id BIGSERIAL PRIMARY KEY,
    pool_id BIGINT NOT NULL REFERENCES pools(id),
    nombre VARCHAR(150) NOT NULL,
    rol_id BIGINT NOT NULL REFERENCES roles(id),
    eliminado BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS idx_lanes_pool_rol_activo
    ON lanes (pool_id, rol_id)
    WHERE eliminado = false;

ALTER TABLE actividades ADD CONSTRAINT fk_actividad_lane
    FOREIGN KEY (lane_id) REFERENCES lanes(id);

-- Tablas nuevas (resumen; Hibernate ddl-auto=update también las crea en entornos de desarrollo)
CREATE TABLE IF NOT EXISTS roles_pool (
    id BIGSERIAL PRIMARY KEY,
    pool_id BIGINT NOT NULL REFERENCES pools(id),
    nombre VARCHAR(100) NOT NULL,
    permisos JSONB,
    eliminado BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS usuario_rol_pool (
    id BIGSERIAL PRIMARY KEY,
    empleado_id BIGINT NOT NULL REFERENCES empleados(id),
    rol_pool_id BIGINT NOT NULL REFERENCES roles_pool(id),
    eliminado BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS proceso_compartido (
    id BIGSERIAL PRIMARY KEY,
    proceso_id BIGINT NOT NULL REFERENCES procesos(id),
    pool_id BIGINT NOT NULL REFERENCES pools(id),
    permiso VARCHAR(20) NOT NULL,
    eliminado BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS mensajes_throw (
    id BIGSERIAL PRIMARY KEY,
    proceso_id BIGINT NOT NULL REFERENCES procesos(id),
    nombre_mensaje VARCHAR(200) NOT NULL,
    payload_template TEXT,
    eliminado BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS mensajes_catch (
    id BIGSERIAL PRIMARY KEY,
    proceso_id BIGINT NOT NULL REFERENCES procesos(id),
    nombre_mensaje VARCHAR(200) NOT NULL,
    correlacion_expr VARCHAR(500),
    iniciar_nueva_instancia BOOLEAN NOT NULL DEFAULT false,
    eliminado BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS mensajes_externos (
    id BIGSERIAL PRIMARY KEY,
    destino_tipo VARCHAR(30) NOT NULL,
    configuracion TEXT,
    credenciales TEXT,
    eliminado BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS tareas_integracion (
    id BIGSERIAL PRIMARY KEY,
    proceso_id BIGINT NOT NULL REFERENCES procesos(id),
    mensaje_externo_id BIGINT NOT NULL REFERENCES mensajes_externos(id),
    payload_mapping TEXT,
    eliminado BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS empleado_rol_sistema (
    id BIGSERIAL PRIMARY KEY,
    empleado_id BIGINT NOT NULL REFERENCES empleados(id),
    nit VARCHAR(255) REFERENCES empresas(nit),
    tipo_rol VARCHAR(20) NOT NULL,
    eliminado BOOLEAN NOT NULL DEFAULT false
);
