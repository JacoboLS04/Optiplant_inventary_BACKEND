INSERT INTO usuario (email, password, nombre, rol, sucursal_id)
SELECT 'admin@optiplant.com',
       '$2a$10$ma82Y4MH7qYyo/Q1YsY1L.MzaOMsPQYRpAIFaFCHFiZGeEFuHcZyG',
       'Administrador Inicial',
       'ADMINISTRADOR',
       NULL
WHERE NOT EXISTS (
    SELECT 1 FROM usuario WHERE email = 'admin@optiplant.com'
);
