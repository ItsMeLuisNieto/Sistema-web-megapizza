package com.megapizza.model;

/** Usuario del sistema. Corresponde a la tabla {@code tb_usuario}. */
public class Usuario {

    public static final String ROL_CLIENTE = "CLIENTE";
    public static final String ROL_CAJERO = "CAJERO";
    public static final String ROL_COCINERO = "COCINERO";
    public static final String ROL_ALMACEN = "ALMACEN";
    public static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    public static final String ESTADO_ACTIVO = "ACTIVO";
    public static final String ESTADO_INACTIVO = "INACTIVO";

    private int idUsuario;
    private String username;
    private String passwordHash;
    private String rol;
    private String estado;

    public Usuario() {
    }

    public Usuario(int idUsuario, String username, String rol) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.rol = rol;
        this.estado = ESTADO_ACTIVO;
    }

    public boolean estaActivo() {
        return ESTADO_ACTIVO.equals(estado);
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
