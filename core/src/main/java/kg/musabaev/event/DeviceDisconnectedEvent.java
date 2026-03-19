package kg.musabaev.event;

import kg.musabaev.DeviceSession;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.SocketAddress;

@Data
@EqualsAndHashCode(callSuper = true)
public non-sealed class DeviceDisconnectedEvent extends Event {

    private final DeviceSession session;
    private final SocketAddress remoteAddress;
}
