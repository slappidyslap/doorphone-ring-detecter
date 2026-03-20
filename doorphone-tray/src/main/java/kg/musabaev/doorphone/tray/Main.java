package kg.musabaev.doorphone.tray;

import kg.musabaev.doorphone.core.DeviceServer;

import java.io.IOException;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws IOException {
//        ThreadFactory threadFactory = runnable -> {
//            var thread = new Thread(runnable);
//            thread.setName("device");
//            return thread;
//        }
        var deviceSessionPool = Executors.newSingleThreadExecutor();
        var commandExecutor = Executors.newVirtualThreadPerTaskExecutor();
        var server = new DeviceServer(9156, deviceSessionPool, commandExecutor);
        server.start();

        var gui = new Gui(server);
        gui.start();
    }
}
