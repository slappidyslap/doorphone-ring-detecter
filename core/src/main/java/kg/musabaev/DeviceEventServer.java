package kg.musabaev;

import kg.musabaev.event.DeviceConnectedEvent;
import kg.musabaev.event.ServerStartedEvent;
import kg.musabaev.listener.DeviceConnectedListener;
import kg.musabaev.listener.ServerStartedListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Сервер для приёма подключений от IoT устройства.
 */
public class DeviceEventServer {

    private final ServerSocketChannel serverChannel;
    private volatile boolean isRunning;

    private final List<ServerStartedListener> serverStartedListeners;
    private final List<DeviceConnectedListener> deviceConnectedListeners;

    /**
     * Создает сервер на указанном порту.
     *
     * @param port порт для приема входящих соединений
     * @throws IOException если не удалось привязать сокет к порту
     */
    public DeviceEventServer(int port) throws IOException {
        this.serverChannel = ServerSocketChannel.open();
        this.serverChannel.bind(new InetSocketAddress(port));

        this.serverStartedListeners = new ArrayList<>();
        this.deviceConnectedListeners = new ArrayList<>();

        this.isRunning = false;
    }

    /**
     * Запускает сервер в фоновом потоке.
     */
    public void start() {
        if (isRunning) throw new RuntimeException("Server already is running"); // todo
        isRunning = true;

        var serverThread = new Thread(this::acceptLoop, "doorphone-ring-detector-server");
        serverThread.setDaemon(true);
        serverThread.start();

        fireServerStartedListeners(new ServerStartedEvent());
    }

    /**
     * Основной цикл приема соединений.
     */
    private void acceptLoop() {
        while (isRunning) {
            DeviceConnection deviceConn;
            try {
                SocketChannel deviceClient = serverChannel.accept();
                SocketAddress remoteAddress = deviceClient.getRemoteAddress();
                deviceConn = new DeviceConnection(deviceClient);

                fireDeviceConnectedListeners(new DeviceConnectedEvent(deviceConn, remoteAddress));
            } catch (IOException e) {
                // TODO
                throw new RuntimeException(e);
            }
            deviceConn.start();
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
    }
}
