package com.uniajc.logistics.observer;

import com.uniajc.logistics.model.Pedido;

public interface ObservadorPedido {
    default void alCambiarEstado(Pedido pedido, String estadoAnterior, String estadoNuevo) {}
    default void alPagarPedido(Pedido pedido) {}
}