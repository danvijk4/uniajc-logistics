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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final List<Pedido> pedidos = new ArrayList<>();
    private static ValidadorPedido cadenaValidacion;
    private static ClienteNotificador clienteObs;
    private static BodegaListener bodegaObs;
    private static AuditoriaLogger auditoriaObs;

    public static void main(String[] args) {
        inicializarSistema();
        Scanner scanner = new Scanner(System.in);
        int opcion = -1;

        while (opcion != 0) {
            mostrarMenu();
            try {
                opcion = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Ingresa un número del menú.\n");
                continue;
            }

            switch (opcion) {
                case 1 -> cargarDesdeCSV(scanner);
                case 2 -> crearPedidoManual(scanner);
                case 3 -> listarPedidos();
                case 4 -> validarPedido(scanner);
                case 5 -> cambiarEstrategiaEnvio(scanner);
                case 6 -> gestionarEstadoPedido(scanner);
                case 7 -> mostrarHistorialObservers();
                case 0 -> System.out.println("Saliendo del sistema logístico...");
                default -> System.out.println("Opción no válida.\n");
            }
        }
        scanner.close();
    }

    private static void inicializarSistema() {
        // Chain of Responsibility
        cadenaValidacion = new ValidadorStock();
        cadenaValidacion
                .enlazarCon(new ValidadorMontoMinimo())
                .enlazarCon(new ValidadorFraude())
                .enlazarCon(new ValidadorCobertura());

        // Observers globales
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
        String ruta = scanner.nextLine().trim();
        if (ruta.isEmpty()) {
            ruta = "pedidos.csv";
        }

        int cargados = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            boolean primeraLinea = true;
            while ((linea = br.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue; // Saltar encabezado
                }
                if (linea.trim().isEmpty()) continue;

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

                EstrategiaEnvio estrategia = crearEstrategia(tipoEnvio, tarifaZona);
                Pedido p = new Pedido(id, cliente, subtotal, pesoKg, cantSolicitada, stockDisp, historial, cobertura, estrategia);
                registrarObservadores(p);
                pedidos.add(p);
                cargados++;
            }
            System.out.println("Se cargaron " + cargados + " pedidos exitosamente desde '" + ruta + "'.");
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
            int tipo = Integer.parseInt(scanner.nextLine().trim());
            EstrategiaEnvio estrategia;
            if (tipo == 2) {
                estrategia = new EnvioExpress();
            } else if (tipo == 3) {
                System.out.print("Tarifa base de zona internacional ($): ");
                double zona = Double.parseDouble(scanner.nextLine().trim());
                estrategia = new EnvioInternacional(zona);
            } else {
                estrategia = new EnvioEstandar();
            }

            Pedido nuevo = new Pedido(id, cliente, subtotal, peso, cantidad, stock, historial, cobertura, estrategia);
            registrarObservadores(nuevo);
            pedidos.add(nuevo);
            System.out.println("Pedido #" + id + " creado correctamente.");
        } catch (NumberFormatException e) {
            System.out.println("Error: Dato numérico inválido. Intenta nuevamente.");
        }
    }

    private static void listarPedidos() {
        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos registrados. Carga el CSV o crea uno manualmente.");
            return;
        }
        System.out.println("\n--- LISTADO DE PEDIDOS ---");
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
        Pedido p = buscarPedido(scanner);
        if (p == null) return;

        try {
            cadenaValidacion.validar(p);
            System.out.println("[OK] El pedido #" + p.getId() + " superó todas las validaciones (Stock, Monto, Fraude y Cobertura).");
        } catch (ValidacionPedidoException e) {
            System.out.println("[RECHAZADO] " + e.getMessage());
        }
    }

    private static void cambiarEstrategiaEnvio(Scanner scanner) {
        Pedido p = buscarPedido(scanner);
        if (p == null) return;

        System.out.println("Estrategia actual: " + p.getEstrategiaEnvio().getNombre() + " ($" + p.calcularCostoEnvio() + ")");
        System.out.print("Nueva estrategia (1=Estándar, 2=Express, 3=Internacional): ");
        try {
            int tipo = Integer.parseInt(scanner.nextLine().trim());
            if (tipo == 1) {
                p.setEstrategiaEnvio(new EnvioEstandar());
            } else if (tipo == 2) {
                p.setEstrategiaEnvio(new EnvioExpress());
            } else if (tipo == 3) {
                System.out.print("Tarifa base de zona internacional ($): ");
                double zona = Double.parseDouble(scanner.nextLine().trim());
                p.setEstrategiaEnvio(new EnvioInternacional(zona));
            } else {
                System.out.println("Opción no válida.");
                return;
            }
            System.out.printf("Estrategia actualizada a %s. Nuevo costo de envío: $%.2f | Total: $%.2f%n",
                    p.getEstrategiaEnvio().getNombre(), p.calcularCostoEnvio(), p.calcularTotal());
        } catch (NumberFormatException e) {
            System.out.println("Entrada numérica inválida.");
        }
    }

    private static void gestionarEstadoPedido(Scanner scanner) {
        Pedido p = buscarPedido(scanner);
        if (p == null) return;

        System.out.println("Estado actual de #" + p.getId() + ": " + p.getEstadoActual().getNombre());
        System.out.println("Acción: 1=Pagar | 2=Preparar | 3=Enviar | 4=Entregar | 5=Cancelar");
        System.out.print("Seleccione acción: ");
        try {
            int accion = Integer.parseInt(scanner.nextLine().trim());
            switch (accion) {
                case 1 -> p.pagar();
                case 2 -> p.preparar();
                case 3 -> p.enviar();
                case 4 -> p.entregar();
                case 5 -> p.cancelar();
                default -> System.out.println("Acción no válida.");
            }
        } catch (TransicionInvalidaException e) {
            System.out.println("[TRANSICIÓN BLOQUEADA] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida.");
        }
    }

    private static void mostrarHistorialObservers() {
        System.out.println("\n--- AUDITORÍA DEL SISTEMA (" + auditoriaObs.getRegistros().size() + " eventos) ---");
        auditoriaObs.getRegistros().forEach(System.out::println);

        System.out.println("\n--- ÓRDENES EN BODEGA (" + bodegaObs.getOrdenesAlistamiento().size() + " órdenes) ---");
        bodegaObs.getOrdenesAlistamiento().forEach(System.out::println);
    }

    private static Pedido buscarPedido(Scanner scanner) {
        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos cargados.");
            return null;
        }
        System.out.print("Ingrese el ID del pedido (ej. PED-101): ");
        String id = scanner.nextLine().trim();
        for (Pedido p : pedidos) {
            if (p.getId().equalsIgnoreCase(id)) {
                return p;
            }
        }
        System.out.println("No se encontró ningún pedido con ID: " + id);
        return null;
    }

    private static EstrategiaEnvio crearEstrategia(String tipo, double tarifaZona) {
        return switch (tipo) {
            case "EXPRESS" -> new EnvioExpress();
            case "INTERNACIONAL" -> new EnvioInternacional(tarifaZona);
            default -> new EnvioEstandar();
        };
    }

    private static void registrarObservadores(Pedido p) {
        p.agregarObservador(clienteObs);
        p.agregarObservador(bodegaObs);
        p.agregarObservador(auditoriaObs);
    }
}