package com.uniajc.logistics.model;

import com.uniajc.logistics.observer.ObservadorPedido;
import com.uniajc.logistics.state.EstadoPedido;
import com.uniajc.logistics.state.EstadoPendiente;
import com.uniajc.logistics.strategy.EstrategiaEnvio;
import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private final String id;
    private final String cliente;
    private final double subtotal;
    private final double pesoKg;
    private final int cantidadSolicitada;
    private final int stockDisponible;
    private final boolean clienteConHistorial;
    private final boolean tieneCobertura;

    private EstadoPedido estadoActual;
    private EstrategiaEnvio estrategiaEnvio;
    private final List<ObservadorPedido> observadores = new ArrayList<>();

    public Pedido(String id, String cliente, double subtotal, double pesoKg,
                  int cantidadSolicitada, int stockDisponible,
                  boolean clienteConHistorial, boolean tieneCobertura,
                  EstrategiaEnvio estrategiaEnvio) {
        this.id = id;
        this.cliente = cliente;
        this.subtotal = subtotal;
        this.pesoKg = pesoKg;
        this.cantidadSolicitada = cantidadSolicitada;
        this.stockDisponible = stockDisponible;
        this.clienteConHistorial = clienteConHistorial;
        this.tieneCobertura = tieneCobertura;
        this.estrategiaEnvio = estrategiaEnvio;
        this.estadoActual = new EstadoPendiente();
    }

    // Gestión de Observadores
    public void agregarObservador(ObservadorPedido observador) {
        observadores.add(observador);
    }

    public void cambiarEstado(EstadoPedido nuevoEstado) {
        String anterior = this.estadoActual.getNombre();
        this.estadoActual = nuevoEstado;
        for (ObservadorPedido obs : observadores) {
            obs.alCambiarEstado(this, anterior, nuevoEstado.getNombre());
        }
    }

    public void notificarPagoBodega() {
        for (ObservadorPedido obs : observadores) {
            obs.alPagarPedido(this);
        }
    }

    // Acciones del ciclo de vida (Patrón State)
    public void pagar() { estadoActual.pagar(this); }
    public void preparar() { estadoActual.preparar(this); }
    public void enviar() { estadoActual.enviar(this); }
    public void entregar() { estadoActual.entregar(this); }
    public void cancelar() { estadoActual.cancelar(this); }

    // Cálculo dinámico con Strategy
    public void setEstrategiaEnvio(EstrategiaEnvio nuevaEstrategia) {
        this.estrategiaEnvio = nuevaEstrategia;
    }

    public double calcularCostoEnvio() {
        return estrategiaEnvio.calcularCosto(this);
    }

    public double calcularTotal() {
        return subtotal + calcularCostoEnvio();
    }

    // Getters
    public String getId() { return id; }
    public String getCliente() { return cliente; }
    public double getSubtotal() { return subtotal; }
    public double getPesoKg() { return pesoKg; }
    public int getCantidadSolicitada() { return cantidadSolicitada; }
    public int getStockDisponible() { return stockDisponible; }
    public boolean isClienteConHistorial() { return clienteConHistorial; }
    public boolean isTieneCobertura() { return tieneCobertura; }
    public EstadoPedido getEstadoActual() { return estadoActual; }
    public EstrategiaEnvio getEstrategiaEnvio() { return estrategiaEnvio; }
}