package kg.musabaev.doorphone.core.listener;

import kg.musabaev.doorphone.core.event.DoorphoneRingDetectedEvent;

@FunctionalInterface
public interface DoorphoneRingDetectedListener {

    void onDetected(DoorphoneRingDetectedEvent event);
}
