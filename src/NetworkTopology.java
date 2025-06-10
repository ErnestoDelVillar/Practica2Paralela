public interface NetworkTopology {
    void configureNetwork(int numberOfNodes, int numberOfMessages); // Modificado para aceptar numberOfMessages
    void sendMessage(int from, int to, String message);
    void runNetwork();
    void shutdown();
}

