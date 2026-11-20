package com.megapizza.model;

import java.math.BigDecimal;

/** Insumo del almacén. Corresponde a la tabla {@code tb_inventario}. */
public class Inventario {

    private int idInsumo;
    private String nombreInsumo;
    private BigDecimal stockActual;
    private BigDecimal stockMinimo;
    private String unidadMedida;

    public Inventario() {
    }

    /** RF12: alerta cuando el stock alcanza o cae por debajo del mínimo. */
    public boolean tieneAlerta() {
        return stockActual != null && stockMinimo != null
                && stockActual.compareTo(stockMinimo) <= 0;
    }

    public int getIdInsumo() {
        return idInsumo;
    }

    public void setIdInsumo(int idInsumo) {
        this.idInsumo = idInsumo;
    }

    public String getNombreInsumo() {
        return nombreInsumo;
    }

    public void setNombreInsumo(String nombreInsumo) {
        this.nombreInsumo = nombreInsumo;
    }

    public BigDecimal getStockActual() {
        return stockActual;
    }

    public void setStockActual(BigDecimal stockActual) {
        this.stockActual = stockActual;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
}
