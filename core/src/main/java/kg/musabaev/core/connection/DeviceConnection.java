package kg.musabaev.core.connection;

import kg.musabaev.core.command.Command;
import kg.musabaev.core.event.DoorphoneRingDetectedEvent;
import kg.musabaev.core.event.Event;
import kg.musabaev.core.event.EventManager;
import kg.musabaev.core.protocol.Protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DeviceConnection implements Runnable {

    private final Socket socket;
    private final EventManager eventManager;
    private DataInputStream in;
    private DataOutputStream out;
    private volatile boolean running = true;

    public DeviceConnection(Socket socket, EventManager eventManager) {
        this.socket = socket;
        this.eventManager = eventManager;
        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Error creating streams for device connection: " + e.getMessage());
            close();
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void sendCommand(Command command) throws IOException {
        if (!isConnected()) {
            throw new IOException("Device is not connected.");
        }
        ByteBuffer buffer = command.toByteBuffer();
        out.write(buffer.array());
        out.flush();
    }

    @Override
    public void run() {
        System.out.println("DeviceConnection started for " + socket.getInetAddress());
        while (running && isConnected()) {
            try {
                byte type = in.readByte();
                int payloadLength = in.readInt();
                byte[] payloadBytes = new byte[payloadLength];
                in.readFully(payloadBytes);

                ByteBuffer payloadBuffer = ByteBuffer.wrap(payloadBytes).order(ByteOrder.BIG_ENDIAN);

                Event event = null;
                switch (type) {
                    case Protocol.EVENT_DOORPHONE_RING_DETECTED:
                        event = new DoorphoneRingDetectedEvent(payloadBuffer);
                        break;
                    // Add other event types here
                    default:
                        System.err.println("Unknown event type received: " + type);
                        break;
                }

                if (event != null) {
                    eventManager.dispatchEvent(event);
                }

            } catch (IOException e) {
                if (running) { // Only log if not intentionally stopped
                    System.err.println("DeviceConnection read error: " + e.getMessage());
                }
                close();
                break;
            }
        }
        System.out.println("DeviceConnection stopped for " + socket.getInetAddress());
    }

    public void close() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
        System.out.println("DeviceConnection closed for " + socket.getInetAddress());
    }
}
