package com.megapizza.controller;

import com.megapizza.config.ConexionBD;
import com.megapizza.dao.ClienteDAO;
import com.megapizza.dao.UsuarioDAO;
import com.megapizza.exception.ServiceException;
import com.megapizza.model.Cliente;
import com.megapizza.model.Usuario;
import com.megapizza.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * CU02: inicio de sesión, registro de clientes y cierre de sesión.
 * El filtro y los servicios consumen el objeto {@code Usuario} de la sesión.
 */
@WebServlet("/auth")
public class AuthController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("registro".equalsIgnoreCase(action == null ? "" : action.trim())
                || "register".equalsIgnoreCase(action == null ? "" : action.trim())) {
            registrarCliente(req, res);
        } else {
            iniciarSesion(req, res);
        }
    }

    /** GET = logout (compatibilidad con la barra de navegación existente). */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession sesion = req.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        res.sendRedirect(req.getContextPath() + "/login.jsp");
    }

    private void iniciarSesion(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = parametro(req, "username");
        String password = req.getParameter("password");
        try {
            Usuario usuario = username.isEmpty() ? null : usuarioDAO.buscarPorUsername(username);
            if (usuario == null || !usuario.estaActivo()
                    || !PasswordUtil.verificar(password, usuario.getPasswordHash())) {
                res.sendRedirect(req.getContextPath() + "/login.jsp?error=credenciales");
                return;
            }
            establecerSesion(req, usuario);
            res.sendRedirect(req.getContextPath() + inicioPorRol(usuario.getRol()));
        } catch (SQLException e) {
            redirigirError(req, res, "/login.jsp", "No se pudo conectar con la base de datos");
        }
    }

    /**
     * CU02: alta de usuario CLIENTE y su ficha en {@code tb_cliente} en una
     * sola transacción. Después de registrarse queda autenticado.
     */
    private void registrarCliente(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = parametro(req, "username");
        String password = req.getParameter("password");
        String nombre = parametro(req, "nombre");
        String telefono = parametro(req, "telefono");
        String direccion = parametro(req, "direccion");
        String correo = parametro(req, "correo");

        if (username.isEmpty() || username.length() > 50
                || password == null || password.isBlank() || nombre.isEmpty()) {
            redirigirError(req, res, "/login.jsp", "Complete usuario, contraseña y nombre");
            return;
        }
        try {
            if (usuarioDAO.existeUsername(username)) {
                redirigirError(req, res, "/login.jsp", "El usuario ya está registrado");
                return;
            }

            Usuario usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setPasswordHash(PasswordUtil.hash(password));
            usuario.setRol(Usuario.ROL_CLIENTE);
            usuario.setEstado(Usuario.ESTADO_ACTIVO);
            Cliente cliente = new Cliente(nombre, telefono, direccion, correo);

            try (Connection conn = ConexionBD.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    usuarioDAO.insertar(conn, usuario);
                    cliente.setIdUsuario(usuario.getIdUsuario());
                    clienteDAO.insertar(conn, cliente);
                    conn.commit();
                } catch (Exception e) {
                    rollback(conn);
                    throw e;
                }
            }

            establecerSesion(req, usuario);
            res.sendRedirect(req.getContextPath() + "/catalogo");
        } catch (SQLException e) {
            redirigirError(req, res, "/login.jsp", "No se pudo registrar el cliente");
        } catch (ServiceException | IllegalArgumentException e) {
            redirigirError(req, res, "/login.jsp", e.getMessage());
        }
    }

    /** Renueva el identificador de sesión al autenticar para evitar fijación. */
    private void establecerSesion(HttpServletRequest req, Usuario usuario) throws SQLException {
        HttpSession sesion = req.getSession();
        req.changeSessionId();
        sesion.setAttribute("usuario", usuario);
        if (Usuario.ROL_CLIENTE.equals(usuario.getRol())) {
            Cliente cliente = clienteDAO.buscarPorUsuario(usuario.getIdUsuario());
            sesion.setAttribute("idCliente", cliente == null ? null : cliente.getIdCliente());
        } else {
            sesion.removeAttribute("idCliente");
        }
    }

    private String inicioPorRol(String rol) {
        return switch (rol == null ? "" : rol) {
            case Usuario.ROL_CAJERO, Usuario.ROL_ADMINISTRADOR -> "/caja";
            case Usuario.ROL_COCINERO -> "/cocina";
            case Usuario.ROL_ALMACEN -> "/almacen";
            default -> "/catalogo";
        };
    }

    private String parametro(HttpServletRequest req, String nombre) {
        String valor = req.getParameter(nombre);
        return valor == null ? "" : valor.trim();
    }

    private void redirigirError(HttpServletRequest req, HttpServletResponse res,
                                String vista, String mensaje) throws IOException {
        res.sendRedirect(req.getContextPath() + vista + "?error="
                + URLEncoder.encode(mensaje, StandardCharsets.UTF_8));
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // Se preserva la excepción original del bloque transaccional.
        }
    }
}
