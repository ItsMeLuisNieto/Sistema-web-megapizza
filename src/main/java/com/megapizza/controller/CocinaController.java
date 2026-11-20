package com.megapizza.controller;

import com.megapizza.exception.ServiceException;
import com.megapizza.model.Pedido;
import com.megapizza.service.PedidoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** CU06: comandas confirmadas y en preparación; sólo transiciones de cocina. */
@WebServlet("/cocina")
public class CocinaController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final PedidoService pedidoService = new PedidoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        List<Pedido> pedidos = new ArrayList<>();
        pedidos.addAll(pedidoService.listar(Pedido.CONFIRMADO));
        pedidos.addAll(pedidoService.listar(Pedido.EN_PREPARACION));
        req.setAttribute("pedidos", pedidos);
        req.getRequestDispatcher("/vistaCocina.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            int idPedido = parseId(req.getParameter("id"));
            pedidoService.cambiarEstado(idPedido, normalizarEstado(req.getParameter("estado")));
            res.sendRedirect(req.getContextPath() + "/cocina?ok=Estado%20actualizado");
        } catch (ServiceException | IllegalArgumentException e) {
            res.sendRedirect(req.getContextPath() + "/cocina?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    /** La cocina sólo puede avanzar a "En Preparacion" o "Listo" (RF06). */
    private String normalizarEstado(String estado) {
        String valor = estado == null ? "" : estado.trim()
                .replace('_', ' ').replace('-', ' ').toLowerCase(Locale.ROOT);
        return switch (valor) {
            case "en preparacion", "en preparación", "preparacion", "preparar" -> Pedido.EN_PREPARACION;
            case "listo", "listo para despacho" -> Pedido.LISTO;
            default -> throw new ServiceException("Estado de cocina no válido");
        };
    }

    private int parseId(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new ServiceException("Pedido no válido");
        }
    }
}
