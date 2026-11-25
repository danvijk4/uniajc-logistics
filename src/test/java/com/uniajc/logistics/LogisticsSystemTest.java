package com.uniajc.logistics;

import com.uniajc.logistics.chain.*;
import com.uniajc.logistics.model.Pedido;
import com.uniajc.logistics.model.TransicionInvalidaException;
import com.uniajc.logistics.model.ValidacionPedidoException;
import com.uniajc.logistics.observer.AuditoriaLogger;
import com.uniajc.logistics.observer.BodegaListener;
import com.uniajc.logistics.observer.ClienteNotificador;
import com.uniajc.logistics.strategy.EnvioEstandar;
import com.uniajc.logistics.strategy.EnvioExpress;
import com.uniajc.logistics.strategy.EnvioInternacional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.uniajc.logistics.strategy.EstrategiaEnvio;

import static org.junit.jupiter.api.Assertions.*;

class LogisticsSystemTest {

    private ValidadorPedido cadenaValidacion;

    @BeforeEach
    void setUp() {
        // Configuración de la cadena de responsabilidad
        cadenaValidacion = new ValidadorStock();
        cadenaValidacion
                .enlazarCon(new ValidadorMontoMinimo())
                .enlazarCon(new ValidadorFraude())
                .enlazarCon(new ValidadorCobertura());
    }

    @Test
    @DisplayName("1. Strategy: Cálculo correcto de tarifas para Estándar, Express e Internacional")
    void testCalculoTarifasStrategy() {
        Pedido pedido = new Pedido("PED-01", "Daniel", 200.0, 4.0, 2, 10, true, true, new EnvioEstandar());
        // Estándar: 10.0 + (4.0 * 2.5) = 20.0
        assertEquals(20.0, pedido.calcularCostoEnvio(), 0.01);

        // Cambio dinámico a Express: (10.0 * 1.8) + 15.0 = 33.0
        pedido.setEstrategiaEnvio(new EnvioExpress());
        assertEquals(33.0, pedido.calcularCostoEnvio(), 0.01);

        // Cambio dinámico a Internacional (tarifa zona 50): 50.0 + (200.0 * 0.15) = 80.0
        pedido.setEstrategiaEnvio(new EnvioInternacional(50.0));
        assertEquals(80.0, pedido.calcularCostoEnvio(), 0.01);
        assertEquals(280.0, pedido.calcularTotal(), 0.01);
    }

    @Test
    @DisplayName("2. State: Flujo completo válido desde Pendiente hasta Entregado")
    void testFlujoCompletoValidoState() {
        Pedido pedido = new Pedido("PED-02", "Daniel", 150.0, 2.0, 1, 5, true, true, new EnvioEstandar());
        assertEquals("Pendiente", pedido.getEstadoActual().getNombre());

        pedido.pagar();
        assertEquals("Pagado", pedido.getEstadoActual().getNombre());

        pedido.preparar();
        assertEquals("EnPreparacion", pedido.getEstadoActual().getNombre());

        pedido.enviar();
        assertEquals("Enviado", pedido.getEstadoActual().getNombre());

        pedido.entregar();
        assertEquals("Entregado", pedido.getEstadoActual().getNombre());
    }

    @Test
    @DisplayName("3. State: Bloqueo de transiciones inválidas lanzando TransicionInvalidaException")
    void testTransicionesInvalidasState() {
        Pedido pedido = new Pedido("PED-03", "Daniel", 150.0, 2.0, 1, 5, true, true, new EnvioEstandar());

        // No se puede enviar un pedido que sigue Pendiente
        assertThrows(TransicionInvalidaException.class, pedido::enviar);

        // Avanzar hasta Enviado e intentar cancelar (no permitido)
        pedido.pagar();
        pedido.preparar();
        pedido.enviar();
        assertThrows(TransicionInvalidaException.class, pedido::cancelar);

        // Entregar e intentar volver a pagar
        pedido.entregar();
        assertThrows(TransicionInvalidaException.class, pedido::pagar);
    }

    @Test
    @DisplayName("4. State: Cancelación válida antes del envío")
    void testCancelacionAntesDeEnvio() {
        Pedido pedido = new Pedido("PED-04", "Daniel", 120.0, 1.5, 1, 5, true, true, new EnvioEstandar());
        pedido.pagar();
        pedido.preparar();
        pedido.cancelar();

        assertEquals("Cancelado", pedido.getEstadoActual().getNombre());
        assertThrows(TransicionInvalidaException.class, pedido::enviar);
    }

