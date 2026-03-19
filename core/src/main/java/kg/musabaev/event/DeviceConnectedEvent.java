package kg.musabaev.event;

import kg.musabaev.DeviceConnection;
import java.net.SocketAddress;

public non-sealed class DeviceConnectedEvent extends Event {

    private final DeviceConnection connection;
    private final SocketAddress remoteAddress;

    public DeviceConnectedEvent(DeviceConnection connection, SocketAddress remoteAddress) {
        this.connection = connection;
        this.remoteAddress = remoteAddress;
    }

    public DeviceConnection getConnection() {
        return connection;
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }
}
