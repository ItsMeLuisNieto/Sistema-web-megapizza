package com.megapizza.controller;

import com.megapizza.dao.ClienteDAO;
import com.megapizza.dao.ProductoDAO;
import com.megapizza.exception.ServiceException;
import com.megapizza.model.Cliente;
import com.megapizza.model.DetallePedido;
import com.megapizza.model.Pedido;
import com.megapizza.model.Usuario;
import com.megapizza.service.PedidoService;
import com.megapizza.util.CarritoUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * CU03/CU04: carrito de la sesión, registro del pedido con pago digital e
 * historial del cliente. Toda regla de montos y stock queda en PedidoService.
 */
@WebServlet("/pedido")
public class PedidoController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final PedidoService pedidoService = new PedidoService();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("mispedidos".equals(action)) {
            req.setAttribute("pedidos", pedidoService.listarPorCliente(idCliente(req)));
            req.getRequestDispatcher("/misPedidos.jsp").forward(req, res);
            return;
        }
        verCarrito(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String action = req.getParameter("action");
        Map<Integer, Integer> carrito = CarritoUtil.obtener(req.getSession());

        switch (action == null ? "" : action) {
            case "agregar" -> {
                CarritoUtil.agregar(carrito, parseId(req.getParameter("id")));
                responder(req, res, carrito);
            }
            case "quitar" -> {
                CarritoUtil.quitar(carrito, parseId(req.getParameter("id")));
                responder(req, res, carrito);
            }
            case "fijar" -> {
                CarritoUtil.fijarCantidad(carrito, parseId(req.getParameter("id")),
                        parseId(req.getParameter("cantidad")));
                responder(req, res, carrito);
            }
            case "vaciar" -> {
                CarritoUtil.vaciar(req.getSession());
                res.sendRedirect(req.getContextPath() + "/pedido?action=ver&ok=Carrito%20vac%C3%ADo");
            }
            case "confirmar" -> confirmar(req, res, carrito);
            default -> res.sendRedirect(req.getContextPath() + "/catalogo");
        }
    }

    private void verCarrito(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            Map<Integer, Integer> carrito = CarritoUtil.obtener(req.getSession());
            List<DetallePedido> detalles = CarritoUtil.detalles(carrito, productoDAO);
            req.setAttribute("detalles", detalles);
            req.setAttribute("total", CarritoUtil.total(detalles));
            int idCliente = idCliente(req);
            req.setAttribute("cliente", idCliente > 0 ? clienteDAO.buscarPorId(idCliente) : null);
            req.getRequestDispatcher("/vistaCarrito.jsp").forward(req, res);
        } catch (SQLException e) {
            throw new ServiceException("No se pudo cargar el carrito", e);
        }
    }

    /** CU04: entrega, método digital y comprobante; el carrito se conserva si falla. */
    private void confirmar(HttpServletRequest req, HttpServletResponse res, Map<Integer, Integer> carrito)
            throws IOException {
        int idCliente = idCliente(req);
        if (idCliente <= 0) {
            res.sendRedirect(req.getContextPath() + "/index.jsp?error=sin-cliente");
            return;
        }
        try {
            Pedido pedido = pedidoService.crearPedido(
                    idCliente,
                    parametro(req, "tipoEntrega", Pedido.TIPO_DELIVERY),
                    parametro(req, "metodoPago", null),
                    parametro(req, "comprobanteUrl", null),
                    carrito);
            CarritoUtil.vaciar(req.getSession());
            res.sendRedirect(req.getContextPath() + "/pedido?action=mispedidos&ok=Pedido%20"
                    + pedido.getIdPedido() + "%20registrado");
        } catch (ServiceException | IllegalArgumentException e) {
            res.sendRedirect(req.getContextPath() + "/pedido?action=ver&error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    private int idCliente(HttpServletRequest req) {
        Object valor = req.getSession().getAttribute("idCliente");
        if (valor instanceof Integer id && id > 0) {
            return id;
        }
        Usuario usuario = (Usuario) req.getSession().getAttribute("usuario");
        if (usuario == null) {
            return 0;
        }
        try {
            Cliente cliente = clienteDAO.buscarPorUsuario(usuario.getIdUsuario());
            return cliente == null ? 0 : cliente.getIdCliente();
        } catch (SQLException e) {
            throw new ServiceException("No se pudo identificar al cliente", e);
        }
    }

    /** Respuesta AJAX para carrito.js; las peticiones de formulario se redirigen. */
    private void responder(HttpServletRequest req, HttpServletResponse res, Map<Integer, Integer> carrito)
            throws IOException {
        if ("XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"unidades\":" + CarritoUtil.totalUnidades(carrito) + "}");
            return;
        }
        res.sendRedirect(req.getContextPath() + "/catalogo");
    }

    private String parametro(HttpServletRequest req, String nombre, String defecto) {
        String valor = req.getParameter(nombre);
        return valor == null || valor.isBlank() ? defecto : valor.trim();
    }

    private int parseId(String valor) {
        try {
            int id = Integer.parseInt(valor);
            if (id <= 0) {
                throw new NumberFormatException("Valor no positivo");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new ServiceException("Producto o cantidad no válidos");
        }
    }
}
