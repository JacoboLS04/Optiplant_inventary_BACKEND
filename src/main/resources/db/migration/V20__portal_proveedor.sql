-- =====================================================================
-- V20: Portal de proveedor.
--  - Nuevo rol PROVEEDOR: los usuarios con este rol se vinculan a un
--    proveedor (columna proveedor_id en usuario).
--  - Nuevo estado CONFIRMADA en la máquina de estados de órdenes de
--    compra: BORRADOR -> ENVIADA -> CONFIRMADA -> EN_TRANSITO -> RECIBIDA.
--    ENVIADA -> CONFIRMADA y CONFIRMADA -> EN_TRANSITO las ejecuta el
--    proveedor desde su portal; el usuario interno ya no puede simularlas.
--  - Usuarios de prueba del portal para los proveedores del seed V18.
--    Contraseña de prueba: "Prueba123!" (mismo hash que V19).
-- =====================================================================

-- ---------------------------------------------------------------------
-- Vincular usuario -> proveedor (solo para el rol PROVEEDOR)
-- ---------------------------------------------------------------------
ALTER TABLE usuario ADD COLUMN proveedor_id BIGINT;

ALTER TABLE usuario
    ADD CONSTRAINT fk_usuario_proveedor
    FOREIGN KEY (proveedor_id) REFERENCES proveedor (id);

CREATE INDEX idx_usuario_proveedor ON usuario (proveedor_id);

-- ---------------------------------------------------------------------
-- Ampliar la máquina de estados con CONFIRMADA
-- ---------------------------------------------------------------------
ALTER TABLE orden_compra DROP CONSTRAINT chk_orden_compra_estado;

ALTER TABLE orden_compra
    ADD CONSTRAINT chk_orden_compra_estado
    CHECK (estado IN
        ('BORRADOR', 'ENVIADA', 'CONFIRMADA', 'EN_TRANSITO', 'RECIBIDA', 'CANCELADA'));

-- ---------------------------------------------------------------------
-- Usuarios de prueba del portal de proveedor
-- ---------------------------------------------------------------------
INSERT INTO usuario (email, password, nombre, rol, sucursal_id, proveedor_id, activo)
SELECT 'proveedor.andina@optiplant.com',
       '$2a$10$qMmk3hiRo5agLeXeynT1Uei0.xP76lycgs80T6dEJ4Xg5eYzVIUp.',
       'Andina (Portal de proveedor)', 'PROVEEDOR',
       NULL,
       (SELECT id FROM proveedor WHERE nombre = 'Distribuidora Andina S.A.C.'),
       true
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'proveedor.andina@optiplant.com');

INSERT INTO usuario (email, password, nombre, rol, sucursal_id, proveedor_id, activo)
SELECT 'proveedor.valle@optiplant.com',
       '$2a$10$qMmk3hiRo5agLeXeynT1Uei0.xP76lycgs80T6dEJ4Xg5eYzVIUp.',
       'Valle (Portal de proveedor)', 'PROVEEDOR',
       NULL,
       (SELECT id FROM proveedor WHERE nombre = 'Alimentos del Valle'),
       true
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'proveedor.valle@optiplant.com');

INSERT INTO usuario (email, password, nombre, rol, sucursal_id, proveedor_id, activo)
SELECT 'proveedor.limpieza@optiplant.com',
       '$2a$10$qMmk3hiRo5agLeXeynT1Uei0.xP76lycgs80T6dEJ4Xg5eYzVIUp.',
       'Limpieza Total (Portal de proveedor)', 'PROVEEDOR',
       NULL,
       (SELECT id FROM proveedor WHERE nombre = 'Limpieza Total EIRL'),
       true
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'proveedor.limpieza@optiplant.com');