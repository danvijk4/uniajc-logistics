package com.uniajc.logistics.state;

public class EstadoCancelado implements EstadoPedido {
    @Override
    public String getNombre() {
        return "Cancelado";
    }
}