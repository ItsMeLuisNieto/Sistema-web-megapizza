package com.megapizza.dao;

import com.megapizza.config.ConexionBD;
import com.megapizza.model.DetallePedido;
import com.megapizza.model.Pedido;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de {@code tb_pedido} y {@code tb_detalle_pedido}.
 *
 * Las escrituras reciben la conexión de la transacción (RNF02): el servicio
 * desactiva el auto-commit, inserta cabecera y detalles y, al confirmar,
 * descuenta el stock mediante InventarioDAO antes de hacer commit.
 */
public class PedidoDAO {

    private static final String SELECT =
            "SELECT p.id_pedido, p.id_cliente, p.fecha_hora, p.tipo_entrega, p.metodo_pago, "
            + "p.comprobante_url, p.estado_pedido, p.monto_total, c.nombre AS nombre_cliente "
            + "FROM tb_pedido p LEFT JOIN tb_cliente c ON c.id_cliente = p.id_cliente ";

    public int insertar(Connection conn, Pedido pedido) throws SQLException {
        String sql = "INSERT INTO tb_pedido "
                + "(id_cliente, fecha_hora, tipo_entrega, metodo_pago, comprobante_url, "
                + " estado_pedido, monto_total) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pedido.getIdCliente());
            ps.setObject(2, pedido.getFechaHora() == null ? LocalDateTime.now() : pedido.getFechaHora());
            ps.setString(3, pedido.getTipoEntrega());
            ps.setString(4, pedido.getMetodoPago());
            if (pedido.getComprobanteUrl() == null || pedido.getComprobanteUrl().isBlank()) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, pedido.getComprobanteUrl());
            }
            ps.setString(6, pedido.getEstadoPedido() == null ? Pedido.PENDIENTE : pedido.getEstadoPedido());
            ps.setBigDecimal(7, pedido.getMontoTotal());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("No se obtuvo el id del pedido creado");
                }
                return rs.getInt(1);
            }
        }
    }

    public void insertarDetalle(Connection conn, int idPedido, DetallePedido detalle) throws SQLException {
        String sql = "INSERT INTO tb_detalle_pedido "
                + "(id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            ps.setInt(2, detalle.getIdProducto());
            ps.setInt(3, detalle.getCantidad());
            ps.setBigDecimal(4, detalle.getPrecioUnitario());
            ps.setBigDecimal(5, detalle.getSubtotal());
            ps.executeUpdate();
        }
    }

    /**
     * Transición Pendiente → Confirmado. La condición sobre el estado actual
     * garantiza que un mismo pedido no pueda confirmarse (y descontar stock) dos veces.
     *
     * @return true si el pedido estaba Pendiente y pasó a Confirmado
     */
    public boolean confirmar(Connection conn, int idPedido) throws SQLException {
        String sql = "UPDATE tb_pedido SET estado_pedido = ? "
                + "WHERE id_pedido = ? AND estado_pedido = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Pedido.CONFIRMADO);
            ps.setInt(2, idPedido);
            ps.setString(3, Pedido.PENDIENTE);
            return ps.executeUpdate() == 1;
        }
    }

    /** Transición genérica con control del estado anterior. */
    public boolean cambiarEstado(Connection conn, int idPedido, String estadoActual, String estadoNuevo)
            throws SQLException {
        String sql = "UPDATE tb_pedido SET estado_pedido = ? "
                + "WHERE id_pedido = ? AND estado_pedido = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estadoNuevo);
            ps.setInt(2, idPedido);
            ps.setString(3, estadoActual);
            return ps.executeUpdate() == 1;
        }
    }

    public List<Pedido> listar(String estado) throws SQLException {
        String sql = SELECT
                + (estado == null || estado.isBlank() ? "" : "WHERE p.estado_pedido = ? ")
                + "ORDER BY p.fecha_hora DESC, p.id_pedido DESC";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (estado != null && !estado.isBlank()) {
                ps.setString(1, estado);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Pedido> pedidos = new ArrayList<>();
                while (rs.next()) {
                    Pedido p = mapear(rs);
                    p.setDetalles(listarDetalles(conn, p.getIdPedido()));
                    pedidos.add(p);
                }
                return pedidos;
            }
        }
    }

    public List<Pedido> listarPorCliente(int idCliente) throws SQLException {
        String sql = SELECT + "WHERE p.id_cliente = ? ORDER BY p.fecha_hora DESC, p.id_pedido DESC";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                List<Pedido> pedidos = new ArrayList<>();
                while (rs.next()) {
                    pedidos.add(mapear(rs));
                }
                return pedidos;
            }
        }
    }

    public Pedido buscarPorId(int idPedido) throws SQLException {
        try (Connection conn = ConexionBD.getConnection()) {
            return buscarPorId(conn, idPedido);
        }
    }

    public Pedido buscarPorId(Connection conn, int idPedido) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE p.id_pedido = ?")) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Pedido pedido = mapear(rs);
                pedido.setDetalles(listarDetalles(conn, idPedido));
                return pedido;
            }
        }
    }

    public List<DetallePedido> listarDetalles(Connection conn, int idPedido) throws SQLException {
        String sql = "SELECT d.id_detalle, d.id_pedido, d.id_producto, pr.nombre AS nombre_producto, "
                + "d.cantidad, d.precio_unitario, d.subtotal "
                + "FROM tb_detalle_pedido d JOIN tb_producto pr ON pr.id_producto = d.id_producto "
                + "WHERE d.id_pedido = ? ORDER BY d.id_detalle";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                List<DetallePedido> detalles = new ArrayList<>();
                while (rs.next()) {
                    DetallePedido d = new DetallePedido();
                    d.setIdDetalle(rs.getInt("id_detalle"));
                    d.setIdPedido(rs.getInt("id_pedido"));
                    d.setIdProducto(rs.getInt("id_producto"));
                    d.setNombreProducto(rs.getString("nombre_producto"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    d.setSubtotal(rs.getBigDecimal("subtotal"));
                    detalles.add(d);
                }
                return detalles;
            }
        }
    }

    /** RF13: total de ventas entregadas. */
    public BigDecimal totalVendido() throws SQLException {
        String sql = "SELECT COALESCE(SUM(monto_total), 0) FROM tb_pedido WHERE estado_pedido = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Pedido.ENTREGADO);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setIdPedido(rs.getInt("id_pedido"));
        p.setIdCliente(rs.getInt("id_cliente"));
        p.setFechaHora(rs.getObject("fecha_hora", LocalDateTime.class));
        p.setTipoEntrega(rs.getString("tipo_entrega"));
        p.setMetodoPago(rs.getString("metodo_pago"));
        p.setComprobanteUrl(rs.getString("comprobante_url"));
        p.setEstadoPedido(rs.getString("estado_pedido"));
        p.setMontoTotal(rs.getBigDecimal("monto_total"));
        p.setNombreCliente(rs.getString("nombre_cliente"));
        return p;
    }
}
