-- ============================================================
-- MegaPizza - Datos de prueba (Bloque 1)
-- Ejecutar después de schema.sql:
--   mysql -u root -p BaseDatosMegapizza < database/data.sql
--
-- Contraseñas: SHA-256("megapizza:" + password) = SHA-256("megapizza:123456")
-- Hash resultante: e68128236c61c226df42957cddc131403fe5346ad584bdd9ece20edf28c178e4
--
-- Usuarios de prueba (contraseña 123456 para cada rol):
--   ADMINISTRADOR: admin / 123456
--   CAJERO:        cajero / 123456
--   COCINERO:      cocina / 123456
--   ALMACEN:       almacen / 123456
--   CLIENTE:       cliente / 123456
-- ============================================================

USE BaseDatosMegapizza;

INSERT INTO tb_usuario (id_usuario, username, password_hash, rol, estado) VALUES
 (1, 'admin',   'e68128236c61c226df42957cddc131403fe5346ad584bdd9ece20edf28c178e4', 'ADMINISTRADOR', 'ACTIVO'),
 (2, 'cajero',  'e68128236c61c226df42957cddc131403fe5346ad584bdd9ece20edf28c178e4', 'CAJERO',        'ACTIVO'),
 (3, 'cocina',  'e68128236c61c226df42957cddc131403fe5346ad584bdd9ece20edf28c178e4', 'COCINERO',      'ACTIVO'),
 (4, 'almacen', 'e68128236c61c226df42957cddc131403fe5346ad584bdd9ece20edf28c178e4', 'ALMACEN',       'ACTIVO'),
 (5, 'cliente', 'e68128236c61c226df42957cddc131403fe5346ad584bdd9ece20edf28c178e4', 'CLIENTE',       'ACTIVO');

INSERT INTO tb_cliente (id_cliente, id_usuario, nombre, telefono, direccion, correo) VALUES
 (1, 5, 'Cliente Demo', '987654321', 'Av. Central 123, Limón', 'cliente@megapizza.test'),
 (2, NULL, 'María Pérez', '912345678', 'Calle 8 #45, Limón', 'maria@example.test');

-- Carta digital RF01: pizzas, pastas, bebidas y combos.
INSERT INTO tb_producto (id_producto, categoria, nombre, descripcion, precio_base, imagen_url) VALUES
 (1,  'Pizza',  'Pizza Margarita',          'Salsa de tomate, mozzarella y albahaca',        32.00, '/megapizza/static/img/pizza_americana.jpg'),
 (2,  'Pizza',  'Pizza Pepperoni',          'Mozzarella y pepperoni recién horneado',        38.00, '/megapizza/static/img/pizza_pepperoni.png'),
 (3,  'Pizza',  'Pizza Jamón y Champiñones', 'Jamón, champiñones y queso fundido',            39.00, NULL),
 (4,  'Pizza',  'Pizza Cuatro Quesos',      'Mozzarella, parmesano, crema y queso fundido',  42.00, NULL),
 (5,  'Pizza',  'Pizza Vegetariana',        'Champiñones, aceitunas y mozzarella',           37.00, NULL),
 (6,  'Pasta',  'Pasta Alfredo',            'Pasta con crema, queso y champiñones',          28.00, NULL),
 (7,  'Pasta',  'Pasta a la Boloñesa',      'Pasta con salsa de tomate y jamón',             30.00, NULL),
 (8,  'Bebida', 'Gaseosa 1.5 L',            'Bebida gaseosa grande',                          9.00, NULL),
 (9,  'Bebida', 'Limonada Natural',         'Limón recién exprimido',                         8.00, NULL),
 (10, 'Combo',  'Combo Familiar',           'Dos pizzas grandes y una gaseosa de 1.5 L',     95.00, NULL),
 (11, 'Combo',  'Combo Pareja',             'Una pizza, una pasta y una gaseosa de 1.5 L',   68.00, NULL),
 (12, 'Combo',  'Combo Individual',         'Una pizza personal y una limonada natural',     45.00, NULL);

