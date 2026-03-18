package kg.musabaev.listener;

import kg.musabaev.event.DoorphoneRingDetectedEvent;

@FunctionalInterface
public interface DoorphoneRingDetectedListener {

    void onDetected(DoorphoneRingDetectedEvent event);
}
