package com.uniajc.logistics.observer;

import com.uniajc.logistics.model.Pedido;
import java.util.ArrayList;
import java.util.List;

public class BodegaListener implements ObservadorPedido {
    private final List<String> ordenesAlistamiento = new ArrayList<>();

    @Override
    public void alPagarPedido(Pedido pedido) {
        String orden = "[BODEGA] Orden de alistamiento generada para el pedido #" + pedido.getId();
        ordenesAlistamiento.add(orden);
        System.out.println(orden);
    }

    public List<String> getOrdenesAlistamiento() {
        return ordenesAlistamiento;
    }
}