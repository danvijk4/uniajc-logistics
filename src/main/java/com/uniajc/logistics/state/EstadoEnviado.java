package com.uniajc.logistics.state;

import com.uniajc.logistics.model.Pedido;

public class EstadoEnviado implements EstadoPedido {
    @Override
    public String getNombre() {
        return "Enviado";
    }

    @Override
    public void entregar(Pedido pedido) {
        pedido.cambiarEstado(new EstadoEntregado());
    }
}