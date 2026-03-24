package kg.musabaev.doorphone.tray;

import kg.musabaev.doorphone.core.DeviceServer;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.util.concurrent.Executors;

public class Main {

    private final static int PORT = 9126;

    public static void main(String[] args) throws IOException {
        DeviceServer server = initDeviceServer();
        server.start();

        var jmdns = JmDNS.create();
        var serviceInfo = ServiceInfo.create(
                "_doorphone._tcp.local.", "doorphone-device-server", PORT, "path=/");
        jmdns.registerService(serviceInfo);

        var gui = new Gui(server);
        gui.start();
    }

    private static DeviceServer initDeviceServer() throws IOException {
        var deviceSessionPool = Executors.newSingleThreadExecutor();
        var commandExecutor = Executors.newVirtualThreadPerTaskExecutor();
        return new DeviceServer(PORT, deviceSessionPool, commandExecutor);
    }
}
