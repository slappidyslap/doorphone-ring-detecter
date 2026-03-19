package kg.musabaev.listener;

import kg.musabaev.event.DeviceConnectedEvent;

@FunctionalInterface
public interface DeviceConnectedListener {

    void onConnected(DeviceConnectedEvent event);
}
