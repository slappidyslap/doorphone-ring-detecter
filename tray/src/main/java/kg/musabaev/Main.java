package kg.musabaev;

import kg.musabaev.core.connection.DeviceServer;
import kg.musabaev.core.event.EventManager;

import java.awt.*;

public class Main {

    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        // Ensure AWT (GUI) components are initialized on the Event Dispatch Thread (EDT)
        EventQueue.invokeLater(() -> {
            EventManager eventManager = new EventManager();

            // Start the DeviceServer in a daemon thread
            DeviceServer deviceServer = new DeviceServer(SERVER_PORT, eventManager);
            Thread serverThread = new Thread(deviceServer);
            serverThread.setDaemon(true); // Make it a daemon thread
            serverThread.setName("DeviceServer-Thread");
            serverThread.start();

            // Create and show the GUI
            Gui gui = new Gui(eventManager);

            // Add a shutdown hook to gracefully stop the server
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down application...");
                deviceServer.stop(); // Stop the server
            }));
        });
    }
}