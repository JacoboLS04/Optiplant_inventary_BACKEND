CREATE TABLE usuario (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(150) NOT NULL,
    rol         VARCHAR(50)  NOT NULL,
    sucursal_id BIGINT
);

ALTER TABLE usuario
    ADD CONSTRAINT fk_usuario_sucursal
    FOREIGN KEY (sucursal_id) REFERENCES sucursal (id);
