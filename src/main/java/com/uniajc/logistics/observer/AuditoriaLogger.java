package com.uniajc.logistics.observer;

import com.uniajc.logistics.model.Pedido;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditoriaLogger implements ObservadorPedido {
    private final List<String> registros = new ArrayList<>();

    @Override
    public void alCambiarEstado(Pedido pedido, String estadoAnterior, String estadoNuevo) {
        String log = "[" + LocalDateTime.now() + "] Pedido #" + pedido.getId()
                + ": " + estadoAnterior + " -> " + estadoNuevo;
        registros.add(log);
        System.out.println(log);
    }

    public List<String> getRegistros() {
        return registros;
    }
}
