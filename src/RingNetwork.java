import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RingNetwork implements NetworkTopology {
    private List<Node> nodes;
    private ExecutorService executor;
    private CountDownLatch messageLatch;

    @Override
    public void configureNetwork(int numberOfNodes, int numberOfMessages) {
        if (numberOfNodes < 2) {
            throw new IllegalArgumentException("RingNetwork requires at least 2 nodes");
        }
        if (numberOfMessages <= 0) {
            throw new IllegalArgumentException("Number of messages must be positive");
        }
        nodes = new ArrayList<>();
        messageLatch = new CountDownLatch(numberOfMessages);
        for (int i = 0; i < numberOfNodes; i++) {
            nodes.add(new Node(i, true, messageLatch));
        }
        for (int i = 0; i < numberOfNodes; i++) {
            int prev = (i - 1 + numberOfNodes) % numberOfNodes;
            int next = (i + 1) % numberOfNodes;
            nodes.get(i).addNeighbor(nodes.get(prev));
            nodes.get(i).addNeighbor(nodes.get(next));
        }
        executor = Executors.newFixedThreadPool(Math.min(numberOfNodes, 3));
    }

    @Override
    public void sendMessage(int from, int to, String message) {
        if (from >= 0 && from < nodes.size() && to >= 0 && to < nodes.size()) {
            System.out.println("Sending message from " + from + " to " + to + ": " + message);
            if (!executor.isShutdown() && !executor.isTerminated()) {
                executor.submit(() -> {
                    nodes.get(from).receiveMessage(new Mensaje(from, to, message, false));
                    System.out.println("Task submitted and executed for message from " + from + " to " + to);
                });
            }
        } else {
            System.out.println("Invalid node ID: from=" + from + ", to=" + to);
        }
    }

    @Override
    public void runNetwork() {
        for (Node node : nodes) {
            executor.submit(() -> {
                System.out.println("Starting node " + node.getId());
                node.run();
            });
        }
    }

    @Override
    public void shutdown() {
        try {
            System.out.println("Current latch count before await: " + messageLatch.getCount());
            executor.shutdown(); // Iniciar cierre ordenado
            for (Node node : nodes) {
                node.stop(); // Detener nodos
            }
            boolean latchCompleted = messageLatch.await(60, TimeUnit.SECONDS); // Esperar latch
            System.out.println("Latch count after await: " + messageLatch.getCount());
            System.out.println("Active threads before termination: " + Thread.activeCount());
            if (!latchCompleted) {
                System.out.println("Forcing cancellation of pending tasks due to latch timeout");
                executor.shutdownNow(); // Cancelar tareas pendientes si el latch no se resuelve
            }
            System.out.println("Waiting for executor to terminate...");
            boolean terminatedGracefully = executor.awaitTermination(60, TimeUnit.SECONDS); // Reducir a 60 segundos
            if (!terminatedGracefully) {
                System.out.println("Executor forced shutdown after timeout");
            } else {
                System.out.println("Executor terminated gracefully");
            }
            if (terminatedGracefully) {
                System.out.println("Shutdown complete");
            } else {
                System.out.println("Shutdown incomplete due to forced termination");
            }
        } catch (InterruptedException e) {
            System.out.println("Shutdown interrupted: " + e.getMessage());
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        } catch (OutOfMemoryError e) {
            System.err.println("OutOfMemoryError: " + e.getMessage());
            executor.shutdownNow();
        }
    }
}