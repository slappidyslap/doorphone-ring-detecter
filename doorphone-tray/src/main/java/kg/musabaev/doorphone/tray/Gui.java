package kg.musabaev.doorphone.tray;

import ch.qos.logback.classic.LoggerContext;
import kg.musabaev.doorphone.core.DeviceServer;
import kg.musabaev.doorphone.core.DeviceSession;
import kg.musabaev.doorphone.core.command.PingCommand;
import kg.musabaev.doorphone.core.event.DeviceConnectedEvent;
import kg.musabaev.doorphone.core.event.DeviceDisconnectedEvent;
import kg.musabaev.doorphone.core.event.DoorphoneRingDetectedEvent;
import kg.musabaev.doorphone.core.event.ServerStartedEvent;
import kg.musabaev.doorphone.core.listener.DeviceConnectedListener;
import kg.musabaev.doorphone.core.listener.DeviceDisconnectedListener;
import kg.musabaev.doorphone.core.listener.DoorphoneRingDetectedListener;
import kg.musabaev.doorphone.core.listener.ServerStartedListener;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;

import static java.awt.TrayIcon.MessageType.*;

@Slf4j
public class Gui implements
        ServerStartedListener,
        DeviceConnectedListener,
        DoorphoneRingDetectedListener,
        DeviceDisconnectedListener {

    private final DeviceServer deviceServer;
    private DeviceSession deviceSession;

    private SystemTray tray;

    private TrayIcon trayIcon;
    private Image disabledIconImage;
    private Image pendingIconImage;
    private Image runningIconImage;

    private PopupMenu trayPopupMenu;
    private MenuItem pingMenuItem;
    private MenuItem openLogsMenuItem;
    private MenuItem exitMenuItem;

    public Gui(DeviceServer deviceServer) {
        this.deviceServer = deviceServer;
        this.deviceSession = null;
        initSystemTray();
        initTrayImages();
        setDefaultDisabledIcon();
        initTrayPopupMenu();
        initListeners();
    }

    public void start() {
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    private void initSystemTray() {
        if (!SystemTray.isSupported())
            throw new RuntimeException("System tray is not supported");
        tray = SystemTray.getSystemTray();
    }

    private void initTrayImages() {
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
    }

    private void initTrayPopupMenu() {
        trayPopupMenu = new PopupMenu();
        pingMenuItem = new MenuItem("Ping Device", new MenuShortcut(KeyEvent.VK_P));
        openLogsMenuItem = new MenuItem("Open logs", new MenuShortcut(KeyEvent.VK_L));
        exitMenuItem = new MenuItem("Exit", new MenuShortcut(KeyEvent.VK_Q, false));

        trayPopupMenu.add(pingMenuItem);
        trayPopupMenu.addSeparator();
        trayPopupMenu.add(openLogsMenuItem);
        trayPopupMenu.add(exitMenuItem);

        trayIcon.setPopupMenu(trayPopupMenu);
    }

    private void initListeners() {
        deviceServer.addServerStartedListener(this);
        deviceServer.addDeviceConnectedListener(event -> {
            deviceSession = event.getSession();
            deviceSession.addDoorphoneRingDetectedListener(this);
            deviceSession.addDeviceDisconnectedListener(this);
        });

        pingMenuItem.addActionListener(e -> deviceSession
                .sendCommandAsync(new PingCommand())
                .thenAccept(resp -> {
                    if (resp.isOk()) {
                        SwingUtilities.invokeLater(() ->
                                trayDisplayMessage(INFO, resp.getData())
                        );
                    } else {
                        SwingUtilities.invokeLater(() ->
                                trayDisplayMessage(ERROR, "No response")
                        );
                    }
                })
        );

        openLogsMenuItem.addActionListener(e -> {
            var context = (LoggerContext) LoggerFactory.getILoggerFactory();
            String curTimestamp = context.getProperty("currentTimestamp");
            try {
                Path logFilePath = userHomePath()
                        .resolve(".doorphone-ring-detector")
                        .resolve("logs")
                        .resolve("log-%s.log".formatted(curTimestamp));
                Desktop.getDesktop().open(logFilePath.toFile());
                log.info("Current log file opened");
            } catch (IOException ex) {
                log.error("Ошибка при попытке открыть текущего лог файла", ex);
                trayDisplayMessage(ERROR, "Unable to open logs.");
            }
        });

        exitMenuItem.addActionListener(e -> {
            try {
                deviceServer.stop();
            } catch (IOException ignored) {
            }
            System.exit(0);
        });
    }

    @Override
    public void onStarted(ServerStartedEvent event) {
        trayIcon.setImage(pendingIconImage);
    }

    @Override
    public void onConnected(DeviceConnectedEvent event) {
        trayDisplayMessage(INFO, "ESP32-S3 подключился. Его локальный IP адрес: %s".formatted(event.getRemoteAddress()));
        trayIcon.setImage(runningIconImage);
    }

    @Override
    public void onDetected(DoorphoneRingDetectedEvent event) {
        trayDisplayMessage(WARNING, "Дядя тебе звонят!");
    }

    @Override
    public void onDisconnected(DeviceDisconnectedEvent event) {
        trayDisplayMessage(WARNING, "ESP32-S3 дисконнектился");
        trayIcon.setImage(pendingIconImage);
    }

    private void trayDisplayMessage(TrayIcon.MessageType type, String msg) {
        trayIcon.displayMessage(type.name(), msg, type);
    }

    private Path userHomePath() {
        return Path.of(System.getProperty("user.home"));
    }
}
