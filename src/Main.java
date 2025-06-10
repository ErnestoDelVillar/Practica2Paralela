//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        //Numero de Nodos y Mensajes
        int numberOfNodes = 4;
        int numberOfMessages = 2;


//        // Crear una instancia de NetworkManager con BusNetwork
//        NetworkManager manager = new NetworkManager(new BusNetwork());
//
//
//        // Configurar la red con la topología de bus con el número de nodos y mensajes
//        manager.configureNetwork(numberOfNodes, numberOfMessages);
//
//        // Enviar mensajes antes de ejecutar la red
//        manager.sendMessage(0, 1, "Hello to Node 1");
//        manager.sendMessage(1, 2, "Hello to Node 2");
//        //manager.sendMessage(2, 3, "Hello to Node 3");
//        //manager.sendMessage(4, 0, "Hello to Node 0");
//
//
//        // Ejecutar la red
//        manager.runNetwork();
//
//        // Cerrar la red
//        manager.shutdown();


        // Crear una instancia de NetworkManager con RingNetwork
        NetworkManager manager = new NetworkManager(new RingNetwork());

        // Configurar la red con la topología de bus con el número de nodos y mensajes
        manager.configureNetwork(numberOfNodes, numberOfMessages);

        // Enviar mensajes antes de ejecutar la red
        manager.sendMessage(0, 1, "Hello to Node 1 from Node 0 ");
        manager.sendMessage(1, 2, "Hello to Node 2 from Node 1");
        //manager.sendMessage(2, 3, "Hello to Node 3");
        //manager.sendMessage(4, 0, "Hello to Node 0");


        System.out.println("Waiting for message enqueuing...");
        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        System.out.println("Starting network execution...");

        // Ejecutar la red
        manager.runNetwork();

        // Cerrar la red
        manager.shutdown();

    }
}