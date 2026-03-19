package kg.musabaev.core.command;

import java.nio.ByteBuffer;

public interface Command {

    byte getType();

    ByteBuffer toByteBuffer();
}
