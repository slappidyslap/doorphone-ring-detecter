package kg.musabaev.core.command;

import kg.musabaev.core.protocol.Protocol;

import java.nio.ByteBuffer;

public class PingCommand implements Command {

    @Override
    public byte getType() {
        return Protocol.CMD_PING;
    }

    @Override
    public ByteBuffer toByteBuffer() {
        // Command [1 byte type] + [4 bytes length] + [payload]
        ByteBuffer buffer = ByteBuffer.allocate(1 + 4);
        buffer.put(getType());
        buffer.putInt(0); // Payload length is 0
        buffer.flip();
        return buffer;
    }
}
