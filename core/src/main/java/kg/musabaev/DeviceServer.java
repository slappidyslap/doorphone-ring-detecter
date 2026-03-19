package kg.musabaev;

import kg.musabaev.event.DeviceConnectedEvent;
import kg.musabaev.event.ServerStartedEvent;
import kg.musabaev.listener.DeviceConnectedListener;
import kg.musabaev.listener.ServerStartedListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.util.Objects.requireNonNull;

/**
 * Сервер для приёма подключений от IoT устройства.
 */
public class DeviceServer implements Runnable {

    private final ServerSocketChannel serverChannel;
    private final ExecutorService sessionPool;
    private final ExecutorService commandExecutor;
    private Thread serverThread;
    private volatile boolean isRunning;

    private final List<ServerStartedListener> serverStartedListeners;
    private final List<DeviceConnectedListener> deviceConnectedListeners;

    /**
     * Создает сервер на указанном порту.
     *
     * @param port порт для приема входящих соединений
     * @throws IOException если не удалось привязать сокет к порту
     */
    public DeviceServer(int port, ExecutorService sessionPool, ExecutorService commandExecutor) throws IOException {
        this.serverChannel = ServerSocketChannel.open();
        this.serverChannel.bind(new InetSocketAddress(port));
        this.sessionPool = sessionPool;
        this.commandExecutor = commandExecutor;
        this.isRunning = false;

        this.serverStartedListeners = new ArrayList<>();
        this.deviceConnectedListeners = new ArrayList<>();
    }

    /**
     * Запускает сервер в фоновом потоке.
     */
    public void start() {
        if (isRunning) throw new RuntimeException("Server already is running"); // todo
        isRunning = true;

        serverThread = Thread
                .ofVirtual()
                .name("doorphone-ring-detector-server")
                .factory()
                .newThread(this);
        serverThread.start();
    }

    @Override
    public void run() {
        fireServerStartedListeners(new ServerStartedEvent());
        acceptLoop();
    }

    /**
     * Основной цикл приема соединений.
     */
    private void acceptLoop() {
        while (isRunning) {
            DeviceSession deviceSession;
            try {
                // Блокируемся, пока IoT устройство не подключится
                SocketChannel deviceClient = serverChannel.accept();
                deviceSession = new DeviceSession(deviceClient, commandExecutor);

                fireDeviceConnectedListeners(
                        new DeviceConnectedEvent(deviceSession, deviceClient.getRemoteAddress()));

                sessionPool.submit(deviceSession);
            } catch (IOException e) {
                // TODO
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Добавляет слушателя старт сервера.
     */
    public void addServerStartedListener(ServerStartedListener listener) {
        serverStartedListeners.add(requireNonNull(listener));
    }

    /**
     * Уведомить всех слушателей старт сервера.
     */
    private void fireServerStartedListeners(ServerStartedEvent event) {
        serverStartedListeners.forEach(l -> l.onStarted(event));
    }

    /**
     * Добавляет слушателя подключения соединения.
     */
    public void addDeviceConnectedListener(DeviceConnectedListener listener) {
        deviceConnectedListeners.add(requireNonNull(listener));
    }

    /**
     * Уведомить всех слушателей подключения соединения.
     */
    private void fireDeviceConnectedListeners(DeviceConnectedEvent event) {
        deviceConnectedListeners.forEach(l -> l.onConnected(event));
    }

    /**
     * Останавливает сервер и закрывает слушающий сокет.
     *
     * @throws IOException если произошла ошибка при закрытии канала
     */
    public void stop() throws IOException {
        isRunning = false;
        serverChannel.close();
        serverThread.interrupt(); // чек
        sessionPool.shutdown();
    }
}
