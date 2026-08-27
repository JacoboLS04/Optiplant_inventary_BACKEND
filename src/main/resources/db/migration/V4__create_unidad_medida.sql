CREATE TABLE unidad_medida (
    id      BIGSERIAL PRIMARY KEY,
    nombre  VARCHAR(100) NOT NULL,
    simbolo VARCHAR(20)  NOT NULL
);

ALTER TABLE unidad_medida ADD CONSTRAINT uq_unidad_medida_nombre UNIQUE (nombre);
