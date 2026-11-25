package com.uniajc.logistics.strategy;

import com.uniajc.logistics.model.Pedido;

public class EnvioEstandar implements EstrategiaEnvio {
    private static final double TARIFA_BASE = 10.0;
    private static final double COSTO_POR_KG = 2.5;

    @Override
    public double calcularCosto(Pedido pedido) {
        return TARIFA_BASE + (pedido.getPesoKg() * COSTO_POR_KG);
    }

    @Override
    public String getNombre() {
        return "Envío Estándar";
    }
}