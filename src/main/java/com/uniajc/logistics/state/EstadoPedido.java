package com.uniajc.logistics.state;

import com.uniajc.logistics.model.Pedido;
import com.uniajc.logistics.model.TransicionInvalidaException;

public interface EstadoPedido {
    String getNombre();

    default void pagar(Pedido pedido) {
        throw new TransicionInvalidaException("No se puede pagar un pedido en estado: " + getNombre());
    }

    default void preparar(Pedido pedido) {
        throw new TransicionInvalidaException("No se puede preparar un pedido en estado: " + getNombre());
    }

    default void enviar(Pedido pedido) {
        throw new TransicionInvalidaException("No se puede enviar un pedido en estado: " + getNombre());
    }

    default void entregar(Pedido pedido) {
        throw new TransicionInvalidaException("No se puede entregar un pedido en estado: " + getNombre());
    }

    default void cancelar(Pedido pedido) {
        throw new TransicionInvalidaException("No se puede cancelar un pedido en estado: " + getNombre());
    }
}