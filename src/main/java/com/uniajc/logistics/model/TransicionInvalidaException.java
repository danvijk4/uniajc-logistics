package com.uniajc.logistics.model;

public class TransicionInvalidaException extends RuntimeException {
    public TransicionInvalidaException(String mensaje) {
        super(mensaje);
    }
}