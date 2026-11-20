package com.megapizza.service;

import com.megapizza.config.ConexionBD;
import com.megapizza.dao.PedidoDAO;
import com.megapizza.dao.ProductoDAO;
import com.megapizza.exception.ServiceException;
import com.megapizza.model.DetallePedido;
import com.megapizza.model.Pedido;
import com.megapizza.model.Producto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Orquesta el registro y el ciclo de vida de los pedidos (CU04, CU05, RF10, RNF02).
 *
 * El registro crea la cabecera y los detalles en estado {@code Pendiente} dentro
 * de una transacción. La confirmación realizada por caja cambia el estado a
 * {@code Confirmado} y descuenta los insumos de {@code tb_receta_insumo} en la
 * MISMA transacción, de modo que no puede haber doble descuento ni stock
 * descontado sin pedido confirmado.
 */
public class PedidoService {

    private static final Map<String, List<String>> TRANSICIONES = Map.of(
            Pedido.PENDIENTE, List.of(Pedido.RECHAZADO),
            Pedido.CONFIRMADO, List.of(Pedido.EN_PREPARACION),
            Pedido.EN_PREPARACION, List.of(Pedido.LISTO),
            Pedido.LISTO, List.of(Pedido.ENTREGADO));

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final InventarioService inventarioService = new InventarioService();

