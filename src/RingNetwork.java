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
    private CountDownLatch sendLatch;

    @Override
    public void configureNetwork(int numberOfNodes, int numberOfMessages) {
        if (numberOfNodes < 2) {
            throw new IllegalArgumentException("RingNetwork requiere al menos 2 nodos");
        }
        if (numberOfMessages <= 0) {
            throw new IllegalArgumentException("El número de mensajes debe ser positivo");
        }
        nodes = new ArrayList<>();
        messageLatch = new CountDownLatch(numberOfMessages);
        sendLatch = new CountDownLatch(numberOfMessages);
        for (int i = 0; i < numberOfNodes; i++) {
            nodes.add(new Node(i, true, messageLatch));
        }
        for (int i = 0; i < numberOfNodes; i++) {
            int prev = (i - 1 + numberOfNodes) % numberOfNodes;
            int next = (i + 1) % numberOfNodes;
            nodes.get(i).addNeighbor(nodes.get(prev));
            nodes.get(i).addNeighbor(nodes.get(next));
        }
        executor = Executors.newFixedThreadPool(numberOfNodes);
    }

    @Override
    public void sendMessage(int from, int to, String message) {
        if (from >= 0 && from < nodes.size() && to >= 0 && to < nodes.size()) {
            System.out.println("Enviando mensaje de " + from + " a " + to + ": " + message);
            if (!executor.isShutdown() && !executor.isTerminated()) {
                executor.submit(() -> {
                    try {
                        nodes.get(from).receiveMessage(new Mensaje(from, to, message, false, (nodes.size() + 1) / 2));
                        System.out.println("Tarea enviada y ejecutada para mensaje de " + from + " a " + to);
                    } finally {
                        sendLatch.countDown();
                        System.out.println("Send latch decreció a: " + sendLatch.getCount());
                    }
                });
            }
        } else {
            System.out.println("ID de nodo inválido: from=" + from + ", to=" + to);
        }
    }

    @Override
    public void runNetwork() {
        for (Node node : nodes) {
            executor.submit(() -> {
                System.out.println("Nodo " + node.getId() + " iniciando");
                node.run();
            });
        }
        System.out.println("Todos los nodos iniciaron");
    }

    @Override
    public void shutdown() {
        try {
            System.out.println("Esperando a que todos los mensajes sean enviados...");
            long remainingTime = 10_000;
            long startTime = System.currentTimeMillis();
            while (remainingTime > 0) {
                try {
                    if (sendLatch.await(remainingTime, TimeUnit.MILLISECONDS)) {
                        break;
                    }
                    System.out.println("Tiempo de espera agotado para el envío de mensajes");
                    break;
                } catch (InterruptedException e) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    remainingTime -= elapsed;
                    if (remainingTime <= 0) {
                        System.out.println("Interrumpido y tiempo de espera agotado para el envío de mensajes");
                        break;
                    }
                }
            }

            System.out.println("Conteo de message latch antes de await: " + messageLatch.getCount());
            boolean latchCompleted = false;
            remainingTime = 60_000;
            startTime = System.currentTimeMillis();
            while (remainingTime > 0) {
                try {
                    if (messageLatch.await(remainingTime, TimeUnit.MILLISECONDS)) {
                        latchCompleted = true;
                        break;
                    }
                    System.out.println("Tiempo de espera agotado para message latch");
                    break;
                } catch (InterruptedException e) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    remainingTime -= elapsed;
                    if (remainingTime <= 0) {
                        System.out.println("Interrumpido y tiempo de espera agotado para message latch");
                        break;
                    }
                }
            }

            System.out.println("Conteo de message latch después de await: " + messageLatch.getCount());
            System.out.println("Hilos activos antes de terminación: " + Thread.activeCount());

            if (!latchCompleted) {
                System.out.println("Forzando cancelación de tareas pendientes debido a timeout de message latch");
                executor.shutdownNow();
            } else {
                executor.shutdown();
                for (Node node : nodes) {
                    node.stop();
                }
                System.out.println("Esperando a que los nodos terminen...");
                remainingTime = 10_000;
                startTime = System.currentTimeMillis();
                boolean nodesTerminated = false;
                while (remainingTime > 0) {
                    try {
                        if (executor.awaitTermination(remainingTime, TimeUnit.MILLISECONDS)) {
                            nodesTerminated = true;
                            break;
                        }
                        System.out.println("Tiempo de espera agotado para la terminación de nodos");
                        break;
                    } catch (InterruptedException e) {
                        long elapsed = System.currentTimeMillis() - startTime;
                        remainingTime -= elapsed;
                        if (remainingTime <= 0) {
                            System.out.println("Interrumpido y tiempo de espera agotado para la terminación de nodos");
                            break;
                        }
                    }
                }
                if (!nodesTerminated) {
                    System.out.println("Forzando cierre del executor porque los nodos no terminaron");
                    executor.shutdownNow();
                }
            }

            System.out.println("Esperando a que el executor termine...");
            remainingTime = 10_000;
            startTime = System.currentTimeMillis();
            boolean terminatedGracefully = false;
            while (remainingTime > 0) {
                try {
                    if (executor.awaitTermination(remainingTime, TimeUnit.MILLISECONDS)) {
                        terminatedGracefully = true;
                        break;
                    }
                    System.out.println("Tiempo de espera agotado para la terminación del executor");
                    break;
                } catch (InterruptedException e) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    remainingTime -= elapsed;
                    if (remainingTime <= 0) {
                        System.out.println("Interrumpido y tiempo de espera agotado para la terminación del executor");
                        break;
                    }
                }
            }

            if (terminatedGracefully) {
                System.out.println("Executor terminó correctamente");
            } else {
                System.out.println("Executor forzó cierre tras timeout");
            }

            if (terminatedGracefully) {
                System.out.println("Cierre completado");
            } else {
                System.out.println("Cierre incompleto debido a terminación forzada");
            }
        } catch (OutOfMemoryError e) {
            System.err.println("Error de memoria insuficiente: " + e.getMessage());
            executor.shutdownNow();
        } finally {
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        }
    }
}