-- Insumos del almacén. Las aceitunas inician bajo su mínimo para probar RF12.
INSERT INTO tb_inventario (id_insumo, nombre_insumo, stock_actual, stock_minimo, unidad_medida) VALUES
 (1,  'Masa de pizza',    30.00, 8.00, 'kg'),
 (2,  'Salsa de tomate',  20.00, 5.00, 'kg'),
 (3,  'Queso mozzarella', 15.00, 6.00, 'kg'),
 (4,  'Pepperoni',         8.00, 2.00, 'kg'),
 (5,  'Jamón',             7.00, 2.00, 'kg'),
 (6,  'Champiñones',       6.00, 2.00, 'kg'),
 (7,  'Aceitunas',         1.00, 1.50, 'kg'),
 (8,  'Crema de leche',    8.00, 2.00, 'kg'),
 (9,  'Pasta',            12.00, 4.00, 'kg'),
 (10, 'Gaseosa 1.5 L',    36.00, 12.00, 'unid'),
 (11, 'Limones',          10.00, 2.00, 'kg'),
 (12, 'Queso parmesano',   5.00, 2.00, 'kg'),
 (13, 'Aceite de oliva',   4.00, 1.50, 'kg'),
 (14, 'Caja combo',       20.00, 5.00, 'unid');

-- Recetas: cantidad_necesaria por unidad de producto (RF10 / RNF02).
INSERT INTO tb_receta_insumo (id_receta, id_producto, id_insumo, cantidad_necesaria) VALUES
 (1,  1, 1, 0.35), (2,  1, 2, 0.20), (3,  1, 3, 0.25),
 (4,  2, 1, 0.35), (5,  2, 2, 0.20), (6,  2, 3, 0.25), (7,  2, 4, 0.15),
 (8,  3, 1, 0.35), (9,  3, 2, 0.20), (10, 3, 3, 0.25), (11, 3, 5, 0.12), (12, 3, 6, 0.12),
 (13, 4, 1, 0.35), (14, 4, 2, 0.15), (15, 4, 3, 0.20), (16, 4, 12, 0.12), (17, 4, 8, 0.08),
 (18, 5, 1, 0.35), (19, 5, 2, 0.20), (20, 5, 3, 0.20), (21, 5, 6, 0.15), (22, 5, 7, 0.08),
 (23, 6, 9, 0.25), (24, 6, 8, 0.18), (25, 6, 3, 0.12), (26, 6, 6, 0.10),
 (27, 7, 9, 0.25), (28, 7, 2, 0.20), (29, 7, 5, 0.10),
 (30, 8, 10, 1.00),
 (31, 9, 11, 0.20),
 (32, 10, 1, 0.70), (33, 10, 2, 0.40), (34, 10, 3, 0.50),
 (35, 10, 4, 0.15), (36, 10, 10, 1.00), (37, 10, 14, 1.00),
 (38, 11, 1, 0.35), (39, 11, 2, 0.20), (40, 11, 3, 0.25),
 (41, 11, 9, 0.20), (42, 11, 8, 0.15), (43, 11, 10, 1.00), (44, 11, 14, 1.00),
 (45, 12, 1, 0.35), (46, 12, 2, 0.20), (47, 12, 3, 0.25),
 (48, 12, 11, 0.15), (49, 12, 14, 1.00);

-- Pedidos de ejemplo con los estados del ciclo de vida documentado.
INSERT INTO tb_pedido (id_pedido, id_cliente, fecha_hora, tipo_entrega, metodo_pago, comprobante_url, estado_pedido, monto_total) VALUES
 (1, 1, '2026-11-18 20:15:00', 'Delivery', 'Yape',    'comprobantes/yape-001.png',    'Entregado',  70.00),
 (2, 1, '2026-11-19 21:05:00', 'Delivery', 'Plin',    'comprobantes/plin-002.png',    'Pendiente',  48.00),
 (3, 2, '2026-11-20 12:30:00', 'Recojo',   'Tarjeta', 'comprobantes/tarjeta-003.png', 'Confirmado', 95.00),
 (4, 2, '2026-11-20 13:10:00', 'Recojo',   'Yape',    'comprobantes/yape-004.png',    'Rechazado',   8.00);

INSERT INTO tb_detalle_pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES
 (1, 1, 1,  1, 32.00, 32.00),
 (2, 1, 2,  1, 38.00, 38.00),
 (3, 2, 3,  1, 39.00, 39.00),
 (4, 2, 8,  1,  9.00,  9.00),
 (5, 3, 10, 1, 95.00, 95.00),
 (6, 4, 9,  1,  8.00,  8.00);

-- Reinicia los AUTO_INCREMENT para que los nuevos registros continúen los ejemplos.
ALTER TABLE tb_usuario        AUTO_INCREMENT = 6;
ALTER TABLE tb_cliente        AUTO_INCREMENT = 3;
ALTER TABLE tb_producto       AUTO_INCREMENT = 13;
ALTER TABLE tb_inventario     AUTO_INCREMENT = 15;
ALTER TABLE tb_receta_insumo  AUTO_INCREMENT = 50;
ALTER TABLE tb_pedido         AUTO_INCREMENT = 5;
ALTER TABLE tb_detalle_pedido AUTO_INCREMENT = 7;
