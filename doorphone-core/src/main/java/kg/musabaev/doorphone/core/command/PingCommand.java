package kg.musabaev.doorphone.core.command;

/**
 * Команда проверки соединения с устройством.
 */
public non-sealed class PingCommand extends Command {

    public PingCommand() {
        super("PING");
    }
}
