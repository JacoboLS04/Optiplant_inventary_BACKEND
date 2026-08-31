-- =====================================================================
-- V18: Datos de prueba para bodega genérica (no solo plantas/fertilizantes).
-- Crea sucursales, usuarios por rol, catálogos, existencias, listas de
-- precio, compras, transferencias y ventas de ejemplo para probar el
-- sistema multi-sucursal end-to-end.
--
-- Todas las FK se resuelven con subconsultas sobre campos únicos para no
-- depender de IDs concretos (BIGSERIAL).
-- =====================================================================

-- ---------------------------------------------------------------------
-- SUCURSALES
-- ---------------------------------------------------------------------
INSERT INTO sucursal (nombre, direccion, estado) VALUES
    ('Bodega Central', 'Av. Los Libertadores 1200, Zona Industrial', 'activa'),
    ('Sucursal Norte',  'Av. Santa Ana 500, Sector Norte',          'activa'),
    ('Sucursal Sur',    'Calle Los Naranjos 340, Sector Sur',       'activa'),
    ('Sucursal Centro', 'Jr. Bolívar 88, Centro de la ciudad',      'activa');

-- ---------------------------------------------------------------------
-- USUARIOS POR ROL
-- Contraseña de prueba para todos: "Prueba123!"
-- (el gestor de contraseñas se hace con BCrypt; este hash es válido)
-- ---------------------------------------------------------------------
INSERT INTO usuario (email, password, nombre, rol, sucursal_id, activo)
SELECT 'gerente@bodega.com',
       '$2a$10$ma82Y4MH7qYyo/Q1YsY1L.MzaOMsPQYRpAIFaFCHFiZGeEFuHcZyG',
       'Gonzalo Rivas', 'GERENTE',
       (SELECT id FROM sucursal WHERE nombre = 'Bodega Central'),
       true
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'gerente@bodega.com');

INSERT INTO usuario (email, password, nombre, rol, sucursal_id, activo)
SELECT 'operador.norte@bodega.com',
       '$2a$10$ma82Y4MH7qYyo/Q1YsY1L.MzaOMsPQYRpAIFaFCHFiZGeEFuHcZyG',
       'Elena Castro', 'OPERADOR',
       (SELECT id FROM sucursal WHERE nombre = 'Sucursal Norte'),
       true
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'operador.norte@bodega.com');

INSERT INTO usuario (email, password, nombre, rol, sucursal_id, activo)
SELECT 'operador.sur@bodega.com',
       '$2a$10$ma82Y4MH7qYyo/Q1YsY1L.MzaOMsPQYRpAIFaFCHFiZGeEFuHcZyG',
       'Pedro Salas', 'OPERADOR',
       (SELECT id FROM sucursal WHERE nombre = 'Sucursal Sur'),
       true
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'operador.sur@bodega.com');

INSERT INTO usuario (email, password, nombre, rol, sucursal_id, activo)
SELECT 'operador.centro@bodega.com',
       '$2a$10$ma82Y4MH7qYyo/Q1YsY1L.MzaOMsPQYRpAIFaFCHFiZGeEFuHcZyG',
       'Lucía Marín', 'OPERADOR',
       (SELECT id FROM sucursal WHERE nombre = 'Sucursal Centro'),
       true
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'operador.centro@bodega.com');

INSERT INTO usuario (email, password, nombre, rol, sucursal_id, activo)
SELECT 'cajero@bodega.com',
       '$2a$10$ma82Y4MH7qYyo/Q1YsY1L.MzaOMsPQYRpAIFaFCHFiZGeEFuHcZyG',
       'Marco Díaz', 'OPERADOR',
       (SELECT id FROM sucursal WHERE nombre = 'Bodega Central'),
       true
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'cajero@bodega.com');

-- ---------------------------------------------------------------------
-- CATEGORÍAS (bodega genérica)
-- ---------------------------------------------------------------------
INSERT INTO categoria (nombre) VALUES
    ('Alimentos'), ('Bebidas'), ('Limpieza'), ('Mercancía general'),
    ('Papelería'), ('Higiene personal') ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------
