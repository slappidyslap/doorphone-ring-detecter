package kg.musabaev.command;

/**
 * Базовый класс для всех команд, отправляемых на устройство - ESP32.
 * <p>
 * Команды инициируются GUI приложением и описывают действия,
 * которые устройство должно выполнить.
 */
public sealed class Command permits PingCommand {
}
