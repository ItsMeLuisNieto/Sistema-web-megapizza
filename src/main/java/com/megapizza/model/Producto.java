package com.megapizza.model;

import java.math.BigDecimal;

/** Producto de la carta. Corresponde a la tabla {@code tb_producto}. */
public class Producto {

    public static final String CATEGORIA_PIZZA = "Pizza";
    public static final String CATEGORIA_PASTA = "Pasta";
    public static final String CATEGORIA_BEBIDA = "Bebida";
    public static final String CATEGORIA_COMBO = "Combo";

    private int idProducto;
    private String categoria;
    private String nombre;
    private String descripcion;
    private BigDecimal precioBase;
    private String imagenUrl;

    public Producto() {
    }

    public Producto(int idProducto, String categoria, String nombre, BigDecimal precioBase) {
        this.idProducto = idProducto;
        this.categoria = categoria;
        this.nombre = nombre;
        this.precioBase = precioBase;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
}
