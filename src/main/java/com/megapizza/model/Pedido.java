package com.megapizza.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Cabecera de una venta. Corresponde a la tabla {@code tb_pedido}. */
public class Pedido {

    public static final String TIPO_DELIVERY = "Delivery";
    public static final String TIPO_RECOJO = "Recojo";

    public static final String PAGO_YAPE = "Yape";
    public static final String PAGO_PLIN = "Plin";
    public static final String PAGO_TARJETA = "Tarjeta";

    public static final String PENDIENTE = "Pendiente";
    public static final String CONFIRMADO = "Confirmado";
    public static final String EN_PREPARACION = "En Preparacion";
    public static final String LISTO = "Listo";
    public static final String ENTREGADO = "Entregado";
    public static final String RECHAZADO = "Rechazado";

    private int idPedido;
    private int idCliente;
    private LocalDateTime fechaHora;
    private String tipoEntrega;
    private String metodoPago;
    private String comprobanteUrl;
    private String estadoPedido;
    private BigDecimal montoTotal;

    /** Datos derivados (no se persisten en tb_pedido). */
    private String nombreCliente;
    private List<DetallePedido> detalles = new ArrayList<>();

    public Pedido() {
    }

    public void addDetalle(DetallePedido detalle) {
        this.detalles.add(detalle);
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getTipoEntrega() {
        return tipoEntrega;
    }

    public void setTipoEntrega(String tipoEntrega) {
        this.tipoEntrega = tipoEntrega;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getComprobanteUrl() {
        return comprobanteUrl;
    }

    public void setComprobanteUrl(String comprobanteUrl) {
        this.comprobanteUrl = comprobanteUrl;
    }

    public String getEstadoPedido() {
        return estadoPedido;
    }

    public void setEstadoPedido(String estadoPedido) {
        this.estadoPedido = estadoPedido;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles == null ? new ArrayList<>() : detalles;
    }
}
