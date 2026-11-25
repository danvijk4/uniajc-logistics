package com.uniajc.logistics.state;

public class EstadoEntregado implements EstadoPedido {
    @Override
    public String getNombre() {
        return "Entregado";
    }
}