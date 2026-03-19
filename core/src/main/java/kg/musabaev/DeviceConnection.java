package kg.musabaev;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Инкапсулирует подключение к IoT устройству и предоставляет методы
 * для отправки команд и получения ответов.
 */
public class DeviceConnection {

    private final AsynchronousSocketChannel channel;

    public DeviceConnection(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }

    /**
     * Отправляет команду устройству и возвращает ответ асинхронно.
     *
     * @param command команда для отправки (например, "PING")
     * @return CompletableFuture с ответом от устройства
     */
    public CompletableFuture<String> sendCommand(String command) {
        CompletableFuture<String> future = new CompletableFuture<>();

        ByteBuffer writeBuffer = ByteBuffer.wrap(command.getBytes(StandardCharsets.UTF_8));
        ByteBuffer readBuffer = ByteBuffer.allocate(64);

        channel.write(writeBuffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer bytesRead, Void attachment) {
                channel.read(readBuffer, null, new CompletionHandler<Integer, Void>() {
                    @Override
                    public void completed(Integer bytesRead, Void attachment) {
                        readBuffer.flip();
                        String response = new String(readBuffer.array(), 0, bytesRead, StandardCharsets.UTF_8);
                        future.complete(response);
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        future.completeExceptionally(exc);
                    }
                });
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                future.completeExceptionally(exc);
            }
        });

        return future;
    }

    /**
     * Начинает асинхронное чтение событий от устройства.
     *
     * @param eventHandler обработчик полученных событий
     */
    public void startListening(Consumer<String> eventHandler) {
        readNextEvent(eventHandler);
    }

    private void readNextEvent(Consumer<String> eventHandler) {
        ByteBuffer buffer = ByteBuffer.allocate(64);

        channel.read(buffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer bytesRead, Void attachment) {
                if (bytesRead == -1) {
                    // Конец потока - подключение закрыто
                    return;
                }
                buffer.flip();
                String event = new String(buffer.array(), 0, bytesRead, StandardCharsets.UTF_8);
                eventHandler.accept(event);
                readNextEvent(eventHandler);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                // TODO
            }
        });
    }

    public boolean isOpen() {
        return channel.isOpen();
    }

    public void close() throws IOException {
        channel.close();
    }
}
