-- =====================================================================
-- V19: Corrige la contraseña de los usuarios de prueba creados en V18.
-- El hash original apuntaba a "admin123"; este hash es válido para la
-- contraseña documentada "Prueba123!".
-- =====================================================================
UPDATE usuario
SET password = '$2a$10$qMmk3hiRo5agLeXeynT1Uei0.xP76lycgs80T6dEJ4Xg5eYzVIUp.'
WHERE email IN (
    'gerente@bodega.com',
    'operador.norte@bodega.com',
    'operador.sur@bodega.com',
    'operador.centro@bodega.com',
    'cajero@bodega.com'
);