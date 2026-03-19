package kg.musabaev.doorphone.core.listener;

import kg.musabaev.doorphone.core.event.DeviceDisconnectedEvent;

/**
 * Слушатель события разрыва соединения.
 */
@FunctionalInterface
public interface DeviceDisconnectedListener {

    void onDisconnected(DeviceDisconnectedEvent event);
}
