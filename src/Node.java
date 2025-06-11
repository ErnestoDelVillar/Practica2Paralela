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
    private final HashSet<Integer> processedMessages;

    public Node(int id, boolean forwardMessages, CountDownLatch messageLatch) {
        this.id = id;
        this.neighbors = new ArrayList<>();
        this.messageQueue = new LinkedBlockingQueue<>();
        this.running = true;
        this.forwardMessages = forwardMessages;
        this.messagesProcessed = new AtomicInteger(0);
        this.messageLatch = messageLatch;
        this.processedMessages = new HashSet<>();
    }

    public synchronized void addNeighbor(Node neighbor) {
        neighbors.add(neighbor);
    }

    public List<Node> getNeighbors() { return new ArrayList<>(neighbors); }
    public int getId() { return id; }

    public void receiveMessage(Mensaje message) {
        synchronized (this) {
            System.out.println("Node " + id + " enqueued message from " + message.getFrom() + " to " + message.getTo());
            messageQueue.offer(new Mensaje(message, message.getTtl()));
        }
    }

    public void run() {
        while (running || !messageQueue.isEmpty() || messageLatch.getCount() > 0) {
            try {
                Mensaje message = messageQueue.poll(1, TimeUnit.SECONDS);
                if (message == null) {
                    if (!running && messageLatch.getCount() == 0) {
                        break;
                    }
                    continue;
                }
                synchronized (processedMessages) {
                    if (!processedMessages.contains(message.getMessageId())) {
                        System.out.println("Node " + id + " processing message from " + message.getFrom() + " to " + message.getTo() + ": " + message.getContent());
                        processedMessages.add(message.getMessageId());
                        messagesProcessed.incrementAndGet();
                        if (message.getTo() == id) {
                            message.setDelivered();
                            if (messageLatch.getCount() > 0) {
                                messageLatch.countDown();
                                System.out.println("Node " + id + " decremented latch to " + messageLatch.getCount());
                            }
                        } else if (forwardMessages && !message.isDelivered() && message.getTtl() > 0) {
                            for (Node neighbor : neighbors) {
                                System.out.println("Node " + id + " forwarding message from " + message.getFrom() + " to neighbor " + neighbor.getId());
                                neighbor.receiveMessage(new Mensaje(message, message.getTtl() - 1));
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                // Ignorar interrupciones durante poll para continuar procesando
                System.out.println("Node " + id + " ignoring interrupt during message processing");
            }
        }
        System.out.println("Node " + id + " finished processing with queue size: " + messageQueue.size());
    }

    public void stop() {
        running = false;
        Thread.currentThread().interrupt(); // Interrumpir para desbloquear poll al cerrar
        System.out.println("Node " + id + " stop called");
    }

    public int getMessagesProcessed() { return messagesProcessed.get(); }
}