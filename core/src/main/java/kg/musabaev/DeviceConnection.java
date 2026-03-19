package kg.musabaev;

import kg.musabaev.command.Command;
import kg.musabaev.event.DeviceDisconnectedEvent;
import kg.musabaev.event.DoorphoneRingDetectedEvent;
import kg.musabaev.listener.DeviceDisconnectedListener;
import kg.musabaev.listener.DoorphoneRingDetectedListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Инкапсулирует активное подключение к IoT устройству.
 * <p>
 * Предоставляет высокоуровневое API для взаимодействия с устройством:
 * <ul>
 *   <li>
 *       Отправка команд {@link Command} с помощью метода: {@link #sendCommand(Command)}
 *   </li>
 *   <li>
 *       Подписка на события устройства, например на звонок домофона (с помощью метода {@link #addDoorphoneRingDetectedListener(DoorphoneRingDetectedListener)})
 *   </li>
 * </ul>
 */
public class DeviceConnection implements Runnable {

    private final SocketChannel deviceChannel;

    private final List<DoorphoneRingDetectedListener> ringDetectedListeners;
    private final List<DeviceDisconnectedListener> deviceDisconnectedListeners;

    /**
     * Создает новое соединение на базе открытого канала.
     *
     * @param deviceChannel открытый канал сокета IoT устройства
     */
    public DeviceConnection(SocketChannel deviceChannel) {
        this.deviceChannel = deviceChannel;

        this.ringDetectedListeners = new ArrayList<>();
        this.deviceDisconnectedListeners = new ArrayList<>();
    }

    /**
     * Отправляет команду IoT устройству.
     *
     * @param cmd объект команды
     */
    public void sendCommand(Command cmd) {
        String rawPayload = cmd
                .getClass()
                .getSimpleName()
                .replace("Command", "")
                .toUpperCase();
        sendCommand(rawPayload);
    }

    /**
     * Отправляет произвольную строковую команду IoT устройству.
     *
     * @param payload строка для отправки
     */
    private void sendCommand(String payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload.getBytes(StandardCharsets.UTF_8));
        while (buffer.hasRemaining()) {
            try {
                deviceChannel.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e); //todo
            }
        }
    }

    @Override
    public void run() {
        readLoop();
    }

    /**
     * Внутренний цикл чтения данных из сокета.
     */
    private void readLoop() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        try {
            while (isOpen()) {
                buffer.clear();
                int bytesRead = deviceChannel.read(buffer);

                if (bytesRead == -1) break;

                if (bytesRead > 0) {
                    buffer.flip();
                    var data = new byte[buffer.remaining()];
                    buffer.get(data);
                    var message = new String(data, StandardCharsets.UTF_8).trim();

                    if (!message.isEmpty()) {
                        processIncomingMessage(message);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            handleDisconnect();
        }
    }

    /**
     * Разбирает входящее сообщение и уведомляет соответствующих слушателей.
     */
    private void processIncomingMessage(String message) {
        if ("RING".equals(message)) fireDoorphoneRingDetectedListeners();
    }

    /**
     * Выполняет очистку ресурсов и уведомляет об отключении.
     */
    private void handleDisconnect() {
        try {
            fireDeviceDisconnectedListeners(
                    new DeviceDisconnectedEvent(deviceChannel.getRemoteAddress()));
            close();
        } catch (IOException ignored) {
            //todo
        }
    }

    /**
     * Добавляет слушателя события звонка домофона.
     */
    public void addDoorphoneRingDetectedListener(DoorphoneRingDetectedListener listener) {
        ringDetectedListeners.add(requireNonNull(listener));
    }

    /**
     * Уведомить всех слушателей звонка домофона.
     */
    private void fireDoorphoneRingDetectedListeners() {
        ringDetectedListeners.forEach(l -> l.onDetected(new DoorphoneRingDetectedEvent()));
    }

    /**
     * Добавляет слушателя разрыва соединения.
     */
    public void addDeviceDisconnectedListener(DeviceDisconnectedListener listener) {
        deviceDisconnectedListeners.add(requireNonNull(listener));
    }

    /**
     * Уведомить всех слушателей разрыва соединения.
     */
    private void fireDeviceDisconnectedListeners(DeviceDisconnectedEvent event) {
        deviceDisconnectedListeners.forEach(l -> l.onDisconnected(event));
    }

    /**
     * Проверяет, открыто ли соединение.
     *
     * @return true, если канал открыт
     */
    public boolean isOpen() {
        return deviceChannel.isOpen();
    }

    /**
     * Принудительно закрывает соединение.
     *
     * @throws IOException если произошла ошибка при закрытии
     */
    public void close() throws IOException {
        deviceChannel.close();
    }
}
