import java.util.concurrent.atomic.AtomicInteger;

public class Mensaje {
    private final int from;
    private final int to;
    private final String content;
    private boolean delivered;
    private final int messageId;
    private final int ttl; // Added TTL to limit message hops

    private static final AtomicInteger idCounter = new AtomicInteger(0); // Thread-safe counter

    public Mensaje(int from, int to, String content, boolean delivered, int ttl) {
        this.from = from;
        this.to = to;
        this.content = content;
        this.delivered = delivered;
        this.messageId = idCounter.getAndIncrement(); // Thread-safe ID generation
        this.ttl = ttl;
    }

    // Copy constructor for creating message with decremented TTL
    public Mensaje(Mensaje other, int newTtl) {
        this.from = other.from;
        this.to = other.to;
        this.content = other.content;
        this.delivered = other.delivered;
        this.messageId = other.messageId; // Retain same ID
        this.ttl = newTtl;
    }

    public int getFrom() { return from; }
    public int getTo() { return to; }
    public String getContent() { return content; }
    public boolean isDelivered() { return delivered; }
    public void setDelivered() { this.delivered = true; }
    public int getMessageId() { return messageId; }
    public int getTtl() { return ttl; }
}