-- UNIDADES DE MEDIDA
-- ---------------------------------------------------------------------
INSERT INTO unidad_medida (nombre, simbolo) VALUES
    ('Kilogramo', 'kg'), ('Gramo', 'g'), ('Litro', 'L'), ('Mililitro', 'ml'),
    ('Unidad', 'und'), ('Caja', 'caja'), ('Paquete', 'paq'), ('Metro', 'm')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------
-- PRODUCTOS
-- ---------------------------------------------------------------------
INSERT INTO producto (sku, nombre, descripcion, estado, categoria_id, unidad_base_id) VALUES
    ('ALM-001', 'Arroz Integral 1kg',   'Arroz integral de grano largo, bolsa 1 kg', 'activo',
     (SELECT id FROM categoria WHERE nombre = 'Alimentos'),
     (SELECT id FROM unidad_medida WHERE simbolo = 'kg')),
    ('ALM-002', 'Fideos Tallarines 500g', 'Pasta de trigo duro, paquete 500 g', 'activo',
     (SELECT id FROM categoria WHERE nombre = 'Alimentos'),
     (SELECT id FROM unidad_medida WHERE simbolo = 'g')),
    ('ALM-003', 'Aceite Vegetal 1L',   'Aceite de girasol refinado, botella 1 L', 'activo',
     (SELECT id FROM categoria WHERE nombre = 'Alimentos'),
     (SELECT id FROM unidad_medida WHERE simbolo = 'L')),
    ('BEB-001', 'Agua Mineral 600ml',  'Agua embotellada sin gas, 600 ml', 'activo',
     (SELECT id FROM categoria WHERE nombre = 'Bebidas'),
     (SELECT id FROM unidad_medida WHERE simbolo = 'ml')),
    ('BEB-002', 'Refresco Cola 2L',    'Gaseosa sabor cola, botella 2 L', 'activo',
     (SELECT id FROM categoria WHERE nombre = 'Bebidas'),
     (SELECT id FROM unidad_medida WHERE simbolo = 'L')),
    ('BEB-003', 'Café Molido 250g',    'Café torrefacto molido, bolsa 250 g', 'activo',
     (SELECT id FROM categoria WHERE nombre = 'Bebidas'),
     (SELECT id FROM unidad_medida WHERE simbolo = 'g')),
    ('LIM-001', 'Detergente en Polvo 3kg', 'Detergente multiusos, caja 3 kg', 'activo',
     (SELECT id FROM categoria WHERE nombre = 'Limpieza'),
     (SELECT id FROM unidad_medida WHERE simbolo = 'kg')),
    ('LIM-002', 'Lavandina 1L',        'Hipoclorito de sodio, bidón 1 L', 'activo',
     (SELECT id FROM categoria WHERE nombre = 'Limpieza'),
     (SELECT id FROM unidad_medida WHERE simbolo = 'L')),
    ('LIM-003', 'Escoba Cerdas Duras', 'Escoba de paja plástica resistente', 'activo',
     (SELECT id FROM categoria WHERE nombre = 'Limpieza'),
     (SELECT id FROM unidad_medida WHERE simbolo = 'und')),
    ('MER-001', 'Caja de Huevos x30',  'Huevos de gallina libres, cubeta 30 und', 'activo',
     (SELECT id FROM categoria WHERE nombre = 'Mercancía general'),
     (SELECT id FROM unidad_medida WHERE simbolo = 'caja')),
    ('MER-002', 'Galletas Surtidas',   'Galletas dulces surtidas, paquete x24', 'activo',
     (SELECT id FROM categoria WHERE nombre = 'Mercancía general'),
     (SELECT id FROM unidad_medida WHERE simbolo = 'paq')),
    ('PAP-001', 'Resma Papel A4',      'Papel bond A4, resma 500 hojas', 'activo',
     (SELECT id FROM categoria WHERE nombre = 'Papelería'),
     (SELECT id FROM unidad_medida WHERE simbolo = 'und')),
    ('PAP-002', 'Bolígrafo Azul',      'Lapicero tinta azul, caja x12', 'activo',
     (SELECT id FROM categoria WHERE nombre = 'Papelería'),
     (SELECT id FROM unidad_medida WHERE simbolo = 'caja')),
    ('HIG-001', 'Jabón de Tocador',    'Jabón en barra 90 g, unidad', 'activo',
     (SELECT id FROM categoria WHERE nombre = 'Higiene personal'),
     (SELECT id FROM unidad_medida WHERE simbolo = 'und')),
    ('HIG-002', 'Papel Higiénico x4',  'Rollo doble hoja, paquete x4', 'activo',
     (SELECT id FROM categoria WHERE nombre = 'Higiene personal'),
     (SELECT id FROM unidad_medida WHERE simbolo = 'paq'));

