public class Main {
    public static void main(String[] args) {
        int numberOfNodes = 4;
        int numberOfMessages = 4;

        System.out.println("\nBusNetwork");
        NetworkManager busManager = new NetworkManager(new BusNetwork());
        busManager.configureNetwork(numberOfNodes, numberOfMessages);
        busManager.sendMessage(0, 1, "Hola al nodo 1 desde el nodo 0");
        busManager.sendMessage(1, 2, "Hola al nodo 2 desde el nodo 1");
        busManager.sendMessage(2, 3, "Hola al nodo 3 desde el nodo 2");
        busManager.sendMessage(3, 0, "Hola al nodo 0 desde el nodo 3");
        busManager.runNetwork();
        busManager.shutdown();

        System.out.println("\n\nRingNetwork");
        NetworkManager ringManager = new NetworkManager(new RingNetwork());
        ringManager.configureNetwork(numberOfNodes, numberOfMessages);
        ringManager.sendMessage(0, 1, "Hola al nodo 1 desde el nodo 0");
        ringManager.sendMessage(1, 2, "Hola al nodo 2 desde el nodo 1");
        ringManager.sendMessage(2, 3, "Hola al nodo 3 desde el nodo 2");
        ringManager.sendMessage(3, 0, "Hola al nodo 0 desde el nodo 3");
        ringManager.runNetwork();
        ringManager.shutdown();

        System.out.println("\n\nMeshNetwork");
        NetworkManager meshManager = new NetworkManager(new MeshNetwork());
        meshManager.configureNetwork(numberOfNodes, numberOfMessages);
        meshManager.sendMessage(0, 1, "Hola al nodo 1 desde el nodo 0");
        meshManager.sendMessage(1, 2, "Hola al nodo 2 desde el nodo 1");
        meshManager.sendMessage(2, 3, "Hola al nodo 3 desde el nodo 2");
        meshManager.sendMessage(3, 0, "Hola al nodo 0 desde el nodo 3");
        meshManager.runNetwork();
        meshManager.shutdown();

        System.out.println("\n\nStarNetwork");
        NetworkManager starManager = new NetworkManager(new StarNetwork());
        starManager.configureNetwork(numberOfNodes, numberOfMessages);
        starManager.sendMessage(0, 1, "Hola al nodo 1 desde el hub");
        starManager.sendMessage(1, 2, "Hola al nodo 2 desde el nodo 1");
        starManager.sendMessage(2, 3, "Hola al nodo 3 desde el nodo 2");
        starManager.sendMessage(3, 0, "Hola al hub desde el nodo 3");
        starManager.runNetwork();
        starManager.shutdown();

        System.out.println("\n\nHypercubeNetwork");
        NetworkManager hypercubeManager = new NetworkManager(new HypercubeNetwork());
        try {
            hypercubeManager.configureNetwork(numberOfNodes, numberOfMessages);
            hypercubeManager.sendMessage(0, 1, "Hola al nodo 1 desde el nodo 0");
            hypercubeManager.sendMessage(1, 2, "Hola al nodo 2 desde el nodo 1");
            hypercubeManager.sendMessage(2, 3, "Hola al nodo 3 desde el nodo 2");
            hypercubeManager.sendMessage(3, 0, "Hola al nodo 0 desde el nodo 3");
            hypercubeManager.runNetwork();
            hypercubeManager.shutdown();
        } catch (IllegalArgumentException e) {
            System.err.println("HypercubeNetwork: Error: " + e.getMessage());
        }

        System.out.println("\n\nTreeNetwork");
        NetworkManager treeManager = new NetworkManager(new TreeNetwork());
        treeManager.configureNetwork(numberOfNodes, numberOfMessages);
        treeManager.sendMessage(0, 1, "Hola al nodo 1 desde el nodo 0");
        treeManager.sendMessage(1, 2, "Hola al nodo 2 desde el nodo 1");
        treeManager.sendMessage(2, 3, "Hola al nodo 3 desde el nodo 2");
        treeManager.sendMessage(3, 0, "Hola al nodo 0 desde el nodo 3");
        treeManager.runNetwork();
        treeManager.shutdown();

        System.out.println("\n\nFullyConnectedNetwork");
        NetworkManager fullyConnectedManager = new NetworkManager(new FullyConnectedNetwork());
        fullyConnectedManager.configureNetwork(numberOfNodes, numberOfMessages);
        fullyConnectedManager.sendMessage(0, 1, "Hola al nodo 1 desde el nodo 0");
        fullyConnectedManager.sendMessage(1, 2, "Hola al nodo 2 desde el nodo 1");
        fullyConnectedManager.sendMessage(2, 3, "Hola al nodo 3 desde el nodo 2");
        fullyConnectedManager.sendMessage(3, 0, "Hola al nodo 0 desde el nodo 3");
        fullyConnectedManager.runNetwork();
        fullyConnectedManager.shutdown();

        System.out.println("\n\nSwitchedNetwork");
        NetworkManager switchedManager = new NetworkManager(new SwitchedNetwork());
        switchedManager.configureNetwork(numberOfNodes, numberOfMessages);
        switchedManager.sendMessage(0, 1, "Hola al nodo 1 desde el nodo 0");
        switchedManager.sendMessage(1, 2, "Hola al nodo 2 desde el nodo 1");
        switchedManager.sendMessage(2, 3, "Hola al nodo 3 desde el nodo 2");
        switchedManager.sendMessage(3, 0, "Hola al nodo 0 desde el nodo 3");
        switchedManager.runNetwork();
        switchedManager.shutdown();
    }
}