package com.uniajc.logistics.chain;

import com.uniajc.logistics.model.Pedido;
import java.util.Optional;

public abstract class ValidadorPedido {
    protected ValidadorPedido siguiente;

    public ValidadorPedido enlazarCon(ValidadorPedido siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    public abstract void validar(Pedido pedido);

    protected void validarSiguiente(Pedido pedido) {
        Optional.ofNullable(siguiente).ifPresent(s -> s.validar(pedido));
    }
}