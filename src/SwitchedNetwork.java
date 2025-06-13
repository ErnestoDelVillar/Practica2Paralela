import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SwitchedNetwork implements NetworkTopology {
    private List<Node> nodes;
    private ExecutorService executor;
    private Map<Integer, Set<Integer>> switchConnections;
    private int numberOfMessages;
    private CountDownLatch messageLatch;

    @Override
    public void configureNetwork(int numberOfNodes, int numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
        nodes = new ArrayList<>();
        executor = Executors.newFixedThreadPool(numberOfNodes);
        switchConnections = new HashMap<>();
        messageLatch = new CountDownLatch(numberOfMessages);

        System.out.println("Conmutada: Configurando " + numberOfNodes + " nodos, " + numberOfMessages + " mensajes");

        for (int i = 0; i < numberOfNodes; i++) {
            nodes.add(new Node(i, true, messageLatch));
            switchConnections.put(i, new HashSet<>());
            System.out.println("Conmutada: Nodo creado " + i);
        }

        Random random = new Random();
        for (Node node : nodes) {
            int conexiones = random.nextInt(numberOfNodes / 2) + 1;
            for (int i = 0; i < conexiones; i++) {
                int vecinoId = random.nextInt(numberOfNodes);
                if (vecinoId != node.getId() && !switchConnections.get(node.getId()).contains(vecinoId)) {
                    node.addNeighbor(nodes.get(vecinoId));
                    switchConnections.get(node.getId()).add(vecinoId);
                    nodes.get(vecinoId).addNeighbor(node);
                    switchConnections.get(vecinoId).add(node.getId());
                    System.out.println("Conmutada: Nodo " + node.getId() + " conectado con nodo " + vecinoId);
                }
            }
        }
    }

    @Override
    public void sendMessage(int from, int to, String message) {
        if (from < 0 || from >= nodes.size() || to < 0 || to >= nodes.size()) {
            System.out.println("Conmutada: IDs de nodo inválidos: desde=" + from + ", hasta=" + to);
            return;
        }
        System.out.println("Conmutada: Enviando mensaje de " + from + " a " + to + ": " + message);
        executor.submit(() -> {
            nodes.get(from).receiveMessage(new Mensaje(from, to, message, false, 5));
        });
    }

    @Override
    public void runNetwork() {
        System.out.println("Conmutada: Ejecutando red");

        for (Node node : nodes) {
            executor.submit(() -> {
                System.out.println("Conmutada: Iniciando nodo " + node.getId());
                node.run();
                System.out.println("Conmutada: Nodo " + node.getId() + " finalizado, procesó " + node.getMessagesProcessed() + " mensajes");
            });
        }

        try {
            System.out.println("Conmutada: Esperando mensajes, cuenta del latch: " + messageLatch.getCount());
            boolean completado = messageLatch.await(60, TimeUnit.SECONDS);
            System.out.println("Conmutada: Latch completado: " + completado + ", cuenta restante: " + messageLatch.getCount());
        } catch (InterruptedException e) {
            System.out.println("Conmutada: Interrumpido mientras esperaba el latch");
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void shutdown() {
        System.out.println("Conmutada: Apagando");
        nodes.forEach(node -> {
            node.stop();
            System.out.println("Conmutada: Nodo detenido " + node.getId());
        });
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.out.println("Conmutada: Apagado forzado");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.out.println("Conmutada: Interrumpido durante apagado");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Conmutada: Apagado completo");
    }
}