-- ---------------------------------------------------------------------
-- EXISTENCIAS por sucursal
-- ---------------------------------------------------------------------
-- Bodega Central (stock saludable)
INSERT INTO existencia (producto_id, sucursal_id, cantidad_fisica, cantidad_reservada, stock_minimo, costo_promedio) VALUES
    ((SELECT id FROM producto WHERE sku = 'ALM-001'), (SELECT id FROM sucursal WHERE nombre = 'Bodega Central'), 120, 10, 20, 4.50),
    ((SELECT id FROM producto WHERE sku = 'ALM-002'), (SELECT id FROM sucursal WHERE nombre = 'Bodega Central'), 80,  5, 15, 2.10),
    ((SELECT id FROM producto WHERE sku = 'BEB-001'), (SELECT id FROM sucursal WHERE nombre = 'Bodega Central'), 200, 20, 30, 0.90),
    ((SELECT id FROM producto WHERE sku = 'LIM-001'), (SELECT id FROM sucursal WHERE nombre = 'Bodega Central'), 60,  0, 10, 8.20),
    ((SELECT id FROM producto WHERE sku = 'PAP-001'), (SELECT id FROM sucursal WHERE nombre = 'Bodega Central'), 45,  12, 8, 3.40);

-- Sucursal Norte (stock bajo en algunos ítems para ver alertas)
INSERT INTO existencia (producto_id, sucursal_id, cantidad_fisica, cantidad_reservada, stock_minimo, costo_promedio) VALUES
    ((SELECT id FROM producto WHERE sku = 'ALM-001'), (SELECT id FROM sucursal WHERE nombre = 'Sucursal Norte'), 15, 0, 20, 4.50),
    ((SELECT id FROM producto WHERE sku = 'BEB-002'), (SELECT id FROM sucursal WHERE nombre = 'Sucursal Norte'), 40, 0, 12, 3.80),
    ((SELECT id FROM producto WHERE sku = 'HIG-002'), (SELECT id FROM sucursal WHERE nombre = 'Sucursal Norte'), 25, 0, 5,  6.70),
    ((SELECT id FROM producto WHERE sku = 'LIM-002'), (SELECT id FROM sucursal WHERE nombre = 'Sucursal Norte'), 90, 0, 10, 2.60);

-- Sucursal Sur
INSERT INTO existencia (producto_id, sucursal_id, cantidad_fisica, cantidad_reservada, stock_minimo, costo_promedio) VALUES
    ((SELECT id FROM producto WHERE sku = 'ALM-003'), (SELECT id FROM sucursal WHERE nombre = 'Sucursal Sur'), 70, 0, 15, 7.90),
    ((SELECT id FROM producto WHERE sku = 'MER-001'), (SELECT id FROM sucursal WHERE nombre = 'Sucursal Sur'), 32, 0, 6,  9.50),
    ((SELECT id FROM producto WHERE sku = 'BEB-003'), (SELECT id FROM sucursal WHERE nombre = 'Sucursal Sur'), 55, 0, 10, 5.40),
    ((SELECT id FROM producto WHERE sku = 'PAP-002'), (SELECT id FROM sucursal WHERE nombre = 'Sucursal Sur'), 100, 0, 15, 1.20);

-- Sucursal Centro
INSERT INTO existencia (producto_id, sucursal_id, cantidad_fisica, cantidad_reservada, stock_minimo, costo_promedio) VALUES
    ((SELECT id FROM producto WHERE sku = 'MER-002'), (SELECT id FROM sucursal WHERE nombre = 'Sucursal Centro'), 48, 0, 10, 2.90),
    ((SELECT id FROM producto WHERE sku = 'HIG-001'), (SELECT id FROM sucursal WHERE nombre = 'Sucursal Centro'), 130, 0, 20, 1.75),
    ((SELECT id FROM producto WHERE sku = 'LIM-003'), (SELECT id FROM sucursal WHERE nombre = 'Sucursal Centro'), 22, 0, 5,  4.30),
    ((SELECT id FROM producto WHERE sku = 'ALM-002'), (SELECT id FROM sucursal WHERE nombre = 'Sucursal Centro'), 60, 0, 15, 2.10);

