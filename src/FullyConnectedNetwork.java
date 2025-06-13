import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FullyConnectedNetwork implements NetworkTopology {
    private List<Node> nodes;
    private ExecutorService executor;
    private int numberOfMessages;
    private CountDownLatch messageLatch;

    @Override
    public void configureNetwork(int numberOfNodes, int numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
        nodes = new ArrayList<>();
        executor = Executors.newFixedThreadPool(numberOfNodes);
        messageLatch = new CountDownLatch(numberOfMessages);

        System.out.println("TotalmenteConectada: Configurando " + numberOfNodes + " nodos, " + numberOfMessages + " mensajes");

        for (int i = 0; i < numberOfNodes; i++) {
            nodes.add(new Node(i, false, messageLatch));
            System.out.println("TotalmenteConectada: Nodo creado " + i);
        }

        for (Node node : nodes) {
            for (Node other : nodes) {
                if (node.getId() != other.getId()) {
                    node.addNeighbor(other);
                    System.out.println("TotalmenteConectada: Nodo " + node.getId() + " conectado con nodo " + other.getId());
                }
            }
        }
    }

    @Override
    public void sendMessage(int from, int to, String message) {
        if (from < 0 || from >= nodes.size() || to < 0 || to >= nodes.size()) {
            System.out.println("TotalmenteConectada: IDs de nodo inválidos: desde=" + from + ", hasta=" + to);
            return;
        }
        System.out.println("TotalmenteConectada: Enviando mensaje de " + from + " a " + to + ": " + message);
        executor.submit(() -> {
            nodes.get(to).receiveMessage(new Mensaje(from, to, message, false, 5));
        });
    }

    @Override
    public void runNetwork() {
        System.out.println("TotalmenteConectada: Ejecutando red");

        for (Node node : nodes) {
            executor.submit(() -> {
                System.out.println("TotalmenteConectada: Iniciando nodo " + node.getId());
                node.run();
                System.out.println("TotalmenteConectada: Nodo " + node.getId() + " finalizado, procesó " + node.getMessagesProcessed() + " mensajes");
            });
        }

        try {
            System.out.println("TotalmenteConectada: Esperando mensajes, cuenta del latch: " + messageLatch.getCount());
            boolean completed = messageLatch.await(60, TimeUnit.SECONDS);
            System.out.println("TotalmenteConectada: Latch completado: " + completed + ", cuenta restante: " + messageLatch.getCount());
        } catch (InterruptedException e) {
            System.out.println("TotalmenteConectada: Interrumpido mientras esperaba el latch");
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void shutdown() {
        System.out.println("TotalmenteConectada: Apagando");
        nodes.forEach(node -> {
            node.stop();
            System.out.println("TotalmenteConectada: Nodo detenido " + node.getId());
        });
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.out.println("TotalmenteConectada: Apagado forzado");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.out.println("TotalmenteConectada: Interrumpido durante apagado");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("TotalmenteConectada: Apagado completo");
    }
}
