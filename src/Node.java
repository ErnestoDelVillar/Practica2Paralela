import java.util.ArrayList;
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

    public Node(int id, boolean forwardMessages, CountDownLatch messageLatch) {
        this.id = id;
        this.neighbors = new ArrayList<>();
        this.messageQueue = new LinkedBlockingQueue<>();
        this.running = true;
        this.forwardMessages = forwardMessages;
        this.messagesProcessed = new AtomicInteger(0);
        this.messageLatch = messageLatch;
    }

    public synchronized void addNeighbor(Node neighbor) {
        neighbors.add(neighbor);
    }

    public List<Node> getNeighbors() { return new ArrayList<>(neighbors); } // Retorna copia para evitar modificaciones externas
    public int getId() { return id; }

    public void receiveMessage(Mensaje message) {
        System.out.println("Node " + id + " enqueued message from " + message.getFrom() + " to " + message.getTo());
        messageQueue.offer(message);
    }

    public void run() {
        while (running || !messageQueue.isEmpty()) {
            try {
                Mensaje message = messageQueue.poll(1, TimeUnit.MILLISECONDS);
                if (message != null) {
                    System.out.println("Node " + id + " processing message from " + message.getFrom() + ": " + message.getContent());
                    messagesProcessed.incrementAndGet();
                    if (forwardMessages && message.getTo() != id) {
                        synchronized (this) { // Sincronizar el reenvío para evitar colisiones
                            for (Node neighbor : neighbors) {
                                neighbor.receiveMessage(message);
                            }
                        }
                    }
                    if (message.getTo() == id) {
                        messageLatch.countDown();
                        System.out.println("Node " + id + " decremented latch to " + messageLatch.getCount());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
        System.out.println("Node " + id + " finished processing");
    }

    public void stop() {
        running = false;
        messageQueue.clear();
    }

    public int getMessagesProcessed() { return messagesProcessed.get(); }
}