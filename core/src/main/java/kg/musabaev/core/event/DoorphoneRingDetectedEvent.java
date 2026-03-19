package kg.musabaev.core.event;

import java.nio.ByteBuffer;

public class DoorphoneRingDetectedEvent extends Event {

    private final long timestamp;

    public DoorphoneRingDetectedEvent(ByteBuffer payload) {
        this.timestamp = payload.getLong();
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "DoorphoneRingDetectedEvent{" +
                "timestamp=" + timestamp +
                '}';
    }
}
