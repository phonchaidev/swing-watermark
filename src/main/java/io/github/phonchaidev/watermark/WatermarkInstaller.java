package io.github.phonchaidev.watermark;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;

/**
 * Utility class to install watermarks on Windows (JFrame, JDialog).
 * Supports per-window install, global auto-install, and runtime config
 * switching.
 */
public class WatermarkInstaller {

    private static AWTEventListener globalListener = null;

    /**
     * Installs or re-installs the watermark on a specific window.
     * If the window already has a watermark, it will be replaced with the new
     * config.
     *
     * @param window The JFrame or JDialog to watermark.
     * @param config The watermark configuration.
     * @return The JLayer wrapping the original content.
     */
    @SuppressWarnings("unchecked")
    public static JLayer<JComponent> install(RootPaneContainer window, WatermarkConfig config) {
        if (window == null)
            return null;

        Container contentPane = window.getContentPane();
        WatermarkLayerUI layerUI = new WatermarkLayerUI(config);

        // If already wrapped in JLayer, unwrap first then re-wrap with new config
        if (contentPane instanceof JLayer) {
            JLayer<JComponent> existingLayer = (JLayer<JComponent>) contentPane;
            JComponent originalContent = (JComponent) existingLayer.getView();

            JLayer<JComponent> newLayer = new JLayer<>(originalContent, layerUI);
            window.setContentPane(newLayer);

            if (window instanceof Window) {
                ((Window) window).revalidate();
                ((Window) window).repaint();
            }
            return newLayer;
        }

        // Fresh install
        JLayer<JComponent> layer = new JLayer<>((JComponent) contentPane, layerUI);
        window.setContentPane(layer);

        if (window instanceof Window) {
            ((Window) window).revalidate();
            ((Window) window).repaint();
        }

        return layer;
    }

    /**
     * Automatically installs the watermark on ALL current and FUTURE windows
     * (Enterprise Mode).
     *
     * @param config The watermark configuration to apply globally.
     */
    public static void installGlobal(WatermarkConfig config) {
        // Install on all currently open windows
        for (Window window : Window.getWindows()) {
            if (window instanceof RootPaneContainer) {
                install((RootPaneContainer) window, config);
            }
        }

        // Remove old listener if exists
        if (globalListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(globalListener);
        }

        // Setup listener for future windows
        globalListener = event -> {
            if (event.getID() == WindowEvent.WINDOW_OPENED) {
                Object source = event.getSource();
                if (source instanceof RootPaneContainer) {
                    install((RootPaneContainer) source, config);
                }
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(globalListener, AWTEvent.WINDOW_EVENT_MASK);
    }

    /**
     * Removes the global installer.
     */
    public static void uninstallGlobal() {
        if (globalListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(globalListener);
            globalListener = null;
        }
    }
}
