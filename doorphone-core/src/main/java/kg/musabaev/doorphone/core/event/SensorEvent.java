package kg.musabaev.doorphone.core.event;

/**
 * Базовый класс для всех событий датчиков.
 */
public sealed class SensorEvent extends Event permits DoorphoneRingDetectedEvent {
}
