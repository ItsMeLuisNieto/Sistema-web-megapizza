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
import java.util.Locale;

/** CU05/CU07: el cajero valida o rechaza comprobantes y registra entregas. */
@WebServlet("/caja")
public class CajaController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final PedidoService pedidoService = new PedidoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setAttribute("pedidos", pedidoService.listar(Pedido.PENDIENTE));
        req.setAttribute("listos", pedidoService.listar(Pedido.LISTO));
        req.setAttribute("totalVendido", pedidoService.totalVendido());
        req.getRequestDispatcher("/vistaCaja.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String accion = req.getParameter("action");
        String estado = req.getParameter("estado");
        try {
            int idPedido = parseId(req.getParameter("id"));
            String mensaje;
            if ("rechazar".equals(accion)) {
                pedidoService.cambiarEstado(idPedido, Pedido.RECHAZADO);
                mensaje = "Comprobante rechazado";
            } else if ("confirmar".equals(accion)) {
                pedidoService.confirmarPedido(idPedido);
                mensaje = "Pago confirmado";
            } else if ("entregar".equals(accion) || esEntregado(estado)) {
                pedidoService.cambiarEstado(idPedido, Pedido.ENTREGADO);
                mensaje = "Entrega registrada";
            } else {
                throw new ServiceException("Acción de caja no válida");
            }
            res.sendRedirect(req.getContextPath() + "/caja?ok="
                    + URLEncoder.encode(mensaje, StandardCharsets.UTF_8));
        } catch (ServiceException | NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/caja?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    /** Acepta tanto el valor canónico como el de la vista heredada. */
    private boolean esEntregado(String estado) {
        if (estado == null) {
            return false;
        }
        String valor = estado.trim().replace('_', ' ').replace('-', ' ').toLowerCase(Locale.ROOT);
        return valor.equals("entregado");
    }

    private int parseId(String valor) {
        if (valor == null) {
            throw new ServiceException("Pedido no válido");
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new ServiceException("Pedido no válido");
        }
    }
}
