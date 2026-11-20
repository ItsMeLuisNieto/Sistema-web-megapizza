package com.megapizza.service;

import com.megapizza.config.ConexionBD;
import com.megapizza.dao.InventarioDAO;
import com.megapizza.exception.ServiceException;
import com.megapizza.model.Inventario;
import com.megapizza.model.Producto;
import com.megapizza.model.RecetaInsumo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Lógica de negocio del almacén (RF08-RF12).
 *
 * Las operaciones de venta reciben la conexión de la transacción abierta por
 * {@link PedidoService}: así la validación y el descuento de insumos ocurren
 * dentro de la misma unidad atómica que el cambio de estado del pedido (RNF02).
 */
public class InventarioService {

    private final InventarioDAO inventarioDAO = new InventarioDAO();

    /** RF08: consulta de existencias. */
    public List<Inventario> listarInsumos() {
        try {
            return inventarioDAO.listarTodos();
        } catch (SQLException e) {
            throw new ServiceException("No se pudo cargar el inventario", e);
        }
    }

    public Inventario buscarInsumo(int idInsumo) {
        try {
            return inventarioDAO.buscarPorId(idInsumo);
        } catch (SQLException e) {
            throw new ServiceException("No se pudo cargar el insumo", e);
        }
    }

    /** RF12: insumos cuyo stock alcanzó o bajó del mínimo configurado. */
    public List<Inventario> listarAlertas() {
        try {
            return inventarioDAO.listarConAlerta();
        } catch (SQLException e) {
            throw new ServiceException("No se pudieron cargar las alertas de stock", e);
        }
    }

    /**
     * RF10: verifica la disponibilidad de todos los insumos de la receta antes
     * de confirmar una venta. No descuenta nada: solo valida con la conexión
     * de la transacción que más adelante hará el descuento.
     */
    public void validarDisponibilidad(Connection conn, Producto producto, int cantidad) throws SQLException {
        if (producto == null || producto.getPrecioBase() == null) {
            throw new ServiceException("Uno de los productos ya no está disponible");
        }
        if (cantidad <= 0) {
            throw new ServiceException("La cantidad del producto debe ser mayor que cero");
        }
        List<RecetaInsumo> receta = inventarioDAO.listarReceta(conn, producto.getIdProducto());
        if (receta.isEmpty()) {
            throw new ServiceException("El producto " + producto.getNombre()
                    + " no tiene receta de insumos configurada");
        }
        for (RecetaInsumo insumo : receta) {
            BigDecimal requerido = insumo.getCantidadNecesaria()
                    .multiply(BigDecimal.valueOf(cantidad));
            if (!inventarioDAO.hayStock(conn, insumo.getIdInsumo(), requerido)) {
                throw new ServiceException("Stock insuficiente de " + insumo.getNombreInsumo()
                        + " para preparar " + producto.getNombre());
            }
        }
    }

    /**
     * RF10/RNF02: descuenta los insumos del pedido cruzando
     * {@code tb_detalle_pedido} con {@code tb_receta_insumo}. Debe invocarse
     * con la conexión de la transacción de confirmación; el DAO agrupa por
     * insumo para evitar un doble descuento dentro del mismo pedido.
     */
    public void descontarInsumosPorVenta(Connection conn, int idPedido) throws SQLException {
        inventarioDAO.descontarInsumosPorPedido(conn, idPedido);
    }

    /** RF09: entrada de mercadería de proveedor. */
    public void registrarEntrada(int idInsumo, BigDecimal cantidad) {
        validarCantidad(cantidad);
        ejecutarTransaccion(conn -> inventarioDAO.sumarStock(conn, idInsumo, cantidad),
                "No se pudo registrar la entrada de stock");
    }

    /** RF11: baja por vencimiento o daño; ajusta las existencias. */
    public void registrarMerma(int idInsumo, BigDecimal cantidad) {
        validarCantidad(cantidad);
        ejecutarTransaccion(conn -> inventarioDAO.registrarMerma(conn, idInsumo, cantidad),
                "No se pudo registrar la merma");
    }

    private void ejecutarTransaccion(Operacion operacion, String mensaje) {
        try (Connection conn = ConexionBD.getConnection()) {
            conn.setAutoCommit(false);
            try {
                operacion.ejecutar(conn);
                conn.commit();
            } catch (Exception e) {
                rollback(conn);
                if (e instanceof ServiceException se) {
                    throw se;
                }
                throw new ServiceException(mensaje, e);
            }
        } catch (SQLException e) {
            throw new ServiceException("No se pudo conectar con la base de datos", e);
        }
    }

    private void validarCantidad(BigDecimal cantidad) {
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new ServiceException("La cantidad debe ser mayor que cero");
        }
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // Se conserva la excepción original del bloque transaccional.
        }
    }

    @FunctionalInterface
    private interface Operacion {
        void ejecutar(Connection conn) throws SQLException;
    }
}
