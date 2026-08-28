CREATE TABLE transferencia (
    id                        BIGSERIAL PRIMARY KEY,
    codigo                    VARCHAR(50)   NOT NULL,
    producto_id               BIGINT        NOT NULL,
    sucursal_origen_id        BIGINT        NOT NULL,
    sucursal_destino_id       BIGINT        NOT NULL,
    usuario_solicitante_id    BIGINT,
    cantidad_solicitada       NUMERIC(12,2) NOT NULL,
    cantidad_despachada       NUMERIC(12,2),
    cantidad_recibida         NUMERIC(12,2),
    urgencia                  VARCHAR(20)   NOT NULL DEFAULT 'NORMAL',
    transportista             VARCHAR(200),
    guia                      VARCHAR(100),
    fecha_estimada_llegada    TIMESTAMP,
    estado                    VARCHAR(20)   NOT NULL DEFAULT 'SOLICITADA',
    fecha_solicitud           TIMESTAMP     NOT NULL DEFAULT now(),
    fecha_despacho            TIMESTAMP,
    fecha_recepcion           TIMESTAMP
);

ALTER TABLE transferencia ADD CONSTRAINT uq_transferencia_codigo UNIQUE (codigo);

ALTER TABLE transferencia
    ADD CONSTRAINT chk_transferencia_estado
    CHECK (estado IN ('SOLICITADA', 'RECHAZADA', 'APROBADA', 'EN_PREPARACION',
                      'EN_TRANSITO', 'RECIBIDA', 'CON_FALTANTES', 'CANCELADA'));

ALTER TABLE transferencia
    ADD CONSTRAINT chk_transferencia_urgencia
    CHECK (urgencia IN ('BAJA', 'NORMAL', 'ALTA', 'CRITICA'));

ALTER TABLE transferencia
    ADD CONSTRAINT chk_transferencia_sucursal_distinta
    CHECK (sucursal_origen_id <> sucursal_destino_id);

ALTER TABLE transferencia
    ADD CONSTRAINT chk_transferencia_cantidad
    CHECK (cantidad_solicitada > 0);

ALTER TABLE transferencia
    ADD CONSTRAINT fk_transferencia_producto
    FOREIGN KEY (producto_id) REFERENCES producto (id);

ALTER TABLE transferencia
    ADD CONSTRAINT fk_transferencia_sucursal_origen
    FOREIGN KEY (sucursal_origen_id) REFERENCES sucursal (id);

ALTER TABLE transferencia
    ADD CONSTRAINT fk_transferencia_sucursal_destino
    FOREIGN KEY (sucursal_destino_id) REFERENCES sucursal (id);

ALTER TABLE transferencia
    ADD CONSTRAINT fk_transferencia_usuario
    FOREIGN KEY (usuario_solicitante_id) REFERENCES usuario (id);

CREATE INDEX idx_transferencia_producto        ON transferencia (producto_id);
CREATE INDEX idx_transferencia_sucursal_origen ON transferencia (sucursal_origen_id);
CREATE INDEX idx_transferencia_sucursal_dest   ON transferencia (sucursal_destino_id);
CREATE INDEX idx_transferencia_estado          ON transferencia (estado);

CREATE TABLE transferencia_aprobacion (
    id                BIGSERIAL PRIMARY KEY,
    transferencia_id  BIGINT      NOT NULL,
    gerente_id        BIGINT      NOT NULL,
    rol_aprobacion    VARCHAR(20) NOT NULL,
    decision          VARCHAR(20) NOT NULL,
    fecha             TIMESTAMP   NOT NULL DEFAULT now(),
    observacion       VARCHAR(500)
);

ALTER TABLE transferencia_aprobacion
    ADD CONSTRAINT chk_aprobacion_rol
    CHECK (rol_aprobacion IN ('ORIGEN', 'DESTINO'));

ALTER TABLE transferencia_aprobacion
    ADD CONSTRAINT chk_aprobacion_decision
    CHECK (decision IN ('APROBADO', 'RECHAZADO'));

ALTER TABLE transferencia_aprobacion
    ADD CONSTRAINT fk_aprobacion_transferencia
    FOREIGN KEY (transferencia_id) REFERENCES transferencia (id);

ALTER TABLE transferencia_aprobacion
    ADD CONSTRAINT fk_aprobacion_gerente
    FOREIGN KEY (gerente_id) REFERENCES usuario (id);

CREATE INDEX idx_aprobacion_transferencia ON transferencia_aprobacion (transferencia_id);
CREATE INDEX idx_aprobacion_rol           ON transferencia_aprobacion (rol_aprobacion);

CREATE TABLE reserva_stock (
    id                BIGSERIAL PRIMARY KEY,
    transferencia_id  BIGINT        NOT NULL,
    producto_id       BIGINT        NOT NULL,
    sucursal_id       BIGINT        NOT NULL,
    cantidad          NUMERIC(12,2) NOT NULL,
    estado            VARCHAR(20)   NOT NULL DEFAULT 'ACTIVA',
    fecha_creacion    TIMESTAMP     NOT NULL DEFAULT now(),
    fecha_liberacion  TIMESTAMP
);

ALTER TABLE reserva_stock
    ADD CONSTRAINT uq_reserva_stock_transferencia UNIQUE (transferencia_id);

ALTER TABLE reserva_stock
    ADD CONSTRAINT chk_reserva_stock_estado
    CHECK (estado IN ('ACTIVA', 'LIBERADA', 'CONSUMIDA'));

ALTER TABLE reserva_stock
    ADD CONSTRAINT fk_reserva_transferencia
    FOREIGN KEY (transferencia_id) REFERENCES transferencia (id);

ALTER TABLE reserva_stock
    ADD CONSTRAINT fk_reserva_producto
    FOREIGN KEY (producto_id) REFERENCES producto (id);

ALTER TABLE reserva_stock
    ADD CONSTRAINT fk_reserva_sucursal
    FOREIGN KEY (sucursal_id) REFERENCES sucursal (id);

CREATE INDEX idx_reserva_transferencia ON reserva_stock (transferencia_id);
CREATE INDEX idx_reserva_sucursal      ON reserva_stock (sucursal_id);

CREATE TABLE transferencia_faltante (
    id                  BIGSERIAL PRIMARY KEY,
    transferencia_id    BIGINT        NOT NULL,
    cantidad_faltante   NUMERIC(12,2) NOT NULL,
    tratamiento         VARCHAR(20)   NOT NULL,
    usuario_id          BIGINT,
    fecha               TIMESTAMP     NOT NULL DEFAULT now()
);

ALTER TABLE transferencia_faltante
    ADD CONSTRAINT chk_faltante_tratamiento
    CHECK (tratamiento IN ('REENVIO', 'AJUSTE', 'RECLAMACION'));

ALTER TABLE transferencia_faltante
    ADD CONSTRAINT chk_faltante_cantidad
    CHECK (cantidad_faltante > 0);

ALTER TABLE transferencia_faltante
    ADD CONSTRAINT fk_faltante_transferencia
    FOREIGN KEY (transferencia_id) REFERENCES transferencia (id);

ALTER TABLE transferencia_faltante
    ADD CONSTRAINT fk_faltante_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario (id);

CREATE INDEX idx_faltante_transferencia ON transferencia_faltante (transferencia_id);
