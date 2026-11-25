package com.uniajc.logistics.state;

import com.uniajc.logistics.model.Pedido;

public class EstadoPendiente implements EstadoPedido {
    @Override
    public String getNombre() {
        return "Pendiente";
    }

    @Override
    public void pagar(Pedido pedido) {
        pedido.cambiarEstado(new EstadoPagado());
        pedido.notificarPagoBodega();
    }

    @Override
    public void cancelar(Pedido pedido) {
        pedido.cambiarEstado(new EstadoCancelado());
    }
}