package com.megapizza.dao;

import com.megapizza.config.ConexionBD;
import com.megapizza.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Acceso a datos de {@code tb_usuario} (RF02 / RNF01). */
public class UsuarioDAO {

    private static final String SELECT =
            "SELECT id_usuario, username, password_hash, rol, estado FROM tb_usuario ";

    public Usuario buscarPorUsername(String username) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Usuario buscarPorId(int idUsuario) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE id_usuario = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** CU02: comprueba unicidad antes de registrar un cliente. */
    public boolean existeUsername(String username) throws SQLException {
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT 1 FROM tb_usuario WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * CU02: alta de usuario con la conexión de la transacción de registro.
     * El servicio/controlador responsable de la transacción hace el commit.
     */
    public Usuario insertar(Connection conn, Usuario usuario) throws SQLException {
        String sql = "INSERT INTO tb_usuario (username, password_hash, rol, estado) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPasswordHash());
            ps.setString(3, usuario.getRol() == null ? Usuario.ROL_CLIENTE : usuario.getRol());
            ps.setString(4, usuario.getEstado() == null ? Usuario.ESTADO_ACTIVO : usuario.getEstado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("No se obtuvo el id del usuario creado");
                }
                usuario.setIdUsuario(rs.getInt(1));
            }
        }
        return usuario;
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRol(rs.getString("rol"));
        u.setEstado(rs.getString("estado"));
        return u;
    }
}
