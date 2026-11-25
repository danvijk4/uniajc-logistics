package com.uniajc.logistics.chain;

import com.uniajc.logistics.model.Pedido;
import com.uniajc.logistics.model.ValidacionPedidoException;

public class ValidadorStock extends ValidadorPedido {
    @Override
    public void validar(Pedido pedido) {
        if (pedido.getStockDisponible() < pedido.getCantidadSolicitada()) {
            throw new ValidacionPedidoException("Stock insuficiente para el pedido #" + pedido.getId());
        }
        validarSiguiente(pedido);
    }
}