-- Insertar empresa de prueba
INSERT INTO empresas (nit, nombre, correo, deleted) 
VALUES ('900123456', 'Empresa Test', 'empresa@test.com', false)
ON CONFLICT (nit) DO NOTHING;

-- Insertar empleado de prueba
INSERT INTO empleados (nit, nombre, tipo_documento, numero_documento, deleted) 
VALUES ('900123456', 'Admin Test', 'CC', '1234567890', false)

-- Insertar credencial de prueba
INSERT INTO credenciales (correo, contrasena) 
VALUES ('admin@test.com', 'password123')
