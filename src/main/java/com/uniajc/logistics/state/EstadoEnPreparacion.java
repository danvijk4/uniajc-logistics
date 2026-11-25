package com.uniajc.logistics.state;

import com.uniajc.logistics.model.Pedido;

public class EstadoEnPreparacion implements EstadoPedido {
    @Override
    public String getNombre() {
        return "EnPreparacion";
    }

    @Override
    public void enviar(Pedido pedido) {
        pedido.cambiarEstado(new EstadoEnviado());
    }

    @Override
    public void cancelar(Pedido pedido) {
        pedido.cambiarEstado(new EstadoCancelado());
    }
}