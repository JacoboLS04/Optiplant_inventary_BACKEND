CREATE TABLE venta (
    id                    BIGSERIAL PRIMARY KEY,
    codigo                VARCHAR(50)    NOT NULL,
    sucursal_id           BIGINT         NOT NULL,
    usuario_id            BIGINT,
    descuento_porcentaje  NUMERIC(5,2)   NOT NULL DEFAULT 0,
    subtotal              NUMERIC(12,2)  NOT NULL DEFAULT 0,
    total                 NUMERIC(12,2)  NOT NULL DEFAULT 0,
    unidades              NUMERIC(12,2)  NOT NULL DEFAULT 0,
    fecha                 TIMESTAMP      NOT NULL DEFAULT now(),
    created_at            TIMESTAMP      NOT NULL DEFAULT now()
);

ALTER TABLE venta ADD CONSTRAINT uq_venta_codigo UNIQUE (codigo);

ALTER TABLE venta
    ADD CONSTRAINT chk_venta_descuento
    CHECK (descuento_porcentaje BETWEEN 0 AND 100);

ALTER TABLE venta
    ADD CONSTRAINT chk_venta_total_positivo
    CHECK (total >= 0);

ALTER TABLE venta
    ADD CONSTRAINT fk_venta_sucursal
    FOREIGN KEY (sucursal_id) REFERENCES sucursal (id);

ALTER TABLE venta
    ADD CONSTRAINT fk_venta_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario (id);

CREATE INDEX idx_venta_sucursal ON venta (sucursal_id);
CREATE INDEX idx_venta_fecha    ON venta (fecha);
CREATE INDEX idx_venta_usuario  ON venta (usuario_id);

CREATE TABLE venta_linea (
    id               BIGSERIAL PRIMARY KEY,
    venta_id         BIGINT        NOT NULL,
    producto_id      BIGINT        NOT NULL,
    cantidad         NUMERIC(12,2) NOT NULL,
    precio_unitario  NUMERIC(12,2) NOT NULL,
    descuento        NUMERIC(12,2) NOT NULL DEFAULT 0,
    subtotal         NUMERIC(12,2) NOT NULL DEFAULT 0
);

ALTER TABLE venta_linea
    ADD CONSTRAINT chk_linea_cantidad_positiva
    CHECK (cantidad > 0);

ALTER TABLE venta_linea
    ADD CONSTRAINT chk_linea_subtotal_positivo
    CHECK (subtotal >= 0);

ALTER TABLE venta_linea
    ADD CONSTRAINT fk_linea_venta
    FOREIGN KEY (venta_id) REFERENCES venta (id) ON DELETE CASCADE;

ALTER TABLE venta_linea
    ADD CONSTRAINT fk_linea_producto
    FOREIGN KEY (producto_id) REFERENCES producto (id);

CREATE INDEX idx_venta_linea_venta    ON venta_linea (venta_id);
CREATE INDEX idx_venta_linea_producto ON venta_linea (producto_id);
