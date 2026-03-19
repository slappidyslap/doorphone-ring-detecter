package kg.musabaev.event;

import kg.musabaev.DeviceConnection;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.SocketAddress;

@Data
@EqualsAndHashCode(callSuper = true)
public non-sealed class DeviceConnectedEvent extends Event {

    private final DeviceConnection connection;
    private final SocketAddress remoteAddress;
}
