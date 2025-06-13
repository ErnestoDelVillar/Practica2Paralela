import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TreeNetwork implements NetworkTopology {
    private List<Node> nodes;
    private ExecutorService executor;
    private final int branchingFactor = 2;
    private int numberOfMessages;
    private CountDownLatch messageLatch;

    @Override
    public void configureNetwork(int numberOfNodes, int numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
        nodes = new ArrayList<>();
        executor = Executors.newFixedThreadPool(numberOfNodes);
        messageLatch = new CountDownLatch(numberOfMessages);

        System.out.println("Árbol: Configurando " + numberOfNodes + " nodos, " + numberOfMessages + " mensajes");

        for (int i = 0; i < numberOfNodes; i++) {
            nodes.add(new Node(i, true, messageLatch));
            System.out.println("Árbol: Nodo creado " + i);
        }

        for (int i = 0; i < numberOfNodes; i++) {
            int leftChild = 2 * i + 1;
            if (leftChild < numberOfNodes) {
                nodes.get(i).addNeighbor(nodes.get(leftChild));
                nodes.get(leftChild).addNeighbor(nodes.get(i));
                System.out.println("Árbol: Nodo " + i + " conectado con hijo izquierdo " + leftChild);
            }
            int rightChild = 2 * i + 2;
            if (rightChild < numberOfNodes) {
                nodes.get(i).addNeighbor(nodes.get(rightChild));
                nodes.get(rightChild).addNeighbor(nodes.get(i));
                System.out.println("Árbol: Nodo " + i + " conectado con hijo derecho " + rightChild);
            }
        }
    }

    @Override
    public void sendMessage(int from, int to, String message) {
        if (from < 0 || from >= nodes.size() || to < 0 || to >= nodes.size()) {
            System.out.println("Árbol: IDs de nodo inválidos: desde=" + from + ", hasta=" + to);
            return;
        }
        System.out.println("Árbol: Enviando mensaje de " + from + " a " + to + ": " + message);
        executor.submit(() -> {
            nodes.get(from).receiveMessage(new Mensaje(from, to, message, false, 5));
        });
    }

    @Override
    public void runNetwork() {
        System.out.println("Árbol: Ejecutando red");

        for (Node node : nodes) {
            executor.submit(() -> {
                System.out.println("Árbol: Iniciando nodo " + node.getId());
                node.run();
                System.out.println("Árbol: Nodo " + node.getId() + " finalizado, procesó " + node.getMessagesProcessed() + " mensajes");
            });
        }

        try {
            System.out.println("Árbol: Esperando mensajes, cuenta del latch: " + messageLatch.getCount());
            boolean completed = messageLatch.await(60, TimeUnit.SECONDS);
            System.out.println("Árbol: Latch completado: " + completed + ", cuenta restante: " + messageLatch.getCount());
        } catch (InterruptedException e) {
            System.out.println("Árbol: Interrumpido mientras esperaba el latch");
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void shutdown() {
        System.out.println("Árbol: Apagando");
        nodes.forEach(node -> {
            node.stop();
            System.out.println("Árbol: Nodo detenido " + node.getId());
        });
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.out.println("Árbol: Apagado forzado");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.out.println("Árbol: Interrumpido durante apagado");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Árbol: Apagado completo");
    }
}
