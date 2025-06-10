public class Mensaje {
    private final int from;
    private final int to;
    private final String content;
    private boolean delivered;
    private final int messageId; // ID único basado en contador

    private static int idCounter = 0;

    public Mensaje(int from, int to, String content, boolean delivered) {
        this.from = from;
        this.to = to;
        this.content = content;
        this.delivered = delivered;
        this.messageId = idCounter++; // ID incremental
    }

    public int getFrom() { return from; }
    public int getTo() { return to; }
    public String getContent() { return content; }
    public boolean isDelivered() { return delivered; }
    public void setDelivered() { this.delivered = true; }
    public int getMessageId() { return messageId; }
}