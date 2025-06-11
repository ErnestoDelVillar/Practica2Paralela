public class Main {
    public static void main(String[] args) {
        // Numero de nodos y mensajes
        int numberOfNodes = 4;
        int numberOfMessages = 4;

        // Prueba BusNetwork
        System.out.println("=== Probando BusNetwork ===");
        NetworkManager busManager = new NetworkManager(new BusNetwork());
        busManager.configureNetwork(numberOfNodes, numberOfMessages);
        busManager.sendMessage(0, 1, "Hello to Node 1 from Node 0");
        busManager.sendMessage(1, 2, "Hello to Node 2 from Node 1");
        busManager.sendMessage(2, 3, "Hello to Node 3 from Node 2");
        busManager.sendMessage(3, 0, "Hello to Node 0 from Node 3");
        busManager.runNetwork();
        busManager.shutdown();

        System.out.println("");

        // Prueba RingNetwork
        System.out.println("=== Probando RingNetwork ===");
        NetworkManager ringManager = new NetworkManager(new RingNetwork());
        ringManager.configureNetwork(numberOfNodes, numberOfMessages);
        ringManager.sendMessage(0, 1, "Hello to Node 1 from Node 0");
        ringManager.sendMessage(1, 2, "Hello to Node 2 from Node 1");
        ringManager.sendMessage(2, 3, "Hello to Node 3 from Node 2");
        ringManager.sendMessage(3, 0, "Hello to Node 0 from Node 3");
        ringManager.runNetwork();
        ringManager.shutdown();
    }
}