package kg.musabaev.core.protocol;


public final class Protocol {

    private Protocol() {
    }

    public static final byte CMD_PING = 0x01;

    public static final byte EVENT_DOORPHONE_RING_DETECTED = (byte) 0x81;
}