    /** CU04: registra el pedido pendiente con pago digital y calcula subtotales/total. */
    public Pedido crearPedido(int idCliente, String tipoEntrega, String metodoPago,
                              String comprobanteUrl, Map<Integer, Integer> carrito) {
        if (idCliente <= 0) {
            throw new ServiceException("Debe iniciar sesión como cliente para pedir");
        }
        if (carrito == null || carrito.isEmpty()) {
            throw new ServiceException("El carrito está vacío");
        }
        if (!List.of(Pedido.TIPO_DELIVERY, Pedido.TIPO_RECOJO).contains(tipoEntrega)) {
            throw new ServiceException("Modalidad de entrega no válida");
        }
        if (!List.of(Pedido.PAGO_YAPE, Pedido.PAGO_PLIN, Pedido.PAGO_TARJETA).contains(metodoPago)) {
            throw new ServiceException("Método de pago no válido");
        }
        if (comprobanteUrl == null || comprobanteUrl.isBlank()) {
            throw new ServiceException("Debe adjuntar el comprobante del pago digital");
        }

        try (Connection conn = ConexionBD.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Pedido pedido = new Pedido();
                pedido.setIdCliente(idCliente);
                pedido.setFechaHora(LocalDateTime.now());
                pedido.setTipoEntrega(tipoEntrega);
                pedido.setMetodoPago(metodoPago);
                pedido.setComprobanteUrl(comprobanteUrl);
                pedido.setEstadoPedido(Pedido.PENDIENTE);

                BigDecimal total = BigDecimal.ZERO;
                for (Map.Entry<Integer, Integer> entry : new LinkedHashMap<>(carrito).entrySet()) {
                    int idProducto = entry.getKey();
                    int cantidad = entry.getValue() == null ? 0 : entry.getValue();
                    if (cantidad <= 0) {
                        throw new ServiceException("Cantidad inválida para el producto " + idProducto);
                    }

                    Producto producto = productoDAO.buscarPorId(conn, idProducto);
                    if (producto == null || producto.getPrecioBase() == null) {
                        throw new ServiceException("Uno de los productos ya no está disponible");
                    }
                    inventarioService.validarDisponibilidad(conn, producto, cantidad);

                    DetallePedido detalle = new DetallePedido(producto.getIdProducto(),
                            producto.getNombre(), cantidad, producto.getPrecioBase());
                    pedido.addDetalle(detalle);
                    total = total.add(detalle.getSubtotal());
                }

                pedido.setMontoTotal(total);
                int idPedido = pedidoDAO.insertar(conn, pedido);
                pedido.setIdPedido(idPedido);
                for (DetallePedido detalle : pedido.getDetalles()) {
                    detalle.setIdPedido(idPedido);
                    pedidoDAO.insertarDetalle(conn, idPedido, detalle);
                }

                conn.commit();
                return pedido;
            } catch (Exception e) {
                rollback(conn);
                throw convertir(e, "No se pudo registrar el pedido");
            }
        } catch (SQLException e) {
            throw new ServiceException("No se pudo conectar con la base de datos", e);
        }
    }

    /** CU05/RF10/RNF02: confirma el pago y descuenta insumos en una sola transacción. */
    public Pedido confirmarPedido(int idPedido) {
        try (Connection conn = ConexionBD.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Pedido pedido = pedidoDAO.buscarPorId(conn, idPedido);
                if (pedido == null) {
                    throw new ServiceException("El pedido no existe");
                }
                if (!Pedido.PENDIENTE.equals(pedido.getEstadoPedido())) {
                    throw new ServiceException("Solo se pueden confirmar pedidos pendientes");
                }
                if (!pedidoDAO.confirmar(conn, idPedido)) {
                    throw new ServiceException("El pedido ya fue confirmado o rechazado");
                }
                // RF10/RNF02: InventarioService aplica el descuento con esta misma
                // conexión y antes del commit; si falla, se revierte el cambio de estado.
                inventarioService.descontarInsumosPorVenta(conn, idPedido);

                pedido.setEstadoPedido(Pedido.CONFIRMADO);
                conn.commit();
                return pedido;
            } catch (Exception e) {
                rollback(conn);
                throw convertir(e, "No se pudo confirmar el pedido");
            }
        } catch (SQLException e) {
            throw new ServiceException("No se pudo conectar con la base de datos", e);
        }
    }

    /** Cambios de estado posteriores a la confirmación (cocina y entrega). */
    public void cambiarEstado(int idPedido, String estadoNuevo) {
        try (Connection conn = ConexionBD.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Pedido pedido = pedidoDAO.buscarPorId(conn, idPedido);
                if (pedido == null) {
                    throw new ServiceException("El pedido no existe");
                }
                List<String> permitidos = TRANSICIONES.get(pedido.getEstadoPedido());
                if (permitidos == null || !permitidos.contains(estadoNuevo)) {
                    throw new ServiceException("Transición de estado no permitida: "
                            + pedido.getEstadoPedido() + " → " + estadoNuevo);
                }
                if (!pedidoDAO.cambiarEstado(conn, idPedido, pedido.getEstadoPedido(), estadoNuevo)) {
                    throw new ServiceException("No se pudo actualizar el pedido");
                }
                conn.commit();
            } catch (Exception e) {
                rollback(conn);
                throw convertir(e, "No se pudo actualizar el pedido");
            }
        } catch (SQLException e) {
            throw new ServiceException("No se pudo conectar con la base de datos", e);
        }
    }

    public List<Pedido> listar(String estado) {
        try {
            return pedidoDAO.listar(estado);
        } catch (SQLException e) {
            throw new ServiceException("No se pudieron cargar los pedidos", e);
        }
    }

    public List<Pedido> listarPorCliente(int idCliente) {
        try {
            return pedidoDAO.listarPorCliente(idCliente);
        } catch (SQLException e) {
            throw new ServiceException("No se pudieron cargar tus pedidos", e);
        }
    }

    public Pedido buscarPorId(int idPedido) {
        try {
            return pedidoDAO.buscarPorId(idPedido);
        } catch (SQLException e) {
            throw new ServiceException("No se pudo cargar el pedido", e);
        }
    }

    public BigDecimal totalVendido() {
        try {
            return pedidoDAO.totalVendido();
        } catch (SQLException e) {
            throw new ServiceException("No se pudo calcular el total de ventas", e);
        }
    }

    private ServiceException convertir(Exception e, String mensaje) {
        if (e instanceof ServiceException se) {
            return se;
        }
        return new ServiceException(mensaje, e);
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // Se conserva la excepción original del bloque transaccional.
        }
    }
}
