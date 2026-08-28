CREATE TABLE proveedor (
    id                    BIGSERIAL PRIMARY KEY,
    nombre                VARCHAR(200) NOT NULL,
    contacto              VARCHAR(200),
    condiciones_generales TEXT
);

CREATE TABLE orden_compra (
    id                         BIGSERIAL PRIMARY KEY,
    codigo                     VARCHAR(50)  NOT NULL,
    proveedor_id               BIGINT       NOT NULL,
    sucursal_destino_id        BIGINT       NOT NULL,
    usuario_id                 BIGINT,
    estado                     VARCHAR(20)  NOT NULL DEFAULT 'BORRADOR',
    fecha_emision              TIMESTAMP    NOT NULL DEFAULT now(),
    fecha_entrega_estimada     DATE,
    transportista              VARCHAR(200),
    guia                       VARCHAR(100),
    condiciones_pago           VARCHAR(300),
    created_at                 TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at                 TIMESTAMP    NOT NULL DEFAULT now()
);

ALTER TABLE orden_compra ADD CONSTRAINT uq_orden_compra_codigo UNIQUE (codigo);

ALTER TABLE orden_compra
    ADD CONSTRAINT chk_orden_compra_estado
    CHECK (estado IN ('BORRADOR', 'ENVIADA', 'EN_TRANSITO', 'RECIBIDA', 'CANCELADA'));

ALTER TABLE orden_compra
    ADD CONSTRAINT fk_orden_compra_proveedor
    FOREIGN KEY (proveedor_id) REFERENCES proveedor (id);

ALTER TABLE orden_compra
    ADD CONSTRAINT fk_orden_compra_sucursal
    FOREIGN KEY (sucursal_destino_id) REFERENCES sucursal (id);

ALTER TABLE orden_compra
    ADD CONSTRAINT fk_orden_compra_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario (id);

CREATE INDEX idx_orden_compra_proveedor  ON orden_compra (proveedor_id);
CREATE INDEX idx_orden_compra_sucursal   ON orden_compra (sucursal_destino_id);
CREATE INDEX idx_orden_compra_estado     ON orden_compra (estado);

CREATE TABLE orden_compra_linea (
    id                BIGSERIAL PRIMARY KEY,
    orden_compra_id   BIGINT        NOT NULL,
    producto_id       BIGINT        NOT NULL,
    cantidad_ordenada NUMERIC(12,2) NOT NULL,
    cantidad_recibida NUMERIC(12,2) NOT NULL DEFAULT 0,
    precio_unitario   NUMERIC(12,2) NOT NULL DEFAULT 0,
    descuento         NUMERIC(12,2) NOT NULL DEFAULT 0
);

ALTER TABLE orden_compra_linea
    ADD CONSTRAINT chk_linea_cantidad_ordenada
    CHECK (cantidad_ordenada > 0);

ALTER TABLE orden_compra_linea
    ADD CONSTRAINT fk_linea_orden_compra
    FOREIGN KEY (orden_compra_id) REFERENCES orden_compra (id);

ALTER TABLE orden_compra_linea
    ADD CONSTRAINT fk_linea_producto
    FOREIGN KEY (producto_id) REFERENCES producto (id);

CREATE INDEX idx_linea_orden      ON orden_compra_linea (orden_compra_id);
CREATE INDEX idx_linea_producto   ON orden_compra_linea (producto_id);
