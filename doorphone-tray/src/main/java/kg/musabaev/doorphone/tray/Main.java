package kg.musabaev.doorphone.tray;

import kg.musabaev.doorphone.core.DeviceServer;
import lombok.extern.slf4j.Slf4j;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.util.concurrent.Executors;

@Slf4j
public class Main {

    private final static int PORT = 9126;

    public static void main(String[] args) throws IOException {
        // Инициализация DeviceServer
        var deviceSessionPool = Executors.newSingleThreadExecutor();
        var commandExecutor = Executors.newVirtualThreadPerTaskExecutor();
        var server = new DeviceServer(PORT, deviceSessionPool, commandExecutor);
        server.start();

        // Инициализация mDNS
        var jmdns = JmDNS.create();
        var serviceInfo = ServiceInfo.create(
                "_doorphone._tcp.local.", "doorphone-device-server", PORT, "path=/");
        jmdns.registerService(serviceInfo);

        // Запуск GUI
        var gui = new Gui(server);
        gui.start();

        Thread.currentThread().setUncaughtExceptionHandler((thread, exception) -> {
            log.error("Uncaught exception", exception);
            try {
                server.stop();
                jmdns.close();
            } catch (IOException e) {
                log.error("Error while closing DeviceServer and JmDNS", e);
            }
            System.exit(666);
        });
    }
}
