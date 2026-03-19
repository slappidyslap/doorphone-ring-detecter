package kg.musabaev.core.event;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class EventManager {

    private final Set<EventListener> listeners = new CopyOnWriteArraySet<>();

    public void addListener(EventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(EventListener listener) {
        listeners.remove(listener);
    }

    public void dispatchEvent(Event event) {
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
