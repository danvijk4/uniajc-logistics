package com.uniajc.logistics.state;

import com.uniajc.logistics.model.Pedido;

public class EstadoPagado implements EstadoPedido {
    @Override
    public String getNombre() {
        return "Pagado";
    }

    @Override
    public void preparar(Pedido pedido) {
        pedido.cambiarEstado(new EstadoEnPreparacion());
    }

    @Override
    public void cancelar(Pedido pedido) {
        pedido.cambiarEstado(new EstadoCancelado());
    }
}