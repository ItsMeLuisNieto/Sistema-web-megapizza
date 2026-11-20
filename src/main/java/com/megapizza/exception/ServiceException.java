package com.megapizza.exception;

import java.io.Serial;

/**
 * Error de validación o de negocio de la capa de servicios.
 *
 * Se propaga hasta el controlador, que decide cómo mostrarlo en la vista;
 * nunca se usa para errores técnicos de JDBC, que se envuelven con su causa.
 */
public class ServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ServiceException(String mensaje) {
        super(mensaje);
    }

    public ServiceException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public ServiceException(Throwable causa) {
        super(causa);
    }
}
