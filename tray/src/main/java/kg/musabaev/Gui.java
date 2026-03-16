package kg.musabaev;

import java.awt.*;

public class Gui {

    private SystemTray tray;
    private Image image, disabledImage;
    private TrayIcon trayIcon;

    private boolean isEnabled;

    public Gui() {
        initSystemTray();
        initImages();

        setDefaultEnabledIcon();

        isEnabled = true;

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

    private void addListeners() {
        // add on click tray listener
        trayIcon.addActionListener(e -> {
            if (isEnabled) {
                trayIcon.setImage(disabledImage);
                isEnabled = false;
            } else {
                trayIcon.setImage(image);
                isEnabled = true;
            }
        });
    }
}
