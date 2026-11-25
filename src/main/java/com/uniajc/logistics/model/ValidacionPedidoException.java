package com.uniajc.logistics.model;

public class ValidacionPedidoException extends RuntimeException {
    public ValidacionPedidoException(String mensaje) {
        super(mensaje);
    }
}