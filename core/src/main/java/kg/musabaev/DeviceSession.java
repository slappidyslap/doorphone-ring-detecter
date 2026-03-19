package kg.musabaev;

import kg.musabaev.command.Command;
import kg.musabaev.command.CommandResponse;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.parseInt;
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
public class DeviceSession implements Runnable {

    private final SocketChannel deviceChannel;
    private final ExecutorService commandExecutor;

    private final List<DoorphoneRingDetectedListener> ringDetectedListeners;
    private final List<DeviceDisconnectedListener> deviceDisconnectedListeners;

    private final AtomicInteger correlationIdCounter;
    private final ConcurrentMap<Integer, CompletableFuture<CommandResponse>> pendingCommandResponses;

    /**
     * Создает новое соединение на базе открытого канала.
     *
     * @param deviceChannel открытый канал сокета IoT устройства
     */
    public DeviceSession(SocketChannel deviceChannel, ExecutorService commandExecutor) {
        this.deviceChannel = deviceChannel;
        this.commandExecutor = commandExecutor;

        this.ringDetectedListeners = new ArrayList<>();
        this.deviceDisconnectedListeners = new ArrayList<>();

        this.correlationIdCounter = new AtomicInteger(0);
        this.pendingCommandResponses = new ConcurrentHashMap<>();
    }

    public CompletableFuture<CommandResponse> sendCommandAsync(Command cmd) {
        var future = new CompletableFuture<CommandResponse>();
        pendingCommandResponses.put(correlationIdCounter.getAndIncrement(), future);

        commandExecutor.submit(() -> sendCommand(cmd));

        return future;
    }

    /**
     * Отправляет команду IoT устройству.
     *
     * @param cmd объект команды
     */
    private void sendCommand(Command cmd) {
        String rawPayload = cmd.getPayload();
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
                        handleOutputMessage(message);
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
     * Разбирает входящее сообщение.
     */
    private void handleOutputMessage(String message) {
        if (message.startsWith("E:")) {
            String[] eventOutputParts = message.split(":");
            var eventType = eventOutputParts[1];

            if (eventType.equals("RING"))
                fireDoorphoneRingDetectedListeners();
        }
        else if (message.startsWith("R:")) {
            String[] commandResponseOutputParts = message.split(":");
            int correlationId = parseInt(commandResponseOutputParts[1]);
            CompletableFuture<CommandResponse> future =
                    pendingCommandResponses.get(correlationId);
            if (future != null)
                future.complete(new CommandResponse(true, commandResponseOutputParts[2]));
            else
                throw new RuntimeException("Future with correlation id %s not found".formatted(correlationId));
        }
    }

    /**
     * Выполняет очистку ресурсов и уведомляет об отключении.
     */
    private void handleDisconnect() {
        try {
            fireDeviceDisconnectedListeners(
                    new DeviceDisconnectedEvent(this, deviceChannel.getRemoteAddress()));
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
