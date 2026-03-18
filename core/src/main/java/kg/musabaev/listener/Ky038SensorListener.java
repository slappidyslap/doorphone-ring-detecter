package kg.musabaev.listener;

import kg.musabaev.event.Ky038SensorEvent;

@FunctionalInterface
public interface Ky038SensorListener {

    void onDetected(Ky038SensorEvent consumer);
}
