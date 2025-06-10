public class NetworkManager {
    private NetworkTopology topology;

    public NetworkManager(NetworkTopology topology) {
        this.topology = topology;
    }

    public void configureNetwork(int numberOfNodes, int numberOfMessages) {
        topology.configureNetwork(numberOfNodes, numberOfMessages);
    }

    public void sendMessage(int from, int to, String message) {
        topology.sendMessage(from, to, message);
    }

    public void runNetwork() {
        topology.runNetwork();
    }

    public void shutdown() {
        topology.shutdown();
    }
}