-- ---------------------------------------------------------------------
-- LISTA DE PRECIOS + PRECIOS
-- ---------------------------------------------------------------------
INSERT INTO lista_precio (nombre, sucursal_id)
SELECT 'Lista Público', (SELECT id FROM sucursal WHERE nombre = 'Bodega Central')
WHERE NOT EXISTS (SELECT 1 FROM lista_precio WHERE nombre = 'Lista Público');

INSERT INTO precio_producto (lista_precio_id, producto_id, precio) VALUES
    ((SELECT id FROM lista_precio WHERE nombre = 'Lista Público'),
     (SELECT id FROM producto WHERE sku = 'ALM-001'), 6.90),
    ((SELECT id FROM lista_precio WHERE nombre = 'Lista Público'),
     (SELECT id FROM producto WHERE sku = 'ALM-002'), 3.50),
    ((SELECT id FROM lista_precio WHERE nombre = 'Lista Público'),
     (SELECT id FROM producto WHERE sku = 'BEB-001'), 1.50),
    ((SELECT id FROM lista_precio WHERE nombre = 'Lista Público'),
     (SELECT id FROM producto WHERE sku = 'LIM-001'), 11.90),
    ((SELECT id FROM lista_precio WHERE nombre = 'Lista Público'),
     (SELECT id FROM producto WHERE sku = 'PAP-001'), 5.20);

-- ---------------------------------------------------------------------
-- MOVIMIENTOS DE INVENTARIO (ingreso/retiro/merma)
-- ---------------------------------------------------------------------
INSERT INTO movimiento_inventario (producto_id, sucursal_id, tipo, motivo, cantidad, usuario_id, fecha) VALUES
    ((SELECT id FROM producto WHERE sku = 'ALM-001'), (SELECT id FROM sucursal WHERE nombre = 'Bodega Central'),
     'ingreso', 'Compra inicial del proveedor', 100,
     (SELECT id FROM usuario WHERE email = 'cajero@bodega.com'), now() - interval '30 days'),
    ((SELECT id FROM producto WHERE sku = 'BEB-001'), (SELECT id FROM sucursal WHERE nombre = 'Bodega Central'),
     'ingreso', 'Reabastecimiento semanal', 150,
     (SELECT id FROM usuario WHERE email = 'cajero@bodega.com'), now() - interval '20 days'),
    ((SELECT id FROM producto WHERE sku = 'BEB-001'), (SELECT id FROM sucursal WHERE nombre = 'Bodega Central'),
     'retiro', 'Salida por venta', 20,
     (SELECT id FROM usuario WHERE email = 'cajero@bodega.com'), now() - interval '10 days'),
    ((SELECT id FROM producto WHERE sku = 'MER-001'), (SELECT id FROM sucursal WHERE nombre = 'Sucursal Sur'),
     'merma', 'Huevos dañados durante transporte', 3,
     (SELECT id FROM usuario WHERE email = 'operador.sur@bodega.com'), now() - interval '5 days'),
    ((SELECT id FROM producto WHERE sku = 'LIM-002'), (SELECT id FROM sucursal WHERE nombre = 'Sucursal Norte'),
     'retiro', 'Rotura de envase, retiro de stock', 5,
     (SELECT id FROM usuario WHERE email = 'operador.norte@bodega.com'), now() - interval '2 days');

-- ---------------------------------------------------------------------
-- PROVEEDORES
-- ---------------------------------------------------------------------
INSERT INTO proveedor (nombre, contacto, condiciones_generales) VALUES
    ('Distribuidora Andina S.A.C.', 'pedidos@andina.com', 'Pago a 30 días, entrega en 48h'),
    ('Alimentos del Valle', 'ventas@valle.com', 'Mínimo de compra S/ 500'),
    ('Limpieza Total EIRL', 'contacto@limpiezatotal.com', 'Descuento 5% en pedidos > S/ 1000');

