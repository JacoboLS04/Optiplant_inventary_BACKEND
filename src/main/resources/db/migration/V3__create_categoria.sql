CREATE TABLE categoria (
    id      BIGSERIAL PRIMARY KEY,
    nombre  VARCHAR(150) NOT NULL
);

ALTER TABLE categoria ADD CONSTRAINT uq_categoria_nombre UNIQUE (nombre);
