package kg.musabaev.doorphone.core.listener;

import kg.musabaev.doorphone.core.event.DeviceConnectedEvent;

@FunctionalInterface
public interface DeviceConnectedListener {

    void onConnected(DeviceConnectedEvent event);
}
