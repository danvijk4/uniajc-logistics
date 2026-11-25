package com.uniajc.logistics.strategy;

import com.uniajc.logistics.model.Pedido;

public interface EstrategiaEnvio {
    double calcularCosto(Pedido pedido);
    String getNombre();
}