CREATE TABLE producto (
    id              BIGSERIAL PRIMARY KEY,
    sku             VARCHAR(20)  NOT NULL,
    nombre          VARCHAR(200) NOT NULL,
    descripcion     TEXT,
    estado          VARCHAR(20)  NOT NULL DEFAULT 'activo',
    categoria_id    BIGINT       NOT NULL,
    unidad_base_id  BIGINT       NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

ALTER TABLE producto ADD CONSTRAINT uq_producto_sku UNIQUE (sku);

ALTER TABLE producto
    ADD CONSTRAINT fk_producto_categoria
    FOREIGN KEY (categoria_id) REFERENCES categoria (id);

ALTER TABLE producto
    ADD CONSTRAINT fk_producto_unidad_medida
    FOREIGN KEY (unidad_base_id) REFERENCES unidad_medida (id);
