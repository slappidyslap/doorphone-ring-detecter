package kg.musabaev.doorphone.core.event;

import kg.musabaev.doorphone.core.DeviceSession;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.SocketAddress;

@Data
@EqualsAndHashCode(callSuper = true)
public non-sealed class DeviceConnectedEvent extends Event {

    private final DeviceSession session;
    private final SocketAddress remoteAddress;
}
