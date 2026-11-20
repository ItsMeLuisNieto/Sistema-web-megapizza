package com.megapizza.util;

import com.megapizza.dao.ProductoDAO;
import com.megapizza.model.DetallePedido;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Operaciones sobre el carrito almacenado en la sesión HTTP (RF03, CU03). */
public final class CarritoUtil {

    private static final String ATRIBUTO = "carrito";

    private CarritoUtil() {
    }

    /** Obtiene el carrito de la sesión, creándolo vacío si todavía no existe. */
    @SuppressWarnings("unchecked")
    public static Map<Integer, Integer> obtener(HttpSession sesion) {
        Object valor = sesion.getAttribute(ATRIBUTO);
        if (!(valor instanceof Map)) {
            valor = new LinkedHashMap<Integer, Integer>();
            sesion.setAttribute(ATRIBUTO, valor);
        }
        return (Map<Integer, Integer>) valor;
    }

    /** RF03: agrega una unidad del producto al carrito. */
    public static void agregar(Map<Integer, Integer> carrito, int idProducto) {
        validarIdProducto(idProducto);
        carrito.merge(idProducto, 1, Integer::sum);
    }

    /** RF03: retira una unidad; si era la última, elimina la línea. */
    public static void quitar(Map<Integer, Integer> carrito, int idProducto) {
        validarIdProducto(idProducto);
        Integer cantidad = carrito.get(idProducto);
        if (cantidad == null) {
            return;
        }
        if (cantidad <= 1) {
            carrito.remove(idProducto);
        } else {
            carrito.put(idProducto, cantidad - 1);
        }
    }

    /** RF03: fija la cantidad de un producto; cero o menos lo retira del carrito. */
    public static void fijarCantidad(Map<Integer, Integer> carrito, int idProducto, int cantidad) {
        validarIdProducto(idProducto);
        if (cantidad <= 0) {
            carrito.remove(idProducto);
        } else {
            carrito.put(idProducto, cantidad);
        }
    }

    public static void vaciar(HttpSession sesion) {
        sesion.removeAttribute(ATRIBUTO);
    }

    public static int totalUnidades(Map<Integer, Integer> carrito) {
        int total = 0;
        for (Integer cantidad : carrito.values()) {
            total += cantidad == null ? 0 : cantidad;
        }
        return total;
    }

    /** Convierte el carrito en detalles con los precios vigentes de la carta. */
    public static List<DetallePedido> detalles(Map<Integer, Integer> carrito, ProductoDAO productoDAO)
            throws SQLException {
        List<DetallePedido> detalles = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : carrito.entrySet()) {
            var producto = productoDAO.buscarPorId(entry.getKey());
            if (producto == null || entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            detalles.add(new DetallePedido(producto.getIdProducto(), producto.getNombre(),
                    entry.getValue(), producto.getPrecioBase()));
        }
        return detalles;
    }

    /** Suma los subtotales ya calculados en cada línea del carrito. */
    public static BigDecimal total(List<DetallePedido> detalles) {
        BigDecimal total = BigDecimal.ZERO;
        for (DetallePedido detalle : detalles) {
            total = total.add(detalle.getSubtotal());
        }
        return total;
    }

    private static void validarIdProducto(int idProducto) {
        if (idProducto <= 0) {
            throw new IllegalArgumentException("Producto no válido");
        }
    }
}
