public class Main {
    public static void main(String[] args) {
        int numberOfNodes = 4;
        int numberOfMessages = 4;

        System.out.println("BusNetwork");
        NetworkManager busManager = new NetworkManager(new BusNetwork());
        busManager.configureNetwork(numberOfNodes, numberOfMessages);
        busManager.sendMessage(0, 1, "Hello to Node 1 from Node 0");
        busManager.sendMessage(1, 2, "Hello to Node 2 from Node 1");
        busManager.sendMessage(2, 3, "Hello to Node 3 from Node 2");
        busManager.sendMessage(3, 0, "Hello to Node 0 from Node 3");
        busManager.runNetwork();
        busManager.shutdown();

        System.out.println("\n\nRingNetwork");
        NetworkManager ringManager = new NetworkManager(new RingNetwork());
        ringManager.configureNetwork(numberOfNodes, numberOfMessages);
        ringManager.sendMessage(0, 1, "Hello to Node 1 from Node 0");
        ringManager.sendMessage(1, 2, "Hello to Node 2 from Node 1");
        ringManager.sendMessage(2, 3, "Hello to Node 3 from Node 2");
        ringManager.sendMessage(3, 0, "Hello to Node 0 from Node 3");
        ringManager.runNetwork();
        ringManager.shutdown();

        System.out.println("\n\neshNetwork");
        NetworkManager meshManager = new NetworkManager(new MeshNetwork());
        meshManager.configureNetwork(numberOfNodes, numberOfMessages);
        meshManager.sendMessage(0, 1, "Hello to Node 1 from Node 0");
        meshManager.sendMessage(1, 2, "Hello to Node 2 from Node 1");
        meshManager.sendMessage(2, 3, "Hello to Node 3 from Node 2");
        meshManager.sendMessage(3, 0, "Hello to Node 0 from Node 3");
        meshManager.runNetwork();
        meshManager.shutdown();

        System.out.println("\n\nHypercubeNetwork");
        NetworkManager hypercubeManager = new NetworkManager(new HypercubeNetwork());
        try {
            hypercubeManager.configureNetwork(numberOfNodes, numberOfMessages);
            hypercubeManager.sendMessage(0, 1, "Hello to Node 1 from Node 0");
            hypercubeManager.sendMessage(1, 2, "Hello to Node 2 from Node 1");
            hypercubeManager.sendMessage(2, 3, "Hello to Node 3 from Node 2");
            hypercubeManager.sendMessage(3, 0, "Hello to Node 0 from Node 3");
            hypercubeManager.runNetwork();
            hypercubeManager.shutdown();
        } catch (IllegalArgumentException e) {
            System.err.println("Hypercube Error: " + e.getMessage());
        }

        System.out.println("\n\nTreeNetwork");
        NetworkManager treeManager = new NetworkManager(new TreeNetwork());
        treeManager.configureNetwork(numberOfNodes, numberOfMessages);
        treeManager.sendMessage(0, 1, "Hello to Node 1 from Node 0");
        treeManager.sendMessage(1, 2, "Hello to Node 2 from Node 1");
        treeManager.sendMessage(2, 3, "Hello to Node 3 from Node 2");
        treeManager.sendMessage(3, 0, "Hello to Node 0 from Node 3");
        treeManager.runNetwork();
        treeManager.shutdown();

        System.out.println("\n\nFullyConnectedNetwork");
        NetworkManager fullyConnectedManager = new NetworkManager(new FullyConnectedNetwork());
        fullyConnectedManager.configureNetwork(numberOfNodes, numberOfMessages);
        fullyConnectedManager.sendMessage(0, 1, "Hello to Node 1 from Node 0");
        fullyConnectedManager.sendMessage(1, 2, "Hello to Node 2 from Node 1");
        fullyConnectedManager.sendMessage(2, 3, "Hello to Node 3 from Node 2");
        fullyConnectedManager.sendMessage(3, 0, "Hello to Node 0 from Node 3");
        fullyConnectedManager.runNetwork();
        fullyConnectedManager.shutdown();

        System.out.println("\n\nSwitchedNetwork");
        NetworkManager switchedManager = new NetworkManager(new SwitchedNetwork());
        switchedManager.configureNetwork(numberOfNodes, numberOfMessages);
        switchedManager.sendMessage(0, 1, "Hello to Node 1 from Node 0");
        switchedManager.sendMessage(1, 2, "Hello to Node 2 from Node 1");
        switchedManager.sendMessage(2, 3, "Hello to Node 3 from Node 2");
        switchedManager.sendMessage(3, 0, "Hello to Node 0 from Node 3");
        switchedManager.runNetwork();
        switchedManager.shutdown();
    }
}