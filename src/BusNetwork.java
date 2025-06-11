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
    private CountDownLatch sendLatch;

    @Override
    public void configureNetwork(int numberOfNodes, int numberOfMessages) {
        if (numberOfNodes < 1) {
            throw new IllegalArgumentException("BusNetwork requires at least 1 node");
        }
        nodes = new ArrayList<>();
        messageLatch = new CountDownLatch(numberOfMessages);
        sendLatch = new CountDownLatch(numberOfMessages);
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
        executor = Executors.newFixedThreadPool(numberOfNodes);
    }

    @Override
    public void sendMessage(int from, int to, String message) {
        if (from >= 0 && from < nodes.size() && to >= 0 && to < nodes.size()) {
            System.out.println("Sending message from " + from + " to " + to + ": " + message);
            executor.submit(() -> {
                try {
                    nodes.get(to).receiveMessage(new Mensaje(from, to, message, false, 1));
                    System.out.println("Task submitted and executed for message from " + from + " to " + to);
                } finally {
                    sendLatch.countDown();
                    System.out.println("Send latch decremented to: " + sendLatch.getCount());
                }
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
        System.out.println("All nodes started");
    }

    @Override
    public void shutdown() {
        try {
            System.out.println("Waiting for all messages to be sent...");
            long remainingTime = 10_000; // 10 segundos en milisegundos
            long startTime = System.currentTimeMillis();
            while (remainingTime > 0) {
                try {
                    if (sendLatch.await(remainingTime, TimeUnit.MILLISECONDS)) {
                        break;
                    }
                    System.out.println("Timeout waiting for messages to be sent");
                    break;
                } catch (InterruptedException e) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    remainingTime -= elapsed;
                    if (remainingTime <= 0) {
                        System.out.println("Interrupted and timeout waiting for messages to be sent");
                        break;
                    }
                }
            }

            System.out.println("Current message latch count before await: " + messageLatch.getCount());
            boolean latchCompleted = false;
            remainingTime = 60_000; // 60 segundos
            startTime = System.currentTimeMillis();
            while (remainingTime > 0) {
                try {
                    if (messageLatch.await(remainingTime, TimeUnit.MILLISECONDS)) {
                        latchCompleted = true;
                        break;
                    }
                    System.out.println("Timeout waiting for message latch");
                    break;
                } catch (InterruptedException e) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    remainingTime -= elapsed;
                    if (remainingTime <= 0) {
                        System.out.println("Interrupted and timeout waiting for message latch");
                        break;
                    }
                }
            }

            System.out.println("Message latch count after await: " + messageLatch.getCount());
            System.out.println("Active threads before termination: " + Thread.activeCount());

            if (!latchCompleted) {
                System.out.println("Forcing cancellation of pending tasks due to message latch timeout");
                executor.shutdownNow();
            } else {
                executor.shutdown();
                for (Node node : nodes) {
                    node.stop();
                }
                System.out.println("Waiting for nodes to terminate...");
                remainingTime = 10_000; // 10 segundos
                startTime = System.currentTimeMillis();
                boolean nodesTerminated = false;
                while (remainingTime > 0) {
                    try {
                        if (executor.awaitTermination(remainingTime, TimeUnit.MILLISECONDS)) {
                            nodesTerminated = true;
                            break;
                        }
                        System.out.println("Timeout waiting for nodes to terminate");
                        break;
                    } catch (InterruptedException e) {
                        long elapsed = System.currentTimeMillis() - startTime;
                        remainingTime -= elapsed;
                        if (remainingTime <= 0) {
                            System.out.println("Interrupted and timeout waiting for nodes to terminate");
                            break;
                        }
                    }
                }
                if (!nodesTerminated) {
                    System.out.println("Forcing executor shutdown as nodes did not terminate");
                    executor.shutdownNow();
                }
            }

            System.out.println("Waiting for executor to terminate...");
            remainingTime = 10_000; // 10 segundos
            startTime = System.currentTimeMillis();
            boolean terminatedGracefully = false;
            while (remainingTime > 0) {
                try {
                    if (executor.awaitTermination(remainingTime, TimeUnit.MILLISECONDS)) {
                        terminatedGracefully = true;
                        break;
                    }
                    System.out.println("Timeout waiting for executor to terminate");
                    break;
                } catch (InterruptedException e) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    remainingTime -= elapsed;
                    if (remainingTime <= 0) {
                        System.out.println("Interrupted and timeout waiting for executor to terminate");
                        break;
                    }
                }
            }

            if (terminatedGracefully) {
                System.out.println("Executor terminated gracefully");
            } else {
                System.out.println("Executor forced shutdown after timeout");
            }

            if (terminatedGracefully) {
                System.out.println("Shutdown complete");
            } else {
                System.out.println("Shutdown incomplete due to forced termination");
            }
        } catch (OutOfMemoryError e) {
            System.err.println("OutOfMemoryError: " + e.getMessage());
            executor.shutdownNow();
        } finally {
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        }
    }
}