-- ---------------------------------------------------------------------
-- ÓRDENES DE COMPRA
-- ---------------------------------------------------------------------
INSERT INTO orden_compra
    (codigo, proveedor_id, sucursal_destino_id, usuario_id, estado,
     fecha_emision, fecha_entrega_estimada, transportista, guia, condiciones_pago)
SELECT 'OC-2026-0001',
       (SELECT id FROM proveedor WHERE nombre = 'Distribuidora Andina S.A.C.'),
       (SELECT id FROM sucursal WHERE nombre = 'Bodega Central'),
       (SELECT id FROM usuario WHERE email = 'cajero@bodega.com'),
       'RECIBIDA',
       now() - interval '25 days', (now() - interval '23 days')::date,
       'Rutas del Norte', 'G-1001', 'Crédito 30 días'
WHERE NOT EXISTS (SELECT 1 FROM orden_compra WHERE codigo = 'OC-2026-0001');

INSERT INTO orden_compra_linea (orden_compra_id, producto_id, cantidad_ordenada, cantidad_recibida, precio_unitario, descuento)
SELECT (SELECT id FROM orden_compra WHERE codigo = 'OC-2026-0001'),
       (SELECT id FROM producto WHERE sku = 'ALM-001'), 60, 60, 4.50, 0;

INSERT INTO orden_compra_linea (orden_compra_id, producto_id, cantidad_ordenada, cantidad_recibida, precio_unitario, descuento)
SELECT (SELECT id FROM orden_compra WHERE codigo = 'OC-2026-0001'),
       (SELECT id FROM producto WHERE sku = 'BEB-001'), 150, 150, 0.90, 0;

INSERT INTO orden_compra
    (codigo, proveedor_id, sucursal_destino_id, usuario_id, estado,
     fecha_emision, fecha_entrega_estimada, transportista, guia, condiciones_pago)
SELECT 'OC-2026-0002',
       (SELECT id FROM proveedor WHERE nombre = 'Alimentos del Valle'),
       (SELECT id FROM sucursal WHERE nombre = 'Sucursal Sur'),
       (SELECT id FROM usuario WHERE email = 'operador.sur@bodega.com'),
       'EN_TRANSITO',
       now() - interval '3 days', (now() + interval '2 days')::date,
       'Logística Sur', 'G-2002', 'Contado'
WHERE NOT EXISTS (SELECT 1 FROM orden_compra WHERE codigo = 'OC-2026-0002');

INSERT INTO orden_compra_linea (orden_compra_id, producto_id, cantidad_ordenada, cantidad_recibida, precio_unitario, descuento)
SELECT (SELECT id FROM orden_compra WHERE codigo = 'OC-2026-0002'),
       (SELECT id FROM producto WHERE sku = 'ALM-003'), 40, 0, 7.90, 0;

-- ---------------------------------------------------------------------
-- TRANSFERENCIAS (modelo multi-item, V15)
-- ---------------------------------------------------------------------
INSERT INTO transferencia
    (codigo, sucursal_origen_id, sucursal_destino_id, usuario_solicitante_id,
     urgencia, estado, transportista, guia, fecha_estimada_llegada,
     fecha_solicitud, fecha_despacho, fecha_recepcion)
SELECT 'TR-2026-0001',
       (SELECT id FROM sucursal WHERE nombre = 'Bodega Central'),
       (SELECT id FROM sucursal WHERE nombre = 'Sucursal Norte'),
       (SELECT id FROM usuario WHERE email = 'operador.norte@bodega.com'),
       'ALTA', 'RECIBIDA', 'Courier Express', 'TR-G1',
       now() - interval '4 days', now() - interval '10 days',
       now() - interval '6 days', now() - interval '4 days'
WHERE NOT EXISTS (SELECT 1 FROM transferencia WHERE codigo = 'TR-2026-0001');

INSERT INTO transferencia_linea (transferencia_id, producto_id, cantidad_solicitada, cantidad_despachada, cantidad_recibida)
SELECT (SELECT id FROM transferencia WHERE codigo = 'TR-2026-0001'),
       (SELECT id FROM producto WHERE sku = 'ALM-001'), 30, 30, 30;

