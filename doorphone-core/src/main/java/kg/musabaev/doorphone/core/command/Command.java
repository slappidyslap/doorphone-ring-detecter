package kg.musabaev.doorphone.core.command;

import lombok.Data;

/**
 * Базовый класс для всех команд, отправляемых на IoT устройство.
 */
@Data
public sealed class Command permits KeyValueCommand, PingCommand {

    private final String key;

    public String getPayload() {
        return "C:" + key;
    }
}
