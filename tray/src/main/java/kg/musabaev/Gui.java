package kg.musabaev;

import kg.musabaev.event.DeviceConnectedEvent;
import kg.musabaev.event.DeviceDisconnectedEvent;
import kg.musabaev.event.DoorphoneRingDetectedEvent;
import kg.musabaev.event.ServerStartedEvent;
import kg.musabaev.listener.DeviceConnectedListener;
import kg.musabaev.listener.DeviceDisconnectedListener;
import kg.musabaev.listener.DoorphoneRingDetectedListener;
import kg.musabaev.listener.ServerStartedListener;

import java.awt.*;
import java.awt.event.KeyEvent;

import static java.awt.TrayIcon.MessageType.INFO;
import static java.awt.TrayIcon.MessageType.WARNING;

public class Gui implements
        ServerStartedListener,
        DeviceConnectedListener,
        DoorphoneRingDetectedListener,
        DeviceDisconnectedListener {

    private final DeviceEventServer server;

    private SystemTray tray;

    private Image disabledIconImage;
    private Image pendingIconImage;
    private Image runningIconImage;
    private TrayIcon trayIcon;

    private PopupMenu trayPopupMenu;
    private MenuItem pingMenuItem;
    private MenuItem exitMenuItem;

    private boolean isDetectorEnabled;

    public Gui(DeviceEventServer deviceEventServer) {
        this.server = deviceEventServer;

        initSystemTray();
        initImages();

        setDefaultDisabledIcon();
        isDetectorEnabled = true;

        setPopupMenu();

        addListeners();
    }

    private void initSystemTray() {
        if (!SystemTray.isSupported())
            throw new RuntimeException("System tray is not supported");
        tray = SystemTray.getSystemTray();
    }

    private void initImages() {
        var toolkit = Toolkit.getDefaultToolkit();
        disabledIconImage = toolkit.createImage(
                ClassLoader.getSystemResource("tray-icon-disabled.png")
        );
        pendingIconImage = toolkit.createImage(
                ClassLoader.getSystemResource("tray-icon-pending.png")
        );
        runningIconImage = toolkit.createImage(
                ClassLoader.getSystemResource("tray-icon-running.png")
        );
    }

    private void setDefaultDisabledIcon() {
        trayIcon = new TrayIcon(disabledIconImage);
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    private void setPopupMenu() {
        trayPopupMenu = new PopupMenu();
        pingMenuItem = new MenuItem("Ping Device");
        exitMenuItem = new MenuItem("Exit", new MenuShortcut(KeyEvent.VK_Q, false));

        trayPopupMenu.add(pingMenuItem);
        trayPopupMenu.addSeparator();
        trayPopupMenu.add(exitMenuItem);

        trayIcon.setPopupMenu(trayPopupMenu);
    }

    private void addListeners() {
        server.addServerStartedListener(this);
        server.addDeviceConnectedListener(event -> {
            DeviceConnection conn = event.getConnection();
            conn.addDoorphoneRingDetectedListener(this);
            conn.addDeviceDisconnectedListener(this);
        });

        // Отправка Ping через ExecutorService
//        pingMenuItem.addActionListener(e -> {
//            commandExecutor.execute(() -> {
//                try {
////                    server.sendCommand(new PingCommand());
//                    trayIcon.displayMessage("Ping", "Команда отправлена!", TrayIcon.MessageType.INFO);
//                } catch (IllegalStateException | IOException ex) {
//                    trayIcon.displayMessage("Ошибка", ex.getMessage(), TrayIcon.MessageType.ERROR);
//                }
//            });
//        });
//
//        // Выход из приложения
//        exitMenuItem.addActionListener(e -> {
//            commandExecutor.shutdownNow();
//            System.exit(0);
//        });
//
//        // Слушатель звонка домофона
////        deviceEventServer.addDoorphoneRingDetectedListener(event -> {
//            if (isDetectorEnabled) {
//                trayIcon.displayMessage("Doorphone", "Звонок!", TrayIcon.MessageType.INFO);
//            }
//        });
    }

    @Override
    public void onStarted(ServerStartedEvent event) {
        trayIcon.setImage(pendingIconImage);
    }

    @Override
    public void onConnected(DeviceConnectedEvent event) {
        trayIcon.displayMessage(
                "INFO",
                "ESP32-S3 подключился. Его локальный IP адрес: %s".formatted(event.getRemoteAddress()),
                INFO);
        trayIcon.setImage(runningIconImage);
    }

    @Override
    public void onDetected(DoorphoneRingDetectedEvent event) {
        trayIcon.displayMessage("WARNING", "Дядя тебе звонят!", WARNING);
    }

    @Override
    public void onDisconnected(DeviceDisconnectedEvent event) {
        trayIcon.displayMessage("WARNING", "ESP32-S3 дисконнектился", WARNING);
        trayIcon.setImage(pendingIconImage);
    }
}
