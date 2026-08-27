CREATE TABLE existencia (
    id                  BIGSERIAL PRIMARY KEY,
    producto_id         BIGINT       NOT NULL,
    sucursal_id         BIGINT       NOT NULL,
    cantidad_fisica     NUMERIC(12,2) NOT NULL DEFAULT 0,
    cantidad_reservada  NUMERIC(12,2) NOT NULL DEFAULT 0,
    stock_minimo        NUMERIC(12,2) NOT NULL DEFAULT 0,
    updated_at          TIMESTAMP    NOT NULL DEFAULT now()
);

ALTER TABLE existencia
    ADD CONSTRAINT uq_existencia_producto_sucursal
    UNIQUE (producto_id, sucursal_id);

ALTER TABLE existencia
    ADD CONSTRAINT fk_existencia_producto
    FOREIGN KEY (producto_id) REFERENCES producto (id);

ALTER TABLE existencia
    ADD CONSTRAINT fk_existencia_sucursal
    FOREIGN KEY (sucursal_id) REFERENCES sucursal (id);
