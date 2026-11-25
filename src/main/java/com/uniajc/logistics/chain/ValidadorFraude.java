package com.uniajc.logistics.chain;

import com.uniajc.logistics.model.Pedido;
import com.uniajc.logistics.model.ValidacionPedidoException;

public class ValidadorFraude extends ValidadorPedido {
    private static final double UMBRAL_RIESGO = 5000.0;

    @Override
    public void validar(Pedido pedido) {
        if (pedido.getSubtotal() > UMBRAL_RIESGO && !pedido.isClienteConHistorial()) {
            throw new ValidacionPedidoException("Alerta de fraude: monto elevado en cliente sin historial.");
        }
        validarSiguiente(pedido);
    }
}