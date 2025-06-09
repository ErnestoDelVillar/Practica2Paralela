import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BusNetwork implements NetworkTopology {
    private List<Node> nodes;
    private ExecutorService executor;
    private CountDownLatch messageLatch;

    @Override
    public void configureNetwork(int numberOfNodes) {
        nodes = new ArrayList<>();
        messageLatch = new CountDownLatch(4); // Para los 4 mensajes
        for (int i = 0; i < numberOfNodes; i++) {
            nodes.add(new Node(i, false, messageLatch));
        }
        for (Node node : nodes) {
            for (Node other : nodes) {
                if (node != other) {
                    node.addNeighbor(other);
                }
            }
        }
        executor = Executors.newFixedThreadPool(numberOfNodes); // 5 hilos, como en la versión exitosa
    }

    @Override
    public void sendMessage(int from, int to, String message) {
        if (from >= 0 && from < nodes.size() && to >= 0 && to < nodes.size()) {
            System.out.println("Sending message from " + from + " to " + to + ": " + message);
            try {
                executor.submit(() -> {
                    nodes.get(to).receiveMessage(new Mensaje(from, to, message));
                    System.out.println("Task submitted and executed for message from " + from + " to " + to);
                });
            } catch (Exception e) {
                System.err.println("Error submitting task for message from " + from + " to " + to + ": " + e.getMessage());
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
            executor.shutdown(); // Cerrar el executor antes de esperar el latch
            messageLatch.await(60, TimeUnit.SECONDS); // Tiempo de espera para el latch
            System.out.println("Latch count after await: " + messageLatch.getCount());
            System.out.println("Active threads before termination: " + Thread.activeCount());
            System.out.println("Waiting for executor to terminate...");
            if (!executor.awaitTermination(240, TimeUnit.SECONDS)) { // Aumentar tiempo de espera
                executor.shutdownNow();
                System.out.println("Executor forced shutdown");
            } else {
                System.out.println("Executor terminated gracefully");
            }
            for (Node node : nodes) {
                node.stop();
            }
            System.out.println("Shutdown complete");
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            System.out.println("Shutdown interrupted: " + e.getMessage());
        } catch (OutOfMemoryError e) {
            System.err.println("OutOfMemoryError: " + e.getMessage());
            executor.shutdownNow();
        }
    }
}