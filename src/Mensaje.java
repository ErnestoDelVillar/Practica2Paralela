public class Mensaje {

    private final int from;
    private final int to;
    private final String content;

    public Mensaje(int from, int to, String content) {
        this.from = from;
        this.to = to;
        this.content = content;
    }

    public int getFrom() { return from; }
    public int getTo() { return to; }
    public String getContent() { return content; }

}
