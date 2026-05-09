INSERT INTO productos (id, sku, nombre, descripcion, precio, costo, categoria_id, activo)
VALUES (10, 'LAP-MAC-01', 'MacBook Pro 14', 'Chip M3, 16GB RAM, 512GB SSD', 1999.99, 1500.00, 1, true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO productos (id, sku, nombre, precio, costo, descripcion, categoria_id, activo)
VALUES (11, 'LAP-DEL-02', 'Dell XPS 13', 1499.50, 1100.00, 'Pantalla 4K, i7 13th Gen', 1, true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO productos (id, sku, nombre, precio, costo, descripcion, categoria_id, activo)
VALUES (12, 'PHO-SAM-01', 'Samsung Galaxy S24', 999.00, 750.00, 'IA integrada, Cámara 50MP', 2, true)
ON CONFLICT (id) DO NOTHING;

-- Si tu base de datos usa una secuencia para los IDs (IDENTITY),
-- es buena práctica reiniciarla para que no intente usar el ID 10 en tu próximo insert manual
SELECT setval('productos_id_seq', (SELECT MAX(id) FROM productos));