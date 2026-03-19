package kg.musabaev;

import java.io.IOException;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws IOException {
//        ThreadFactory threadFactory = runnable -> {
//            var thread = new Thread(runnable);
//            thread.setName("device");
//            return thread;
//        }
        var deviceConnPool = Executors.newSingleThreadExecutor();
        var server = new DeviceServer(8080, deviceConnPool);
        server.start();

        new Gui(server);
    }
}
