# AGENTS.md — Proyecto Sistema Web Megapizza

## 1. Contexto del Proyecto
Sistema de información web para la gestión de ventas en línea (delivery y recojo en tienda) y control de inventario en tiempo real para la pizzería **Megapizza**.
Toda la implementación debe auditarse y alinearse estrictamente con los artefactos de diseño ubicados en `docs/AvanceProy2_Grupo4.pdf` y `docs/ParticipaciónEnClase6_Grupo4.pdf`, eliminando cualquier archivo del borrador inicial que no corresponda a estas especificaciones.

## 2. Stack Tecnológico Obligatorio
- **Lenguaje Backend:** Java 17+ (Jakarta EE / Javax Servlets y JSP). **NO** usar Spring Boot, Hibernate ni JPA.
- **Servidor de Aplicaciones:** Apache Tomcat 10+ (empaquetado `.war`).
- **Base de Datos y Persistencia:** MySQL / PostgreSQL mediante **JDBC puro** (`ConexionBD.java` con `PreparedStatement`).
- **Frontend:** JSP, HTML5, CSS3 (`static/css/estilos.css`), JavaScript vanilla (`static/js/carrito.js`) con peticiones `fetch`/AJAX.
- **Gestor de Dependencias:** Maven (`pom.xml`).

## 3. Arquitectura en 3 Capas (`com.megapizza`)
Respeta estrictamente la separación de responsabilidades:
1. **Capa de Presentación (`src/main/webapp/` y `controller/`):**
   - Vistas JSP: `index.jsp` (redirige a `/catalogo`), `login.jsp`, `catalogo.jsp`, `vistaCarrito.jsp`, `misPedidos.jsp`, `vistaCaja.jsp`, `vistaCocina.jsp`, `vistaAlmacen.jsp`, `error.jsp`.
   - Servlets/Controladores: `AuthController`, `CatalogoController`, `PedidoController`, `CajaController`, `CocinaController`, `AlmacenController`.
2. **Capa de Lógica de Negocio (`service/`):**
   - `PedidoService`: Orquesta validación de carrito, cálculo de subtotales/totales, creación de pedido en estado `"Pendiente"` y transición a `"Confirmado"`.
   - `InventarioService`: Verifica stock disponible antes de vender, descuenta insumos automáticamente según `RecetaInsumo` al confirmar el pago (RF10), registra mermas (RF11) y genera alertas de stock mínimo (RF12).
3. **Capa de Acceso a Datos (`dao/` y `model/`):**
   - Modelos (POJOs): `Usuario`, `Cliente`, `Pedido`, `DetallePedido`, `Producto`, `Inventario`, `RecetaInsumo`.
   - DAOs: `UsuarioDAO`, `ClienteDAO`, `PedidoDAO`, `ProductoDAO`, `InventarioDAO`.

## 4. Esquema de Base de Datos (`database/schema.sql`)
Unificar nombres de tablas con prefijo `tb_`:
- `tb_usuario` (`id_usuario`, `username`, `password_hash`, `rol` [CLIENTE, CAJERO, COCINERO, ALMACEN, ADMINISTRADOR], `estado`)
- `tb_cliente` (`id_cliente`, `id_usuario`, `nombre`, `telefono`, `direccion`, `correo`)
- `tb_producto` (`id_producto`, `categoria` [Pizza, Pasta, Bebida, Combo], `nombre`, `descripcion`, `precio_base`, `imagen_url`)
- `tb_pedido` (`id_pedido`, `id_cliente`, `fecha_hora`, `tipo_entrega` [Delivery, Recojo], `metodo_pago` [Yape, Plin, Tarjeta], `comprobante_url`, `estado_pedido` [Pendiente, Confirmado, En Preparacion, Listo, Entregado, Rechazado], `monto_total`)
- `tb_detalle_pedido` (`id_detalle`, `id_pedido`, `id_producto`, `cantidad`, `precio_unitario`, `subtotal`)
- `tb_inventario` (`id_insumo`, `nombre_insumo`, `stock_actual`, `stock_minimo`, `unidad_medida`)
- `tb_receta_insumo` (`id_receta`, `id_producto`, `id_insumo`, `cantidad_necesaria`)

## 5. Reglas Críticas de Negocio y Seguridad
- **Control Transaccional (RNF02):** Al confirmar un pedido en `PedidoService` / `PedidoDAO`, desactivar auto-commit (`conn.setAutoCommit(false)`). Insertar cabecera, detalles y descontar stock en `tb_inventario` cruzando con `tb_receta_insumo` dentro de una sola transacción atomizada (`commit()` / `rollback()`). Evitar doble descuento de stock.
- **Seguridad por Roles (RNF01):** `AuthFilter.java` debe proteger las rutas `/caja/*`, `/cocina/*`, `/almacen/*` y `/admin/*` validando el rol en `HttpSession`.
- **Flujo CU04 & CU05:** El cliente registra el pedido en estado `"Pendiente"` adjuntando pago digital (Yape/Plin/Tarjeta). El cajero valida en `vistaCaja.jsp` y cambia el estado a `"Confirmado"`, disparando el descuento de insumos y enviando la comanda a `vistaCocina.jsp`.