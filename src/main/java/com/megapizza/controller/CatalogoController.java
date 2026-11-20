package com.megapizza.controller;

import com.megapizza.dao.ProductoDAO;
import com.megapizza.exception.ServiceException;
import com.megapizza.model.Producto;
import com.megapizza.util.CarritoUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/** CU01: carta digital, filtrable por categoría o por texto. */
@WebServlet("/catalogo")
public class CatalogoController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String filtro = req.getParameter("q");
        String busqueda = filtro == null ? "" : filtro.trim();
        try {
            List<String> categorias = productoDAO.listarCategorias();
            String categoria = categorias.stream()
                    .filter(c -> c.equalsIgnoreCase(busqueda))
                    .findFirst()
                    .orElse(null);

            List<Producto> productos;
            if (busqueda.isEmpty()) {
                productos = productoDAO.listarTodos();
            } else if (categoria != null) {
                productos = productoDAO.listarPorCategoria(categoria);
            } else {
                productos = productoDAO.buscar(busqueda);
            }

            req.setAttribute("productos", productos);
            req.setAttribute("categorias", categorias);
            req.setAttribute("filtro", filtro);
            req.setAttribute("unidadesCarrito", CarritoUtil.totalUnidades(
                    CarritoUtil.obtener(req.getSession())));
            req.getRequestDispatcher("/catalogo.jsp").forward(req, res);
        } catch (SQLException e) {
            throw new ServiceException("No se pudo cargar el catálogo", e);
        }
    }
}
