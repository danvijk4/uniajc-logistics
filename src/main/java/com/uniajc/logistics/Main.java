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
import com.uniajc.logistics.strategy.EstrategiaEnvio;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class Main {

    private static final List<Pedido> pedidos = new ArrayList<>();
    private static ValidadorPedido cadenaValidacion;
    private static ClienteNotificador clienteObs;
    private static BodegaListener bodegaObs;
    private static AuditoriaLogger auditoriaObs;

    // Registro polimórfico de estrategias (CERO if/else y CERO switch)
    private static final Map<String, Function<Double, EstrategiaEnvio>> REGISTRO_ESTRATEGIAS = Map.of(
            "1", tarifa -> new EnvioEstandar(),
            "ESTANDAR", tarifa -> new EnvioEstandar(),
            "2", tarifa -> new EnvioExpress(),
            "EXPRESS", tarifa -> new EnvioExpress(),
            "3", EnvioInternacional::new,
            "INTERNACIONAL", EnvioInternacional::new
    );

    // Registro polimórfico de acciones de estado (CERO switch)
    private static final Map<String, AccionEstado> ACCIONES_ESTADO = Map.of(
            "1", Pedido::pagar,
            "2", Pedido::preparar,
            "3", Pedido::enviar,
            "4", Pedido::entregar,
            "5", Pedido::cancelar
    );

    @FunctionalInterface
    private interface AccionEstado {
        void ejecutar(Pedido p) throws TransicionInvalidaException;
    }

    public static void main(String[] args) {
        inicializarSistema();
        Scanner scanner = new Scanner(System.in);

        Map<String, Runnable> menuAcciones = Map.of(
                "1", () -> cargarDesdeCSV(scanner),
                "2", () -> crearPedidoManual(scanner),
                "3", Main::listarPedidos,
                "4", () -> validarPedido(scanner),
                "5", () -> cambiarEstrategiaEnvio(scanner),
                "6", () -> gestionarEstadoPedido(scanner),
                "7", Main::mostrarHistorialObservers
        );

        String opcion = "";
        while (!opcion.equals("0")) {
            mostrarMenu();
            opcion = scanner.nextLine().trim();
            Runnable accion = menuAcciones.get(opcion);
            while (accion != null) {
                accion.run();
                break;
            }
        }
        System.out.println("Saliendo del sistema logístico...");
        scanner.close();
    }

    private static void inicializarSistema() {
        cadenaValidacion = new ValidadorStock();
        cadenaValidacion
                .enlazarCon(new ValidadorMontoMinimo())
                .enlazarCon(new ValidadorFraude())
                .enlazarCon(new ValidadorCobertura());

        clienteObs = new ClienteNotificador();
        bodegaObs = new BodegaListener();
        auditoriaObs = new AuditoriaLogger();
    }

    private static void mostrarMenu() {
        System.out.println("\n==========================================");
        System.out.println("   SISTEMA LOGÍSTICO UNIAJC - MENÚ");
        System.out.println("==========================================");
        System.out.println("1. Cargar pedidos desde archivo CSV");
        System.out.println("2. Crear nuevo pedido manualmente");
        System.out.println("3. Listar pedidos y calcular costos");
        System.out.println("4. Validar pedido (Chain of Responsibility)");
        System.out.println("5. Cambiar estrategia de envío (Strategy)");
        System.out.println("6. Cambiar estado del pedido (State + Observer)");
        System.out.println("7. Ver historial de registros (Observer)");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
    }

    private static void cargarDesdeCSV(Scanner scanner) {
        System.out.print("Nombre del archivo CSV (Enter para 'pedidos.csv'): ");
        String entrada = scanner.nextLine().trim();
        String ruta = entrada.isEmpty() ? "pedidos.csv" : entrada;

        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            List<String> lineas = br.lines().skip(1).filter(l -> !l.trim().isEmpty()).toList();
            for (String linea : lineas) {
                String[] datos = linea.split(",");
                String id = datos[0].trim();
                String cliente = datos[1].trim();
                double subtotal = Double.parseDouble(datos[2].trim());
                double pesoKg = Double.parseDouble(datos[3].trim());
                int cantSolicitada = Integer.parseInt(datos[4].trim());
                int stockDisp = Integer.parseInt(datos[5].trim());
                boolean historial = Boolean.parseBoolean(datos[6].trim());
                boolean cobertura = Boolean.parseBoolean(datos[7].trim());
                String tipoEnvio = datos[8].trim().toUpperCase();
                double tarifaZona = Double.parseDouble(datos[9].trim());

                EstrategiaEnvio estrategia = resolverEstrategia(tipoEnvio, tarifaZona);
                Pedido p = new Pedido(id, cliente, subtotal, pesoKg, cantSolicitada, stockDisp, historial, cobertura, estrategia);
                registrarObservadores(p);
                pedidos.add(p);
            }
            System.out.println("Se cargaron " + lineas.size() + " pedidos exitosamente desde '" + ruta + "'.");
        } catch (IOException | RuntimeException e) {
            System.out.println("Error al leer el archivo CSV: " + e.getMessage());
        }
    }

    private static void crearPedidoManual(Scanner scanner) {
        try {
            System.out.print("ID del pedido (ej. PED-200): ");
            String id = scanner.nextLine().trim();
            System.out.print("Nombre del cliente: ");
            String cliente = scanner.nextLine().trim();
            System.out.print("Subtotal ($): ");
            double subtotal = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Peso (kg): ");
            double peso = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Cantidad solicitada: ");
            int cantidad = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Stock disponible: ");
            int stock = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("¿Tiene historial válido? (true/false): ");
            boolean historial = Boolean.parseBoolean(scanner.nextLine().trim());
            System.out.print("¿Tiene cobertura de entrega? (true/false): ");
            boolean cobertura = Boolean.parseBoolean(scanner.nextLine().trim());

            System.out.print("Tipo de envío (1=Estándar, 2=Express, 3=Internacional): ");
            String tipo = scanner.nextLine().trim();
            System.out.print("Tarifa base de zona (0 si es nacional): ");
            double zona = Double.parseDouble(scanner.nextLine().trim());

            EstrategiaEnvio estrategia = resolverEstrategia(tipo, zona);
            Pedido nuevo = new Pedido(id, cliente, subtotal, peso, cantidad, stock, historial, cobertura, estrategia);
            registrarObservadores(nuevo);
            pedidos.add(nuevo);
            System.out.println("Pedido #" + id + " creado correctamente con estrategia " + estrategia.getNombre() + ".");
        } catch (NumberFormatException e) {
            System.out.println("Error: Dato numérico inválido.");
        }
    }

    private static void listarPedidos() {
        System.out.println("\n--- LISTADO DE PEDIDOS (" + pedidos.size() + ") ---");
        for (Pedido p : pedidos) {
            System.out.printf("ID: %s | Cliente: %s | Estado: %s | Estrategia: %s | Subtotal: $%.2f | Envío: $%.2f | Total: $%.2f%n",
                    p.getId(),
                    p.getCliente(),
                    p.getEstadoActual().getNombre(),
                    p.getEstrategiaEnvio().getNombre(),
                    p.getSubtotal(),
                    p.calcularCostoEnvio(),
                    p.calcularTotal());
        }
    }

    private static void validarPedido(Scanner scanner) {
        buscarPedido(scanner).ifPresent(p -> {
            try {
                cadenaValidacion.validar(p);
                System.out.println("[OK] El pedido #" + p.getId() + " superó todas las validaciones.");
            } catch (ValidacionPedidoException e) {
                System.out.println("[RECHAZADO] " + e.getMessage());
            }
        });
    }

    private static void cambiarEstrategiaEnvio(Scanner scanner) {
        buscarPedido(scanner).ifPresent(p -> {
            System.out.println("Estrategia actual: " + p.getEstrategiaEnvio().getNombre() + " ($" + p.calcularCostoEnvio() + ")");
            System.out.print("Nueva estrategia (1=Estándar, 2=Express, 3=Internacional): ");
            String tipo = scanner.nextLine().trim();
            System.out.print("Tarifa base de zona (0 si es nacional): ");
            try {
                double zona = Double.parseDouble(scanner.nextLine().trim());
                p.setEstrategiaEnvio(resolverEstrategia(tipo, zona));
                System.out.printf("Estrategia actualizada a %s. Nuevo costo de envío: $%.2f | Total: $%.2f%n",
                        p.getEstrategiaEnvio().getNombre(), p.calcularCostoEnvio(), p.calcularTotal());
            } catch (NumberFormatException e) {
                System.out.println("Entrada numérica inválida.");
            }
        });
    }

    private static void gestionarEstadoPedido(Scanner scanner) {
        buscarPedido(scanner).ifPresent(p -> {
            System.out.println("Estado actual de #" + p.getId() + ": " + p.getEstadoActual().getNombre());
            System.out.println("Acción: 1=Pagar | 2=Preparar | 3=Enviar | 4=Entregar | 5=Cancelar");
            System.out.print("Seleccione acción: ");
            String accionKey = scanner.nextLine().trim();
            try {
                AccionEstado accion = ACCIONES_ESTADO.getOrDefault(accionKey, ped -> System.out.println("Acción no válida."));
                accion.ejecutar(p);
            } catch (TransicionInvalidaException e) {
                System.out.println("[TRANSICIÓN BLOQUEADA] " + e.getMessage());
            }
        });
    }

    private static void mostrarHistorialObservers() {
        System.out.println("\n--- AUDITORÍA DEL SISTEMA (" + auditoriaObs.getRegistros().size() + " eventos) ---");
        auditoriaObs.getRegistros().forEach(System.out::println);

        System.out.println("\n--- ÓRDENES EN BODEGA (" + bodegaObs.getOrdenesAlistamiento().size() + " órdenes) ---");
        bodegaObs.getOrdenesAlistamiento().forEach(System.out::println);
    }

    private static Optional<Pedido> buscarPedido(Scanner scanner) {
        System.out.print("Ingrese el ID del pedido (ej. PED-101): ");
        String id = scanner.nextLine().trim();
        Optional<Pedido> encontrado = pedidos.stream()
                .filter(p -> p.getId().equalsIgnoreCase(id))
                .findFirst();
        encontrado.ifPresentOrElse(
                p -> {},
                () -> System.out.println("No se encontró ningún pedido con ID: " + id)
        );
        return encontrado;
    }

    private static EstrategiaEnvio resolverEstrategia(String clave, double tarifaZona) {
        return REGISTRO_ESTRATEGIAS
                .getOrDefault(clave.toUpperCase(), tarifa -> new EnvioEstandar())
                .apply(tarifaZona);
    }

    private static void registrarObservadores(Pedido p) {
        p.agregarObservador(clienteObs);
        p.agregarObservador(bodegaObs);
        p.agregarObservador(auditoriaObs);
    }
}