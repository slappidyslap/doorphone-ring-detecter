package kg.musabaev.event;

/**
 * Базовый класс для всех событий датчиков.
 */
public sealed class SensorEvent extends Event permits DoorphoneRingDetectedEvent {
}
