-- 1. Forzar la limpieza de registros previos para evitar conflictos de llaves primarias duplicadas
DELETE FROM productos;

-- 2. Inserciones actualizadas con la columna mandatoria sucursal_id
INSERT INTO productos (id, sku, nombre, descripcion, precio, costo, categoria_id, sucursal_id, activo)
VALUES (10, 'LAP-MAC-01', 'MacBook Pro 14', 'Chip M3, 16GB RAM, 512GB SSD', 1999.99, 1500.00, 1, 7, true)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO productos (id, sku, nombre, precio, costo, descripcion, categoria_id, sucursal_id, activo)
VALUES (11, 'LAP-DEL-02', 'Dell XPS 13', 1499.50, 1100.00, 'Pantalla 4K, i7 13th Gen', 1, 7, true)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO productos (id, sku, nombre, precio, costo, descripcion, categoria_id, sucursal_id, activo)
--> Cambié este ID a 12 para que coincida con tu script inicial y no choque con la Dell
VALUES (12, 'PHO-SAM-01', 'Samsung Galaxy S24', 999.00, 750.00, 'IA integrada, Cámara 50MP', 2, 7, true)
    ON CONFLICT (id) DO NOTHING;

-- 3. Sincronizar el secuenciador automático de PostgreSQL para evitar errores en futuros POSTs manuales
SELECT setval('productos_id_seq', (SELECT MAX(id) FROM productos));