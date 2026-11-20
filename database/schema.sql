-- ============================================================
-- MegaPizza - Esquema de base de datos (MySQL / MariaDB)
-- Bloque 1: modelo físico unificado con prefijo tb_ según AGENTS.md
-- y AvanceProy2_Grupo4.pdf.
--
-- Ejecutar: mysql -u root -p < database/schema.sql
-- ADVERTENCIA: este script recrea las tablas (borra datos previos).
-- ============================================================

CREATE DATABASE IF NOT EXISTS BaseDatosMegapizza
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE BaseDatosMegapizza;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS tb_detalle_pedido;
DROP TABLE IF EXISTS tb_receta_insumo;
DROP TABLE IF EXISTS tb_pedido;
DROP TABLE IF EXISTS tb_cliente;
DROP TABLE IF EXISTS tb_producto;
DROP TABLE IF EXISTS tb_inventario;
DROP TABLE IF EXISTS tb_usuario;
SET FOREIGN_KEY_CHECKS = 1;

-- RF02 / RNF01: autenticación y autorización por rol.
CREATE TABLE tb_usuario (
    id_usuario    INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50) NOT NULL UNIQUE,
    password_hash CHAR(64) NOT NULL COMMENT 'SHA-256(megapizza:password)',
    rol           ENUM('CLIENTE', 'CAJERO', 'COCINERO', 'ALMACEN', 'ADMINISTRADOR') NOT NULL,
    estado        ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO'
) ENGINE = InnoDB;

-- Datos del cliente; un cliente puede tener una cuenta de usuario.
CREATE TABLE tb_cliente (
    id_cliente  INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario  INT UNIQUE,
    nombre      VARCHAR(100) NOT NULL,
    telefono    VARCHAR(15),
    direccion   VARCHAR(255),
    correo      VARCHAR(100),
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario)
        REFERENCES tb_usuario (id_usuario)
) ENGINE = InnoDB;

-- RF01: carta digital. La categoría es una de las cuatro del dominio.
CREATE TABLE tb_producto (
    id_producto  INT AUTO_INCREMENT PRIMARY KEY,
    categoria    ENUM('Pizza', 'Pasta', 'Bebida', 'Combo') NOT NULL,
    nombre       VARCHAR(100) NOT NULL,
    descripcion  TEXT,
    precio_base  DECIMAL(10, 2) NOT NULL,
    imagen_url   VARCHAR(255),
    CONSTRAINT chk_producto_precio CHECK (precio_base >= 0)
) ENGINE = InnoDB;

-- RF08/RF09/RF11/RF12: insumos del almacén y stock mínimo de alerta.
CREATE TABLE tb_inventario (
    id_insumo     INT AUTO_INCREMENT PRIMARY KEY,
    nombre_insumo VARCHAR(100) NOT NULL,
    stock_actual  DECIMAL(10, 2) NOT NULL DEFAULT 0,
    stock_minimo  DECIMAL(10, 2) NOT NULL DEFAULT 0,
    unidad_medida VARCHAR(20) NOT NULL,
    CONSTRAINT chk_inventario_stock CHECK (stock_actual >= 0)
) ENGINE = InnoDB;

-- CU04/CU05/RF04/RF05: cabecera del pedido y comprobante de pago digital.
CREATE TABLE tb_pedido (
    id_pedido       INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente      INT,
    fecha_hora      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_entrega    ENUM('Delivery', 'Recojo') NOT NULL,
    metodo_pago     ENUM('Yape', 'Plin', 'Tarjeta') NOT NULL,
    comprobante_url VARCHAR(255),
    estado_pedido   ENUM('Pendiente', 'Confirmado', 'En Preparacion', 'Listo', 'Entregado', 'Rechazado')
                    NOT NULL DEFAULT 'Pendiente',
    monto_total     DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente)
        REFERENCES tb_cliente (id_cliente)
) ENGINE = InnoDB;

-- Líneas de venta: precio y subtotal quedan congelados al registrar el pedido.
CREATE TABLE tb_detalle_pedido (
    id_detalle      INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido       INT NOT NULL,
    id_producto     INT NOT NULL,
    cantidad        INT NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    subtotal        DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido)
        REFERENCES tb_pedido (id_pedido),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto)
        REFERENCES tb_producto (id_producto),
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0)
) ENGINE = InnoDB;

-- RF10/RNF02:receta que vincula cada producto con sus insumos.
-- La restricción UNIQUE impide recetas duplicadas y, por tanto, doble descuento.
CREATE TABLE tb_receta_insumo (
    id_receta          INT AUTO_INCREMENT PRIMARY KEY,
    id_producto        INT NOT NULL,
    id_insumo          INT NOT NULL,
    cantidad_necesaria DECIMAL(10, 2) NOT NULL,
    CONSTRAINT uq_receta_producto_insumo UNIQUE (id_producto, id_insumo),
    CONSTRAINT fk_receta_producto FOREIGN KEY (id_producto)
        REFERENCES tb_producto (id_producto),
    CONSTRAINT fk_receta_insumo FOREIGN KEY (id_insumo)
        REFERENCES tb_inventario (id_insumo),
    CONSTRAINT chk_receta_cantidad CHECK (cantidad_necesaria > 0)
) ENGINE = InnoDB;

CREATE INDEX idx_pedido_estado ON tb_pedido (estado_pedido);
CREATE INDEX idx_pedido_cliente ON tb_pedido (id_cliente);
CREATE INDEX idx_detalle_pedido ON tb_detalle_pedido (id_pedido);
CREATE INDEX idx_receta_producto ON tb_receta_insumo (id_producto);
