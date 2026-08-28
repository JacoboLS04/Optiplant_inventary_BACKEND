CREATE TABLE lista_precio (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(150) NOT NULL,
    sucursal_id BIGINT
);

ALTER TABLE lista_precio
    ADD CONSTRAINT fk_lista_precio_sucursal
    FOREIGN KEY (sucursal_id) REFERENCES sucursal (id);

CREATE TABLE precio_producto (
    id              BIGSERIAL PRIMARY KEY,
    lista_precio_id BIGINT        NOT NULL,
    producto_id     BIGINT        NOT NULL,
    precio          NUMERIC(12,2) NOT NULL DEFAULT 0
);

ALTER TABLE precio_producto
    ADD CONSTRAINT uq_precio_producto_lista
    UNIQUE (producto_id, lista_precio_id);

ALTER TABLE precio_producto
    ADD CONSTRAINT fk_precio_producto_lista
    FOREIGN KEY (lista_precio_id) REFERENCES lista_precio (id);

ALTER TABLE precio_producto
    ADD CONSTRAINT fk_precio_producto
    FOREIGN KEY (producto_id) REFERENCES producto (id);

CREATE INDEX idx_precio_producto_lista ON precio_producto (lista_precio_id);
CREATE INDEX idx_precio_producto       ON precio_producto (producto_id);

INSERT INTO lista_precio (nombre, sucursal_id)
SELECT 'Lista Global', NULL
WHERE NOT EXISTS (SELECT 1 FROM lista_precio WHERE nombre = 'Lista Global');
