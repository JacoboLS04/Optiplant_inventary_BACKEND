ALTER TABLE movimiento_inventario
    DROP CONSTRAINT chk_movimiento_tipo;

ALTER TABLE movimiento_inventario
    ADD CONSTRAINT chk_movimiento_tipo
    CHECK (tipo IN ('ingreso', 'retiro', 'merma'));
