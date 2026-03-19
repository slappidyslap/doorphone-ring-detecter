package kg.musabaev.core.connection;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

public class ConnectionManager {

    private static final ConnectionManager INSTANCE = new ConnectionManager();

    private final AtomicReference<DeviceConnection> activeConnectionRef = new AtomicReference<>();

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        return INSTANCE;
    }

    public void setActiveConnection(DeviceConnection connection) {
        DeviceConnection oldConnection = activeConnectionRef.getAndSet(connection);
        if (oldConnection != null && oldConnection.isConnected()) {
            oldConnection.close();
        }
    }

    public DeviceConnection getActiveConnection() {
        return activeConnectionRef.get();
    }

    public boolean hasActiveConnection() {
        DeviceConnection connection = activeConnectionRef.get();
        return connection != null && connection.isConnected();
    }
}