INSERT INTO transferencia_linea (transferencia_id, producto_id, cantidad_solicitada, cantidad_despachada, cantidad_recibida)
SELECT (SELECT id FROM transferencia WHERE codigo = 'TR-2026-0001'),
       (SELECT id FROM producto WHERE sku = 'LIM-001'), 15, 15, 15;

INSERT INTO transferencia_aprobacion (transferencia_id, gerente_id, rol_aprobacion, decision, observacion)
SELECT (SELECT id FROM transferencia WHERE codigo = 'TR-2026-0001'),
       (SELECT id FROM usuario WHERE email = 'gerente@bodega.com'),
       'ORIGEN', 'APROBADO', 'Origen con stock disponible';

INSERT INTO transferencia_aprobacion (transferencia_id, gerente_id, rol_aprobacion, decision, observacion)
SELECT (SELECT id FROM transferencia WHERE codigo = 'TR-2026-0001'),
       (SELECT id FROM usuario WHERE email = 'gerente@bodega.com'),
       'DESTINO', 'APROBADO', 'Destino necesita reposición urgente';

INSERT INTO reserva_stock (transferencia_linea_id, producto_id, sucursal_id, cantidad, estado, fecha_liberacion)
SELECT tl.id, tl.producto_id,
       (SELECT id FROM sucursal WHERE nombre = 'Bodega Central'),
       tl.cantidad_despachada, 'CONSUMIDA', now() - interval '4 days'
FROM transferencia_linea tl
WHERE tl.transferencia_id = (SELECT id FROM transferencia WHERE codigo = 'TR-2026-0001');

-- Segunda transferencia (aprobada, lista para preparar/despachar)
INSERT INTO transferencia
    (codigo, sucursal_origen_id, sucursal_destino_id, usuario_solicitante_id,
     urgencia, estado, fecha_solicitud, fecha_estimada_llegada)
SELECT 'TR-2026-0002',
       (SELECT id FROM sucursal WHERE nombre = 'Bodega Central'),
       (SELECT id FROM sucursal WHERE nombre = 'Sucursal Centro'),
       (SELECT id FROM usuario WHERE email = 'operador.centro@bodega.com'),
       'NORMAL', 'APROBADA', now() - interval '3 days', now() + interval '2 days'
WHERE NOT EXISTS (SELECT 1 FROM transferencia WHERE codigo = 'TR-2026-0002');

INSERT INTO transferencia_linea (transferencia_id, producto_id, cantidad_solicitada, cantidad_despachada)
SELECT (SELECT id FROM transferencia WHERE codigo = 'TR-2026-0002'),
       (SELECT id FROM producto WHERE sku = 'PAP-001'), 12, 12;

INSERT INTO transferencia_aprobacion (transferencia_id, gerente_id, rol_aprobacion, decision)
SELECT (SELECT id FROM transferencia WHERE codigo = 'TR-2026-0002'),
       (SELECT id FROM usuario WHERE email = 'gerente@bodega.com'), 'ORIGEN', 'APROBADO';

INSERT INTO transferencia_aprobacion (transferencia_id, gerente_id, rol_aprobacion, decision)
SELECT (SELECT id FROM transferencia WHERE codigo = 'TR-2026-0002'),
       (SELECT id FROM usuario WHERE email = 'gerente@bodega.com'), 'DESTINO', 'APROBADO';

INSERT INTO reserva_stock (transferencia_linea_id, producto_id, sucursal_id, cantidad, estado)
SELECT tl.id, tl.producto_id,
       (SELECT id FROM sucursal WHERE nombre = 'Bodega Central'),
       tl.cantidad_despachada, 'ACTIVA'
FROM transferencia_linea tl
WHERE tl.transferencia_id = (SELECT id FROM transferencia WHERE codigo = 'TR-2026-0002');

-- Tercera transferencia: solicitada (aún sin aprobar) para evaluar flujo de agresión
INSERT INTO transferencia
    (codigo, sucursal_origen_id, sucursal_destino_id, usuario_solicitante_id,
     urgencia, estado, fecha_solicitud, fecha_estimada_llegada)
