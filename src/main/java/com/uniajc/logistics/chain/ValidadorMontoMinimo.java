package com.uniajc.logistics.chain;

import com.uniajc.logistics.model.Pedido;
import com.uniajc.logistics.model.ValidacionPedidoException;

public class ValidadorMontoMinimo extends ValidadorPedido {
    private static final double MONTO_MINIMO = 50.0;

    @Override
    public void validar(Pedido pedido) {
        if (pedido.getSubtotal() < MONTO_MINIMO) {
            throw new ValidacionPedidoException("El subtotal no cumple con el monto mínimo de $" + MONTO_MINIMO);
        }
        validarSiguiente(pedido);
    }
}