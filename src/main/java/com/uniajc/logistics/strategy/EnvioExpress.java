package com.uniajc.logistics.strategy;

import com.uniajc.logistics.model.Pedido;

public class EnvioExpress implements EstrategiaEnvio {
    private static final double TARIFA_BASE = 10.0;
    private static final double MULTIPLICADOR = 1.8;
    private static final double PRIORIDAD_DESPACHO = 15.0;

    @Override
    public double calcularCosto(Pedido pedido) {
        return (TARIFA_BASE * MULTIPLICADOR) + PRIORIDAD_DESPACHO;
    }

    @Override
    public String getNombre() {
        return "Envío Express";
    }
}