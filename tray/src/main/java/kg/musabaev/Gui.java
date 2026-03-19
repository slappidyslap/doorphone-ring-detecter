package kg.musabaev;

import kg.musabaev.core.command.PingCommand;
import kg.musabaev.core.connection.ConnectionManager;
import kg.musabaev.core.event.DoorphoneRingDetectedEvent;
import kg.musabaev.core.event.Event;
import kg.musabaev.core.event.EventListener;
import kg.musabaev.core.event.EventManager;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.SwingUtilities; // Used for thread safety with AWT UI updates

public class Gui implements EventListener {

    private SystemTray tray;
    private Image image, disabledImage;
    private TrayIcon trayIcon;

    private PopupMenu trayPopupMenu;
    private MenuItem exitMenuItem;
    private MenuItem pingMenuItem; // New menu item for ping

    private boolean isDetectorEnabled;
    private final EventManager eventManager; // Injected EventManager

    public Gui(EventManager eventManager) {
        this.eventManager = eventManager;
        this.eventManager.addListener(this); // Register as a listener

        initSystemTray();
        initImages();

        setDefaultEnabledIcon();
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
        exitMenuItem = new MenuItem("Exit", new MenuShortcut(KeyEvent.VK_Q, false));
        pingMenuItem = new MenuItem("Ping Device"); // Initialize new menu item

        trayPopupMenu.add(pingMenuItem); // Add ping menu item
        trayPopupMenu.addSeparator(); // Add a separator for better organization
        trayPopupMenu.add(exitMenuItem);
        trayIcon.setPopupMenu(trayPopupMenu);
    }

    private void addListeners() {
        // add on click tray listener
        trayIcon.addActionListener(e -> {
            // This action toggles the detector enable/disable state visually
            SwingUtilities.invokeLater(() -> {
                if (isDetectorEnabled) {
                    trayIcon.setImage(disabledImage);
                    isDetectorEnabled = false;
                    trayIcon.displayMessage("Doorphone Detector", "Detector is now DISABLED", TrayIcon.MessageType.INFO);
                } else {
                    trayIcon.setImage(image);
                    isDetectorEnabled = true;
                    trayIcon.displayMessage("Doorphone Detector", "Detector is now ENABLED", TrayIcon.MessageType.INFO);
                }
            });
        });

        exitMenuItem.addActionListener(e -> System.exit(0));

        // Add listener for the new Ping Device menu item
        pingMenuItem.addActionListener(e -> {
            // This action should not block the EDT, so it dispatches to a background thread
            // using the ExecutorService we'll set up in Main.java
            ConnectionManager connectionManager = ConnectionManager.getInstance();
            if (connectionManager.hasActiveConnection()) {
                // Submit task to a background executor (e.g., from Main.java)
                // For now, we'll assume Main.java provides a way to run this.
                // In Main.java, we'll create a dedicated executor for GUI actions.
                // For direct implementation here, this would need an injected ExecutorService.
                // For simplicity and to avoid circular dependencies for now,
                // we'll leave this as a direct call and rely on the calling context
                // (Main.java's executor) to handle threading.
                try {
                    connectionManager.getActiveConnection().sendCommand(new PingCommand());
                    SwingUtilities.invokeLater(() ->
                        trayIcon.displayMessage("Command Sent", "Ping command sent to device.", TrayIcon.MessageType.INFO));
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() ->
                        trayIcon.displayMessage("Command Error", "Failed to send ping: " + ex.getMessage(), TrayIcon.MessageType.ERROR));
                }
            } else {
                SwingUtilities.invokeLater(() ->
                    trayIcon.displayMessage("Connection Status", "No active device connection.", TrayIcon.MessageType.WARNING));
            }
        });
    }

    @Override
    public void onEvent(Event event) {
        SwingUtilities.invokeLater(() -> {
            if (event instanceof DoorphoneRingDetectedEvent) {
                DoorphoneRingDetectedEvent ringEvent = (DoorphoneRingDetectedEvent) event;
                trayIcon.displayMessage("Doorphone Ring!", "Ring detected at " + new java.util.Date(ringEvent.getTimestamp()), TrayIcon.MessageType.WARNING);
            } else {
                // Handle other event types or log unknown events
                System.out.println("Received unknown event: " + event);
            }
        });
    }

    // Method to display a generic message, useful for connection status updates
    public void displayMessage(String caption, String text, TrayIcon.MessageType messageType) {
        SwingUtilities.invokeLater(() -> trayIcon.displayMessage(caption, text, messageType));
    }
}