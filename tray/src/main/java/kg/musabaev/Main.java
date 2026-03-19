package kg.musabaev;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        DeviceEventServer server = new DeviceEventServer(8080);
        server.start();

        new Gui(server);
        System.out.println("код после new Gui();");
    }
}
