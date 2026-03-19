package kg.musabaev.listener;

import kg.musabaev.event.ServerStartedEvent;

@FunctionalInterface
public interface ServerStartedListener {

    void onStarted(ServerStartedEvent event);
}
