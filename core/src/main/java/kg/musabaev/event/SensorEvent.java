package kg.musabaev.event;

/**
 * Базовый класс для всех событий, передаваемых между IoT устройством
 * и GUI приложением.
 * <p>
 * События могут генерироваться как на стороне устройства (например, срабатывание сенсора),
 * так и на стороне GUI (например, изменение конфигурации).
 */
public sealed class SensorEvent permits DoorphoneRingDetectedEvent {
}
