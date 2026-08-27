CREATE TABLE sucursal (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(150) NOT NULL,
    direccion   VARCHAR(300),
    estado      VARCHAR(20)  NOT NULL DEFAULT 'activa'
);

ALTER TABLE sucursal ADD CONSTRAINT uq_sucursal_nombre UNIQUE (nombre);
