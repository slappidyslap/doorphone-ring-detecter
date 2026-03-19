package kg.musabaev;

import kg.musabaev.event.DoorphoneRingDetectedEvent;
import kg.musabaev.listener.DoorphoneRingDetectedListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.util.Objects.requireNonNull;

/**
 * Сервер, что слушает входящие события от IoT устройства и уведомляет слушателей.
 */
public class DeviceEventServer {

    private final AsynchronousServerSocketChannel channel;

    private DeviceConnection deviceConnection;

    // ========== Listeners ==========
    private final List<DoorphoneRingDetectedListener> doorphoneRingDetectedListeners;

    /**
     * Создаёт сервер и начинает прослушивание входящих подключений от IoT устройства.
     *
     * @param serverPort порт, на котором сервер принимает подключения
     * @param executor   пул потоков для обработки асинхронных I/O операций
     * @throws RuntimeException TODO
     */
    public DeviceEventServer(int serverPort, ExecutorService executor) {
        try {
            var group = AsynchronousChannelGroup.withThreadPool(executor);
            channel = AsynchronousServerSocketChannel.open(group);
            channel.bind(new InetSocketAddress(serverPort));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        doorphoneRingDetectedListeners = new ArrayList<>();

        acceptDevice();
    }

    private void acceptDevice() {
        channel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel device, Void attachment) {
                deviceConnection = new DeviceConnection(device);
                deviceConnection.startListening(event -> {
                    if ("RING".equals(event)) {
                        fireDoorphoneRingDetectedListeners();
                    }
                });
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                throw new RuntimeException(exc); // TODO
            }
        });
    }

    /**
     * Добавить слушателя события звонка домофона.
     * <p>
     * Слушатель будет вызван при получении события {@code RING} от устройства.
     *
     * @param listener слушатель, не может быть {@code null}
     * @see DoorphoneRingDetectedListener
     * @see #fireDoorphoneRingDetectedListeners()
     */
    public void addDoorphoneRingDetectedListener(DoorphoneRingDetectedListener listener) {
        doorphoneRingDetectedListeners.add(requireNonNull(listener));
    }

    /**
     * Уведомить всех слушателей звонка домофона.
     */
    private void fireDoorphoneRingDetectedListeners() {
        doorphoneRingDetectedListeners.forEach(
                l -> l.onDetected(new DoorphoneRingDetectedEvent()));
    }

    /**
     * Проверяет, подключено ли устройство.
     */
    public boolean isDeviceConnected() {
        return deviceConnection != null && deviceConnection.isOpen();
    }

    public void close() throws IOException {
        if (deviceConnection != null) {
            deviceConnection.close();
        }
        channel.close();
    }
}
