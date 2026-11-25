package com.uniajc.logistics.strategy;

import com.uniajc.logistics.model.Pedido;

public class EnvioInternacional implements EstrategiaEnvio {
    private final double tarifaZona;
    private static final double PORCENTAJE_ARANCEL = 0.15;

    public EnvioInternacional(double tarifaZona) {
        this.tarifaZona = tarifaZona;
    }

    @Override
    public double calcularCosto(Pedido pedido) {
        return tarifaZona + (pedido.getSubtotal() * PORCENTAJE_ARANCEL);
    }

    @Override
    public String getNombre() {
        return "Envío Internacional";
    }
}