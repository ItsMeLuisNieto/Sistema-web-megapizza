package com.megapizza.controller;

import com.megapizza.exception.ServiceException;
import com.megapizza.model.Inventario;
import com.megapizza.service.InventarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * CU08/CU09/CU11/CU12: consulta de stock, abastecimiento, mermas y alertas.
 * CU10 (descuento por venta) se ejecuta desde PedidoService, no desde aquí.
 */
@WebServlet("/almacen")
public class AlmacenController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final InventarioService inventarioService = new InventarioService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setAttribute("insumos", inventarioService.listarInsumos());
        req.setAttribute("alertas", inventarioService.listarAlertas());
        req.getRequestDispatcher("/vistaAlmacen.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String action = req.getParameter("action");
        try {
            int idInsumo = parseId(req.getParameter("idInsumo"));
            BigDecimal cantidad = parseCantidad(req.getParameter("cantidad"));

            switch (action == null ? "" : action) {
                case "entrada" -> inventarioService.registrarEntrada(idInsumo, cantidad);
                case "merma" -> inventarioService.registrarMerma(idInsumo, cantidad);
                case "ajuste" -> registrarAjuste(idInsumo, cantidad);
                default -> throw new ServiceException("Acción de almacén no válida");
            }
            res.sendRedirect(req.getContextPath() + "/almacen?ok=Inventario%20actualizado");
        } catch (ServiceException | IllegalArgumentException e) {
            res.sendRedirect(req.getContextPath() + "/almacen?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    /** Compatibilidad con la vista de ajuste: recibe el stock final, no un delta. */
    private void registrarAjuste(int idInsumo, BigDecimal nuevoStock) {
        Inventario insumo = inventarioService.buscarInsumo(idInsumo);
        if (insumo == null || insumo.getStockActual() == null) {
            throw new ServiceException("El insumo no existe");
        }
        BigDecimal diferencia = nuevoStock.subtract(insumo.getStockActual());
        if (diferencia.signum() > 0) {
            inventarioService.registrarEntrada(idInsumo, diferencia);
        } else if (diferencia.signum() < 0) {
            inventarioService.registrarMerma(idInsumo, diferencia.abs());
        }
    }

    private int parseId(String valor) {
        try {
            int id = Integer.parseInt(valor);
            if (id <= 0) {
                throw new NumberFormatException("Valor no positivo");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new ServiceException("Insumo no válido");
        }
    }

    private BigDecimal parseCantidad(String valor) {
        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException | NullPointerException e) {
            throw new ServiceException("Cantidad no válida");
        }
    }
}
