package com.megapizza.dao;

import com.megapizza.config.ConexionBD;
import com.megapizza.model.Inventario;
import com.megapizza.model.RecetaInsumo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de {@code tb_inventario} y {@code tb_receta_insumo}.
 * Todos los métodos de escritura reciben la conexión de la transacción para
 * cumplir RNF02: cabecera, detalles y descuento de stock se confirman juntos.
 */
public class InventarioDAO {

    private static final String SELECT =
            "SELECT id_insumo, nombre_insumo, stock_actual, stock_minimo, unidad_medida "
            + "FROM tb_inventario ";

    public List<Inventario> listarTodos() throws SQLException {
        return listar(SELECT + "ORDER BY nombre_insumo");
    }

    /** RF12: insumos cuyo stockactual es menor o igual al mínimo. */
    public List<Inventario> listarConAlerta() throws SQLException {
        return listar(SELECT + "WHERE stock_actual <= stock_minimo ORDER BY nombre_insumo");
    }

    public Inventario buscarPorId(int idInsumo) throws SQLException {
        try (Connection conn = ConexionBD.getConnection()) {
            return buscarPorId(conn, idInsumo);
        }
    }

    public Inventario buscarPorId(Connection conn, int idInsumo) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE id_insumo = ?")) {
            ps.setInt(1, idInsumo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Receta de un producto, cruzando {@code tb_receta_insumo} con {@code tb_inventario}. */
    public List<RecetaInsumo> listarReceta(Connection conn, int idProducto) throws SQLException {
        String sql = "SELECT ri.id_receta, ri.id_producto, ri.id_insumo, ri.cantidad_necesaria, "
                + "i.nombre_insumo, i.unidad_medida "
                + "FROM tb_receta_insumo ri "
                + "JOIN tb_inventario i ON i.id_insumo = ri.id_insumo "
                + "WHERE ri.id_producto = ? ORDER BY ri.id_insumo";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                List<RecetaInsumo> receta = new ArrayList<>();
                while (rs.next()) {
                    RecetaInsumo r = new RecetaInsumo();
                    r.setIdReceta(rs.getInt("id_receta"));
                    r.setIdProducto(rs.getInt("id_producto"));
                    r.setIdInsumo(rs.getInt("id_insumo"));
                    r.setCantidadNecesaria(rs.getBigDecimal("cantidad_necesaria"));
                    r.setNombreInsumo(rs.getString("nombre_insumo"));
                    r.setUnidadMedida(rs.getString("unidad_medida"));
                    receta.add(r);
                }
                return receta;
            }
        }
    }

    /** RF10: verifica disponibilidad antes de vender, sin descontar todavía. */
    public boolean hayStock(Connection conn, int idInsumo, BigDecimal cantidad) throws SQLException {
        String sql = "SELECT 1 FROM tb_inventario WHERE id_insumo = ? AND stock_actual >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idInsumo);
            ps.setBigDecimal(2, cantidad);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** RF09: entrada de mercadería de proveedor. */
    public void sumarStock(Connection conn, int idInsumo, BigDecimal cantidad) throws SQLException {
        String sql = "UPDATE tb_inventario SET stock_actual = stock_actual + ? WHERE id_insumo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, cantidad);
            ps.setInt(2, idInsumo);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("El insumo " + idInsumo + " no existe");
            }
        }
    }

    /**
     * RF10/RNF02: descuenta un insumo solo si hay stock suficiente.
     * Devuelve false cuando no alcanza, para que el servicio haga rollback.
     */
    public boolean descontarStock(Connection conn, int idInsumo, BigDecimal cantidad) throws SQLException {
        String sql = "UPDATE tb_inventario SET stock_actual = stock_actual - ? "
                + "WHERE id_insumo = ? AND stock_actual >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, cantidad);
            ps.setInt(2, idInsumo);
            ps.setBigDecimal(3, cantidad);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Descuenta todos los insumos de un pedido a partir de {@code tb_detalle_pedido}
     * y {@code tb_receta_insumo}. Las cantidades se agrupan por insumo para que
     * un mismo insumo nunca se descuente dos veces dentro del pedido (RNF02).
     * Debe ejecutarse con la misma conexión de la transacción de confirmación.
     *
     * @throws SQLException si no hay stock para alguno de los insumos
     */
    public void descontarInsumosPorPedido(Connection conn, int idPedido) throws SQLException {
        String sql = "SELECT ri.id_insumo, i.nombre_insumo, "
                + "SUM(ri.cantidad_necesaria * dp.cantidad) AS requerido "
                + "FROM tb_detalle_pedido dp "
                + "JOIN tb_receta_insumo ri ON ri.id_producto = dp.id_producto "
                + "JOIN tb_inventario i ON i.id_insumo = ri.id_insumo "
                + "WHERE dp.id_pedido = ? "
                + "GROUP BY ri.id_insumo, i.nombre_insumo "
                + "ORDER BY ri.id_insumo";

        List<Requerimiento> requerimientos = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requerimientos.add(new Requerimiento(
                            rs.getInt("id_insumo"),
                            rs.getString("nombre_insumo"),
                            rs.getBigDecimal("requerido")));
                }
            }
        }
        if (requerimientos.isEmpty()) {
            throw new SQLException("El pedido " + idPedido + " no tiene insumos por descontar");
        }
        for (Requerimiento r : requerimientos) {
            if (!descontarStock(conn, r.idInsumo(), r.cantidad())) {
                throw new SQLException("Stock insuficiente de " + r.nombreInsumo()
                        + " para confirmar el pedido " + idPedido);
            }
        }
    }

    /** Merma o baja de insumo (RF11): descuenta validando stock disponible. */
    public void registrarMerma(Connection conn, int idInsumo, BigDecimal cantidad) throws SQLException {
        if (!descontarStock(conn, idInsumo, cantidad)) {
            throw new SQLException("Stock insuficiente para registrar la merma del insumo " + idInsumo);
        }
    }

    private List<Inventario> listar(String sql) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Inventario> insumos = new ArrayList<>();
            while (rs.next()) {
                insumos.add(mapear(rs));
            }
            return insumos;
        }
    }

    private Inventario mapear(ResultSet rs) throws SQLException {
        Inventario i = new Inventario();
        i.setIdInsumo(rs.getInt("id_insumo"));
        i.setNombreInsumo(rs.getString("nombre_insumo"));
        i.setStockActual(rs.getBigDecimal("stock_actual"));
        i.setStockMinimo(rs.getBigDecimal("stock_minimo"));
        i.setUnidadMedida(rs.getString("unidad_medida"));
        return i;
    }

    private record Requerimiento(int idInsumo, String nombreInsumo, BigDecimal cantidad) {
    }
}
