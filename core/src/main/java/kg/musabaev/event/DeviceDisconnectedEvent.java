package kg.musabaev.event;

import java.net.SocketAddress;

public non-sealed class DeviceDisconnectedEvent extends Event {

    private final SocketAddress remoteAddress;

    public DeviceDisconnectedEvent(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }
}
