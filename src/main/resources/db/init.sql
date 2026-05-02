-- Insertar empresa de prueba
INSERT INTO empresas (nit, nombre, correo, deleted) 
VALUES ('900123456', 'Empresa Test', 'empresa@test.com', false)
ON CONFLICT (nit) DO NOTHING;

-- Insertar empleado de prueba
INSERT INTO empleados (id, nit, nombre, tipo_documento, numero_documento, deleted) 
VALUES (1, '900123456', 'Admin Test', 'CC', '1234567890', false)
ON CONFLICT (id) DO NOTHING;

-- Insertar credencial de prueba
INSERT INTO credenciales (id, correo, contrasena) 
VALUES (1, 'admin@test.com', 'password123')
ON CONFLICT (id) DO NOTHING;
