public interface NetworkTopology {
    void configureNetwork(int numberOfNodes);
    void sendMessage(int from, int to, String message);
    void runNetwork();
    void shutdown();
}

