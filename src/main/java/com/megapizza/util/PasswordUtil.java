package com.megapizza.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hash de contraseñas almacenado en {@code tb_usuario.password_hash} (CHAR(64)).
 *
 * El esquema fija 64 caracteres hex, por lo que se mantiene SHA-256 con un
 * prefijo de aplicación; la comparación es de tiempo constante para no filtrar
 * el hash almacenado. Para cambiar a un KDF con sal habría que migrar primero
 * el esquema y los datos de prueba (Bloque 1).
 */
public final class PasswordUtil {

    private static final String PREFIJO = "megapizza:";
    private static final int LONGITUD_HASH = 64;

    private PasswordUtil() {
    }

    /** Devuelve el hash hex de 64 caracteres que espera {@code tb_usuario}. */
    public static String hash(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((PREFIJO + password).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    /** Compara en tiempo constante; un hash corrupto se trata como credencial inválida. */
    public static boolean verificar(String password, String hashAlmacenado) {
        if (password == null || password.isBlank() || !esHashValido(hashAlmacenado)) {
            return false;
        }
        String normalizado = hashAlmacenado.trim();
        return MessageDigest.isEqual(
                hash(password).getBytes(StandardCharsets.US_ASCII),
                normalizado.getBytes(StandardCharsets.US_ASCII));
    }

    /** Valida el formato del valor guardado en {@code password_hash}. */
    public static boolean esHashValido(String hash) {
        if (hash == null || hash.trim().length() != LONGITUD_HASH) {
            return false;
        }
        String valor = hash.trim();
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            if ((c < '0' || c > '9') && (c < 'a' || c > 'f')) {
                return false;
            }
        }
        return true;
    }
}