SELECT 'TR-2026-0003',
       (SELECT id FROM sucursal WHERE nombre = 'Sucursal Sur'),
       (SELECT id FROM sucursal WHERE nombre = 'Sucursal Norte'),
       (SELECT id FROM usuario WHERE email = 'operador.norte@bodega.com'),
       'CRITICA', 'SOLICITADA', now() - interval '1 day', now() + interval '3 days'
WHERE NOT EXISTS (SELECT 1 FROM transferencia WHERE codigo = 'TR-2026-0003');

INSERT INTO transferencia_linea (transferencia_id, producto_id, cantidad_solicitada)
SELECT (SELECT id FROM transferencia WHERE codigo = 'TR-2026-0003'),
       (SELECT id FROM producto WHERE sku = 'HIG-002'), 20;

INSERT INTO transferencia_linea (transferencia_id, producto_id, cantidad_solicitada)
SELECT (SELECT id FROM transferencia WHERE codigo = 'TR-2026-0003'),
       (SELECT id FROM producto WHERE sku = 'BEB-003'), 15;

-- ---------------------------------------------------------------------
-- VENTAS
-- ---------------------------------------------------------------------
INSERT INTO venta (codigo, sucursal_id, usuario_id, descuento_porcentaje, subtotal, total, unidades, fecha)
SELECT 'VT-2026-0001',
       (SELECT id FROM sucursal WHERE nombre = 'Bodega Central'),
       (SELECT id FROM usuario WHERE email = 'cajero@bodega.com'),
       0, 21.20, 21.20, 8, now() - interval '7 days'
WHERE NOT EXISTS (SELECT 1 FROM venta WHERE codigo = 'VT-2026-0001');

INSERT INTO venta_linea (venta_id, producto_id, cantidad, precio_unitario, descuento, subtotal)
SELECT (SELECT id FROM venta WHERE codigo = 'VT-2026-0001'),
       (SELECT id FROM producto WHERE sku = 'BEB-001'), 6, 1.50, 0, 9.00;

INSERT INTO venta_linea (venta_id, producto_id, cantidad, precio_unitario, descuento, subtotal)
SELECT (SELECT id FROM venta WHERE codigo = 'VT-2026-0001'),
       (SELECT id FROM producto WHERE sku = 'ALM-001'), 2, 6.10, 0, 12.20;

INSERT INTO venta (codigo, sucursal_id, usuario_id, descuento_porcentaje, subtotal, total, unidades, fecha)
SELECT 'VT-2026-0002',
       (SELECT id FROM sucursal WHERE nombre = 'Sucursal Centro'),
       (SELECT id FROM usuario WHERE email = 'operador.centro@bodega.com'),
       10, 30.00, 27.00, 12, now() - interval '2 days'
WHERE NOT EXISTS (SELECT 1 FROM venta WHERE codigo = 'VT-2026-0002');

INSERT INTO venta_linea (venta_id, producto_id, cantidad, precio_unitario, descuento, subtotal)
SELECT (SELECT id FROM venta WHERE codigo = 'VT-2026-0002'),
       (SELECT id FROM producto WHERE sku = 'HIG-001'), 12, 2.50, 3.00, 27.00;

INSERT INTO venta (codigo, sucursal_id, usuario_id, descuento_porcentaje, subtotal, total, unidades, fecha)
SELECT 'VT-2026-0003',
       (SELECT id FROM sucursal WHERE nombre = 'Sucursal Norte'),
       (SELECT id FROM usuario WHERE email = 'operador.norte@bodega.com'),
       0, 18.60, 18.60, 5, now() - interval '1 day'
WHERE NOT EXISTS (SELECT 1 FROM venta WHERE codigo = 'VT-2026-0003');

INSERT INTO venta_linea (venta_id, producto_id, cantidad, precio_unitario, descuento, subtotal)
SELECT (SELECT id FROM venta WHERE codigo = 'VT-2026-0003'),
       (SELECT id FROM producto WHERE sku = 'HIG-002'), 2, 6.60, 0, 13.20;

INSERT INTO venta_linea (venta_id, producto_id, cantidad, precio_unitario, descuento, subtotal)
SELECT (SELECT id FROM venta WHERE codigo = 'VT-2026-0003'),
       (SELECT id FROM producto WHERE sku = 'BEB-002'), 3, 1.80, 0, 5.40;
