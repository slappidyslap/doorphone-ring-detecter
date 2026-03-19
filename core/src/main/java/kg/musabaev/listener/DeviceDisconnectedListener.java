package kg.musabaev.listener;

import kg.musabaev.event.DeviceDisconnectedEvent;

/**
 * Слушатель события разрыва соединения.
 */
@FunctionalInterface
public interface DeviceDisconnectedListener {

    void onDisconnected(DeviceDisconnectedEvent event);
}
