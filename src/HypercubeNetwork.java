import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Clase que representa una red de tipo hipercubo
public class HypercubeNetwork implements NetworkTopology {
    private List<Node> nodos;
    private ExecutorService ejecutor;
    private int dimension;
    private int cantidadMensajes;
    private CountDownLatch latchMensajes;

    @Override
    public void configureNetwork(int cantidadNodos, int cantidadMensajes) {
        this.cantidadMensajes = cantidadMensajes;
        dimension = (int) (Math.log(cantidadNodos) / Math.log(2));
        if (Math.pow(2, dimension) != cantidadNodos) {
            throw new IllegalArgumentException("La cantidad de nodos debe ser una potencia de 2 para Hipercubo. Recibido: " + cantidadNodos);
        }

        nodos = new ArrayList<>();
        ejecutor = Executors.newFixedThreadPool(cantidadNodos);
        latchMensajes = new CountDownLatch(cantidadMensajes);

        System.out.println("Hipercubo: Configurando " + cantidadNodos + " nodos, " + cantidadMensajes + " mensajes");

        for (int i = 0; i < cantidadNodos; i++) {
            nodos.add(new Node(i, false, latchMensajes));
            System.out.println("Hipercubo: Nodo creado " + i);
        }

        for (int i = 0; i < cantidadNodos; i++) {
            for (int j = 0; j < dimension; j++) {
                int idVecino = i ^ (1 << j);
                nodos.get(i).addNeighbor(nodos.get(idVecino));
                System.out.println("Hipercubo: Nodo " + i + " conectado con nodo " + idVecino);
            }
        }
    }

    @Override
    public void sendMessage(int desde, int hacia, String mensaje) {
        if (desde < 0 || desde >= nodos.size() || hacia < 0 || hacia >= nodos.size()) {
            System.out.println("Hipercubo: IDs de nodo inválidos: desde=" + desde + ", hacia=" + hacia);
            return;
        }

        System.out.println("Hipercubo: Enviando mensaje de " + desde + " a " + hacia + ": " + mensaje);
        ejecutor.submit(() -> {
            nodos.get(hacia).receiveMessage(new Mensaje(desde, hacia, mensaje, false, 5));
        });
    }

    @Override
    public void runNetwork() {
        System.out.println("Hipercubo: Ejecutando red");
        for (Node nodo : nodos) {
            ejecutor.submit(() -> {
                System.out.println("Hipercubo: Iniciando nodo " + nodo.getId());
                nodo.run();
                System.out.println("Hipercubo: Nodo " + nodo.getId() + " finalizado, procesó " + nodo.getMessagesProcessed() + " mensajes");
            });
        }

        try {
            System.out.println("Hipercubo: Esperando mensajes, latch restante: " + latchMensajes.getCount());
            boolean completado = latchMensajes.await(60, TimeUnit.SECONDS);
            System.out.println("Hipercubo: Latch completado: " + completado + ", restante: " + latchMensajes.getCount());
        } catch (InterruptedException e) {
            System.out.println("Hipercubo: Interrumpido mientras esperaba latch");
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void shutdown() {
        System.out.println("Hipercubo: Apagando red");
        nodos.forEach(nodo -> {
            nodo.stop();
            System.out.println("Hipercubo: Nodo detenido " + nodo.getId());
        });
        ejecutor.shutdown();
        try {
            if (!ejecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.out.println("Hipercubo: apagado forzado");
                ejecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.out.println("Hipercubo: Interrumpido durante apagado");
            ejecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Hipercubo: Apagado completado");
    }
}
