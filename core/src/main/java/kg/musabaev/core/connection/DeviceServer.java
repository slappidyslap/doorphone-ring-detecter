package kg.musabaev.core.connection;

import kg.musabaev.core.event.EventManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DeviceServer implements Runnable {

    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running = true;
    private final EventManager eventManager;
    private final ExecutorService connectionExecutor; // To run DeviceConnection in a separate thread

    public DeviceServer(int port, EventManager eventManager) {
        this.port = port;
        this.eventManager = eventManager;

        ThreadFactory daemonThreadFactory = r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("DeviceConnection-Thread");
            return thread;
        };
        this.connectionExecutor = Executors.newSingleThreadExecutor(daemonThreadFactory);
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("DeviceServer started on port " + port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Device connected from: " + clientSocket.getInetAddress());

                    DeviceConnection connection = new DeviceConnection(clientSocket, eventManager);
                    ConnectionManager.getInstance().setActiveConnection(connection);
                    connectionExecutor.submit(connection); // Run DeviceConnection in its own daemon thread

                } catch (IOException e) {
                    if (running) { // Only log if not intentionally stopped
                        System.err.println("DeviceServer accept error: " + e.getMessage());
                    }
                    // If accept fails, try again in the loop
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start DeviceServer on port " + port + ": " + e.getMessage());
        } finally {
            close();
        }
        System.out.println("DeviceServer stopped.");
    }

    public void stop() {
        running = false;
        close();
    }

    private void close() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            connectionExecutor.shutdownNow(); // Shut down the executor for connections
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }
}
