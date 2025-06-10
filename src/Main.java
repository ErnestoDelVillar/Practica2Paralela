//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        // Crear una instancia de NetworkManager con BusNetwork
        NetworkManager manager = new NetworkManager(new BusNetwork());

        // Configurar la red con la topología de bus y el número de nodos
        manager.configureNetwork(5);

        // Enviar mensajes antes de ejecutar la red
        manager.sendMessage(0, 1, "Hello to Node 1");
        manager.sendMessage(1, 2, "Hello to Node 2");
        manager.sendMessage(2, 3, "Hello to Node 3");
        manager.sendMessage(4, 0, "Hello to Node 0");

        // Ejecutar la red
        manager.runNetwork();

        // Cerrar la red
        manager.shutdown();
    }
}