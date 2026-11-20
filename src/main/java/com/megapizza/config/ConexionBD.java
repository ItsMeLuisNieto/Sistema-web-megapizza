package com.megapizza.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fábrica de conexiones JDBC puras hacia BaseDatosMegapizza.
 *
 * La configuración puede sobreescribirse con variables de entorno o propiedades
 * del sistema (útil para desplegar en otro servidor sin recompilar):
 * DB_URL, DB_USER, DB_PASSWORD, DB_DRIVER.
 */
public final class ConexionBD {

    private static final String DRIVER = prop("DB_DRIVER", "com.mysql.cj.jdbc.Driver");
    private static final String URL = prop("DB_URL",
            "jdbc:mysql://localhost:3306/BaseDatosMegapizza"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
            + "&useUnicode=true&characterEncoding=UTF-8");
    private static final String USER = prop("DB_USER", "root");
    private static final String PASSWORD = prop("DB_PASSWORD", "");

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("No se encontró el driver JDBC: " + DRIVER);
        }
    }

    private ConexionBD() {
    }

    /**
     * Devuelve una conexión con auto-commit activo. Las operaciones que deben
     * ser atómicas desactivan el auto-commit en el servicio/DAO responsable.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String prop(String clave, String valorPorDefecto) {
        String valor = System.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            valor = System.getenv(clave);
        }
        return (valor == null || valor.isBlank()) ? valorPorDefecto : valor;
    }
}
