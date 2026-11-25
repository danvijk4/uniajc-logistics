package com.uniajc.logistics.observer;

import com.uniajc.logistics.model.Pedido;
import java.util.ArrayList;
import java.util.List;

public class ClienteNotificador implements ObservadorPedido {
    private final List<String> notificacionesEnviadas = new ArrayList<>();

    @Override
    public void alCambiarEstado(Pedido pedido, String estadoAnterior, String estadoNuevo) {
        String mensaje = "[CLIENTE - " + pedido.getCliente() + "] Tu pedido #" + pedido.getId()
                + " cambió de " + estadoAnterior + " a " + estadoNuevo;
        notificacionesEnviadas.add(mensaje);
        System.out.println(mensaje);
    }

    public List<String> getNotificacionesEnviadas() {
        return notificacionesEnviadas;
    }
}