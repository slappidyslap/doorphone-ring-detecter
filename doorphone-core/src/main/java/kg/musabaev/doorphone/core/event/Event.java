package kg.musabaev.doorphone.core.event;

/**
 * Базовый класс всех событий.
 */
public sealed class Event permits DeviceConnectedEvent, DeviceDisconnectedEvent, SensorEvent, ServerStartedEvent {
}
