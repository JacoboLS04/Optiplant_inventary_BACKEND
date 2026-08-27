CREATE TABLE movimiento_inventario (
    id              BIGSERIAL PRIMARY KEY,
    producto_id     BIGINT         NOT NULL,
    sucursal_id     BIGINT         NOT NULL,
    tipo            VARCHAR(20)    NOT NULL,
    motivo          VARCHAR(300)   NOT NULL,
    cantidad        NUMERIC(12,2)  NOT NULL,
    usuario_id      BIGINT,
    fecha           TIMESTAMP      NOT NULL DEFAULT now()
);

ALTER TABLE movimiento_inventario
    ADD CONSTRAINT chk_movimiento_tipo
    CHECK (tipo IN ('ingreso', 'retiro'));

ALTER TABLE movimiento_inventario
    ADD CONSTRAINT chk_movimiento_cantidad_positiva
    CHECK (cantidad > 0);

ALTER TABLE movimiento_inventario
    ADD CONSTRAINT fk_movimiento_producto
    FOREIGN KEY (producto_id) REFERENCES producto (id);

ALTER TABLE movimiento_inventario
    ADD CONSTRAINT fk_movimiento_sucursal
    FOREIGN KEY (sucursal_id) REFERENCES sucursal (id);

ALTER TABLE movimiento_inventario
    ADD CONSTRAINT fk_movimiento_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario (id);

CREATE INDEX idx_movimiento_producto ON movimiento_inventario (producto_id);
CREATE INDEX idx_movimiento_sucursal ON movimiento_inventario (sucursal_id);
CREATE INDEX idx_movimiento_fecha    ON movimiento_inventario (fecha);
