package com.megapizza.model;

import java.math.BigDecimal;

/** Insumo necesario para elaborar un producto. Corresponde a {@code tb_receta_insumo}. */
public class RecetaInsumo {

    private int idReceta;
    private int idProducto;
    private int idInsumo;
    private BigDecimal cantidadNecesaria;

    /** Datos derivados del join con tb_inventario; no se persisten aquí. */
    private String nombreInsumo;
    private String unidadMedida;

    public RecetaInsumo() {
    }

    public int getIdReceta() {
        return idReceta;
    }

    public void setIdReceta(int idReceta) {
        this.idReceta = idReceta;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getIdInsumo() {
        return idInsumo;
    }

    public void setIdInsumo(int idInsumo) {
        this.idInsumo = idInsumo;
    }

    public BigDecimal getCantidadNecesaria() {
        return cantidadNecesaria;
    }

    public void setCantidadNecesaria(BigDecimal cantidadNecesaria) {
        this.cantidadNecesaria = cantidadNecesaria;
    }

    public String getNombreInsumo() {
        return nombreInsumo;
    }

    public void setNombreInsumo(String nombreInsumo) {
        this.nombreInsumo = nombreInsumo;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
}
