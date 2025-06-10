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
        messageLatch = new CountDownLatch(4);
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
        executor = Executors.newFixedThreadPool(3);
    }

    @Override
    public void sendMessage(int from, int to, String message) {
        if (from >= 0 && from < nodes.size() && to >= 0 && to < nodes.size()) {
            System.out.println("Sending message from " + from + " to " + to + ": " + message);
            executor.submit(() -> {
                nodes.get(to).receiveMessage(new Mensaje(from, to, message));
                System.out.println("Task submitted and executed for message from " + from + " to " + to);
            });
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
            executor.shutdown();
            for (Node node : nodes) {
                node.stop();
            }
            messageLatch.await(5, TimeUnit.SECONDS);
            System.out.println("Latch count after await: " + messageLatch.getCount());
            System.out.println("Active threads before termination: " + Thread.activeCount());
            System.out.println("Waiting for executor to terminate...");
            boolean terminatedGracefully = executor.awaitTermination(480, TimeUnit.SECONDS);
            if (!terminatedGracefully) {
                System.out.println("Executor forced shutdown");
                executor.shutdownNow();
            } else {
                System.out.println("Executor terminated gracefully");
            }
            if (terminatedGracefully) {
                System.out.println("Shutdown complete");
            } else {
                System.out.println("Shutdown incomplete due to forced termination");
            }
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