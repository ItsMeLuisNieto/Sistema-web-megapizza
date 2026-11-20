package com.megapizza.dao;

import com.megapizza.config.ConexionBD;
import com.megapizza.model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de {@code tb_producto} (RF01). */
public class ProductoDAO {

    private static final String SELECT =
            "SELECT id_producto, categoria, nombre, descripcion, precio_base, imagen_url "
            + "FROM tb_producto ";

    public List<Producto> listarTodos() throws SQLException {
        return listar(SELECT + "ORDER BY categoria, nombre");
    }

    public List<Producto> listarPorCategoria(String categoria) throws SQLException {
        if (categoria == null || categoria.isBlank()) {
            return listarTodos();
        }
        String sql = SELECT + "WHERE categoria = ? ORDER BY nombre";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoria);
            try (ResultSet rs = ps.executeQuery()) {
                List<Producto> productos = new ArrayList<>();
                while (rs.next()) {
                    productos.add(mapear(rs));
                }
                return productos;
            }
        }
    }

    public List<String> listarCategorias() throws SQLException {
        String sql = "SELECT DISTINCT categoria FROM tb_producto ORDER BY categoria";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<String> categorias = new ArrayList<>();
            while (rs.next()) {
                categorias.add(rs.getString("categoria"));
            }
            return categorias;
        }
    }

    public List<Producto> buscar(String texto) throws SQLException {
        String sql = SELECT + "WHERE nombre LIKE ? OR descripcion LIKE ? ORDER BY categoria, nombre";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String patron = "%" + (texto == null ? "" : texto.trim()) + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                List<Producto> productos = new ArrayList<>();
                while (rs.next()) {
                    productos.add(mapear(rs));
                }
                return productos;
            }
        }
    }

    public Producto buscarPorId(int idProducto) throws SQLException {
        try (Connection conn = ConexionBD.getConnection()) {
            return buscarPorId(conn, idProducto);
        }
    }

    /** Variante con conexión externa para leer precios dentro de una transacción. */
    public Producto buscarPorId(Connection conn, int idProducto) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE id_producto = ?")) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    private List<Producto> listar(String sql) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Producto> productos = new ArrayList<>();
            while (rs.next()) {
                productos.add(mapear(rs));
            }
            return productos;
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("id_producto"));
        p.setCategoria(rs.getString("categoria"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecioBase(rs.getBigDecimal("precio_base"));
        p.setImagenUrl(rs.getString("imagen_url"));
        return p;
    }
}
