package kg.musabaev.doorphone.core.listener;

import kg.musabaev.doorphone.core.event.ServerStartedEvent;

@FunctionalInterface
public interface ServerStartedListener {

    void onStarted(ServerStartedEvent event);
}