    @Test
    @DisplayName("5. Observer: Notificación automática al cliente, bodega y auditoría")
    void testNotificacionesObserver() {
        Pedido pedido = new Pedido("PED-05", "Daniel", 300.0, 3.0, 2, 10, true, true, new EnvioExpress());
        ClienteNotificador clienteObs = new ClienteNotificador();
        BodegaListener bodegaObs = new BodegaListener();
        AuditoriaLogger auditoriaObs = new AuditoriaLogger();

        pedido.agregarObservador(clienteObs);
        pedido.agregarObservador(bodegaObs);
        pedido.agregarObservador(auditoriaObs);

        pedido.pagar();
        pedido.preparar();

        assertEquals(2, clienteObs.getNotificacionesEnviadas().size());
        assertEquals(1, bodegaObs.getOrdenesAlistamiento().size());
        assertEquals(2, auditoriaObs.getRegistros().size());
    }

    @Test
    @DisplayName("6. Chain of Responsibility: Aprobación de un pedido que cumple todas las reglas")
    void testCadenaValidacionExitosa() {
        Pedido pedidoValido = new Pedido("PED-06", "Daniel", 500.0, 2.0, 2, 10, true, true, new EnvioEstandar());
        assertDoesNotThrow(() -> cadenaValidacion.validar(pedidoValido));
    }

    @Test
    @DisplayName("7. Chain of Responsibility: Rechazo por stock insuficiente o monto menor al mínimo")
    void testCadenaValidacionFallaStockYMonto() {
        // Falla por stock (solicita 15, hay 5)
        Pedido sinStock = new Pedido("PED-07A", "Daniel", 200.0, 2.0, 15, 5, true, true, new EnvioEstandar());
        ValidacionPedidoException exStock = assertThrows(ValidacionPedidoException.class,
                () -> cadenaValidacion.validar(sinStock));
        assertTrue(exStock.getMessage().contains("Stock insuficiente"));

        // Falla por monto mínimo ($30 < $50)
        Pedido montoBajo = new Pedido("PED-07B", "Daniel", 30.0, 1.0, 1, 5, true, true, new EnvioEstandar());
        ValidacionPedidoException exMonto = assertThrows(ValidacionPedidoException.class,
                () -> cadenaValidacion.validar(montoBajo));
        assertTrue(exMonto.getMessage().contains("monto mínimo"));
    }

    @Test
    @DisplayName("8. Chain of Responsibility: Rechazo por alerta de fraude o falta de cobertura")
    void testCadenaValidacionFallaFraudeYCobertura() {
        // Falla por fraude (subtotal > $5000 sin historial)
        Pedido sospechoso = new Pedido("PED-08A", "Desconocido", 6000.0, 2.0, 1, 5, false, true, new EnvioExpress());
        ValidacionPedidoException exFraude = assertThrows(ValidacionPedidoException.class,
                () -> cadenaValidacion.validar(sospechoso));
        assertTrue(exFraude.getMessage().contains("fraude"));

        // Falla por cobertura (tieneCobertura = false)
        Pedido sinCobertura = new Pedido("PED-08B", "Daniel", 400.0, 2.0, 1, 5, true, false, new EnvioEstandar());
        ValidacionPedidoException exCobertura = assertThrows(ValidacionPedidoException.class,
                () -> cadenaValidacion.validar(sinCobertura));
        assertTrue(exCobertura.getMessage().contains("cobertura"));
    }

    @Test
    @DisplayName("9. OCP: Añadir una nueva estrategia de envío sin modificar Pedido ni clases existentes")
    void testPrincipioAbiertoCerradoNuevaEstrategia() {
        EstrategiaEnvio envioDron = new EstrategiaEnvio() {
            @Override
            public String getNombre() {
                return "Envío Dron Express";
            }

            @Override
            public double calcularCosto(Pedido pedido) {
                return 25.0 + (pedido.getPesoKg() * 5.0);
            }
        };

        Pedido pedido = new Pedido("PED-09", "Daniel", 300.0, 2.0, 1, 10, true, true, envioDron);

        assertEquals("Envío Dron Express", pedido.getEstrategiaEnvio().getNombre());
        assertEquals(35.0, pedido.calcularCostoEnvio(), 0.01);
        assertEquals(335.0, pedido.calcularTotal(), 0.01);
    }
}