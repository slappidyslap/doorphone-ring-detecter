package kg.musabaev;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Gui {

    private final DeviceEventServer deviceEventServer;
    private final ExecutorService commandExecutor;

    private SystemTray tray;

    private Image image, disabledImage;
    private TrayIcon trayIcon;

    private PopupMenu trayPopupMenu;
    private MenuItem pingMenuItem;
    private MenuItem exitMenuItem;

    private boolean isDetectorEnabled;

    public Gui(DeviceEventServer deviceEventServer) {
        this.deviceEventServer = deviceEventServer;
        this.commandExecutor = Executors.newSingleThreadExecutor();

        deviceEventServer.addDeviceConnectedListener(event -> {
            DeviceConnection conn = event.getConnection();
//            conn.addDoorphoneRingDetectedListener(this);
        });

        initSystemTray();
        initImages();

        setDefaultEnabledIcon();
        isDetectorEnabled = true;

        setPopupMenu();

        addListeners();

        trayIcon.displayMessage("Doorphone Detector", "Сервер запущен и готов к работе!", TrayIcon.MessageType.INFO);
    }

    private void initSystemTray() {
        if (!SystemTray.isSupported())
            throw new RuntimeException("System tray is not supported");
        tray = SystemTray.getSystemTray();
    }

    private void initImages() {
        var toolkit = Toolkit.getDefaultToolkit();
        image = toolkit.createImage(
                ClassLoader.getSystemResource("tray-icon.png")
        );

        disabledImage = toolkit.createImage(
                ClassLoader.getSystemResource("tray-icon-disabled.png")
        );
    }

    private void setDefaultEnabledIcon() {
        trayIcon = new TrayIcon(image);
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
        // Переключение иконки и статуса
        trayIcon.addActionListener(e -> {
            if (isDetectorEnabled) {
                trayIcon.setImage(disabledImage);
                isDetectorEnabled = false;
            } else {
                trayIcon.setImage(image);
                isDetectorEnabled = true;
            }
        });

        // Отправка Ping через ExecutorService
//        pingMenuItem.addActionListener(e -> {
//            commandExecutor.execute(() -> {
//                try {
////                    deviceEventServer.sendCommand(new PingCommand());
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
}
