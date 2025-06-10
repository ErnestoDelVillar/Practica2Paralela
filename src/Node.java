import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Node {
    private final int id;
    private final List<Node> neighbors;
    private final BlockingQueue<Mensaje> messageQueue;
    private volatile boolean running;
    private final boolean forwardMessages;
    private final AtomicInteger messagesProcessed;
    private final CountDownLatch messageLatch;
    private final HashSet<Integer> processedMessages; // Usar Integer para messageId

    public Node(int id, boolean forwardMessages, CountDownLatch messageLatch) {
        this.id = id;
        this.neighbors = new ArrayList<>();
        this.messageQueue = new LinkedBlockingQueue<>();
        this.running = true;
        this.forwardMessages = forwardMessages;
        this.messagesProcessed = new AtomicInteger(0);
        this.messageLatch = messageLatch;
        this.processedMessages = new HashSet<>(); // Cambiar a HashSet<Integer>
    }

    public synchronized void addNeighbor(Node neighbor) {
        neighbors.add(neighbor);
    }

    public List<Node> getNeighbors() { return new ArrayList<>(neighbors); }
    public int getId() { return id; }

    public void receiveMessage(Mensaje message) {
        synchronized (this) {
            System.out.println("Node " + id + " enqueued message from " + message.getFrom() + " to " + message.getTo());
            messageQueue.offer(new Mensaje(message.getFrom(), message.getTo(), message.getContent(), message.isDelivered()));
        }
    }

    public void run() {
        while (running || !messageQueue.isEmpty()) {
            try {
                Mensaje message = messageQueue.poll(1, TimeUnit.MILLISECONDS);
                if (message != null && !processedMessages.contains(message.getMessageId())) {
                    synchronized (this) {
                        System.out.println("Node " + id + " processing message from " + message.getFrom() + ": " + message.getContent());
                        processedMessages.add(message.getMessageId());
                        messagesProcessed.incrementAndGet();
                        if (message.getTo() == id) {
                            message.setDelivered(); // Establecer delivered al llegar al destino
                            if (messageLatch.getCount() > 0) { // Decremento solo si hay cuentas pendientes
                                messageLatch.countDown();
                                System.out.println("Node " + id + " decremented latch to " + messageLatch.getCount());
                            }
                        } else if (forwardMessages && !message.isDelivered()) {
                            for (Node neighbor : neighbors) {
                                Mensaje copy = new Mensaje(message.getFrom(), message.getTo(), message.getContent(), message.isDelivered());
                                neighbor.receiveMessage(copy);
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
        synchronized (this) {
            System.out.println("Node " + id + " finished processing");
        }
    }

    public void stop() {
        running = false;
        messageQueue.clear(); // Limpiar cola para evitar bucles
    }

    public int getMessagesProcessed() { return messagesProcessed.get(); }
}