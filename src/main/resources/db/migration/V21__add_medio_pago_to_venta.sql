ALTER TABLE venta ADD COLUMN medio_pago VARCHAR(50);

CREATE INDEX idx_venta_medio_pago ON venta (medio_pago);
