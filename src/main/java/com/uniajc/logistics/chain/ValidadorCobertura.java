package com.uniajc.logistics.chain;

import com.uniajc.logistics.model.Pedido;
import com.uniajc.logistics.model.ValidacionPedidoException;

public class ValidadorCobertura extends ValidadorPedido {
    @Override
    public void validar(Pedido pedido) {
        if (!pedido.isTieneCobertura()) {
            throw new ValidacionPedidoException("La dirección de destino no tiene cobertura logística.");
        }
        validarSiguiente(pedido);
    }
}