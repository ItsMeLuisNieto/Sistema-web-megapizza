package com.megapizza.dao;

import com.megapizza.config.ConexionBD;
import com.megapizza.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de {@code tb_cliente}. */
public class ClienteDAO {

    private static final String SELECT =
            "SELECT id_cliente, id_usuario, nombre, telefono, direccion, correo FROM tb_cliente ";

    public Cliente buscarPorId(int idCliente) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE id_cliente = ?")) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Cliente buscarPorUsuario(int idUsuario) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE id_usuario = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Cliente> listarTodos() throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + "ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            List<Cliente> clientes = new ArrayList<>();
            while (rs.next()) {
                clientes.add(mapear(rs));
            }
            return clientes;
        }
    }

    public int insertar(Cliente cliente) throws SQLException {
        try (Connection conn = ConexionBD.getConnection()) {
            return insertar(conn, cliente);
        }
    }

    /** Variante con conexión externa para el registro atómico de CU02. */
    public int insertar(Connection conn, Cliente cliente) throws SQLException {
        String sql = "INSERT INTO tb_cliente (id_usuario, nombre, telefono, direccion, correo) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (cliente.getIdUsuario() == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, cliente.getIdUsuario());
            }
            ps.setString(2, cliente.getNombre());
            ps.setString(3, cliente.getTelefono());
            ps.setString(4, cliente.getDireccion());
            ps.setString(5, cliente.getCorreo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        int idUsuario = rs.getInt("id_usuario");
        c.setIdUsuario(rs.wasNull() ? null : idUsuario);
        c.setNombre(rs.getString("nombre"));
        c.setTelefono(rs.getString("telefono"));
        c.setDireccion(rs.getString("direccion"));
        c.setCorreo(rs.getString("correo"));
        return c;
    }
}
