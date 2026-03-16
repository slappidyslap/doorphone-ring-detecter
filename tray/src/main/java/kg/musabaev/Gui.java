package kg.musabaev;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Gui {

    private SystemTray tray;

    private Image image, disabledImage;
    private TrayIcon trayIcon;

    private PopupMenu trayPopupMenu;
    private MenuItem exitMenuItem;

    private boolean isDetectorEnabled;

    public Gui() {
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

        trayPopupMenu.add(exitMenuItem);
        trayIcon.setPopupMenu(trayPopupMenu);
    }

    private void addListeners() {
        // add on click tray listener
        trayIcon.addActionListener(e -> {
            if (isDetectorEnabled) {
                trayIcon.setImage(disabledImage);
                isDetectorEnabled = false;
            } else {
                trayIcon.setImage(image);
                isDetectorEnabled = true;
            }
        });

        exitMenuItem.addActionListener(e -> System.exit(0));
    }
}
