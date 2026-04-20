package io.github.phonchaidev.watermark;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;

/**
 * Utility class to install watermarks on Windows (JFrame, JDialog).
 * Supports per-window install, global auto-install, and runtime config
 * switching.
 *
 * <h3>Implementation Strategy</h3>
 * <p>Instead of wrapping the window's {@code contentPane} with a
 * {@link JLayer}, the watermark is added as a {@link WatermarkOverlayPanel}
 * directly to the window's {@link JLayeredPane} at z-order
 * {@code DRAG_LAYER + 100} (= 500).  This ensures the watermark is drawn
 * <em>above every Swing layer</em> — including third-party modal overlays
 * such as dj-raven {@code modal-dialog} — without interfering with their
 * internal component hierarchy.
 *
 * <h3>Why the old JLayer approach failed</h3>
 * <p>The previous implementation wrapped the {@code contentPane} with a
 * {@code JLayer}.  The dj-raven modal-dialog renders its overlay in the
 * {@code JLayeredPane} at {@code MODAL_LAYER} (200), which is <em>above</em>
 * the {@code contentPane}, so the watermark was hidden behind the modal.
 *
 * <h3>Global mode restriction</h3>
 * <p>The global listener only targets {@link JFrame} and {@link JDialog}.
 * {@link JWindow} and other {@link RootPaneContainer} subtypes used
 * internally by UI libraries (e.g. popup helpers) are intentionally excluded.
 */
public class WatermarkInstaller {

    /** Client-property key used to store the overlay on the root pane. */
    private static final String OVERLAY_KEY = "WatermarkInstaller.overlay";

    /** Client-property key used to store the resize listener on the root pane. */
    private static final String LISTENER_KEY = "WatermarkInstaller.listener";

    private static AWTEventListener globalListener = null;

    /**
     * Installs or re-installs the watermark on a specific window.
     *
     * <p>A transparent {@link WatermarkOverlayPanel} is added to the window's
     * {@link JLayeredPane} at z-order 500 (above {@code DRAG_LAYER}).
     * Mouse events pass through the overlay so UI components remain fully
     * interactive.
     *
     * @param window The {@link JFrame} or {@link JDialog} to watermark.
     * @param config The watermark configuration.
     */
    public static void install(RootPaneContainer window, WatermarkConfig config) {
        if (window == null) {
            return;
        }

        JRootPane rootPane = window.getRootPane();
        JLayeredPane layeredPane = rootPane.getLayeredPane();
        Container contentPane = window.getContentPane();

        // --- Remove existing overlay and its resize listener ---
        WatermarkOverlayPanel oldOverlay =
                (WatermarkOverlayPanel) rootPane.getClientProperty(OVERLAY_KEY);
        if (oldOverlay != null) {
            layeredPane.remove(oldOverlay);
        }

        ComponentListener oldListener =
                (ComponentListener) rootPane.getClientProperty(LISTENER_KEY);
        if (oldListener != null) {
            rootPane.removeComponentListener(oldListener);
        }

        // --- Create new transparent overlay ---
        WatermarkOverlayPanel overlay = new WatermarkOverlayPanel(config);

        // Layer 500: above DEFAULT(0) PALETTE(100) MODAL(200) POPUP(300) DRAG(400)
        // FlatLaf title pane sits at FRAME_CONTENT_LAYER (-30000) — still below 500,
        // but we intentionally match contentPane bounds so the title bar is not covered.
        layeredPane.add(overlay, Integer.valueOf(JLayeredPane.DRAG_LAYER + 100));

        // Sync overlay bounds to the contentPane (which is a direct child of the
        // JLayeredPane).  contentPane.getBounds() returns its position within the
        // layeredPane, already excluding the FlatLaf/OS title bar at the top.
        Runnable syncBounds = () -> {
            overlay.setBounds(contentPane.getBounds());
            overlay.repaint();
        };

        // invokeLater ensures the layout is fully computed before we read bounds
        SwingUtilities.invokeLater(syncBounds);

        // Re-sync on every window resize
        ComponentListener listener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                syncBounds.run();
            }
        };
        rootPane.addComponentListener(listener);

        // Persist references for future reinstall/cleanup
        rootPane.putClientProperty(OVERLAY_KEY, overlay);
        rootPane.putClientProperty(LISTENER_KEY, listener);

        layeredPane.revalidate();
        layeredPane.repaint();
    }

    /**
     * Automatically installs the watermark on ALL current and FUTURE windows
     * (Enterprise Mode).
     *
     * <p>Only {@link JFrame} and {@link JDialog} instances are targeted;
     * internal framework windows ({@link JWindow}, etc.) are skipped to
     * prevent interfering with third-party libraries.
     *
     * @param config The watermark configuration to apply globally.
     */
    public static void installGlobal(WatermarkConfig config) {
        // Install on all currently open top-level windows
        for (Window window : Window.getWindows()) {
            if (isTargetWindow(window)) {
                install((RootPaneContainer) window, config);
            }
        }

        // Remove old global listener if exists
        if (globalListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(globalListener);
        }

        // Listen for future windows — JFrame / JDialog only
        globalListener = event -> {
            if (event.getID() == WindowEvent.WINDOW_OPENED) {
                Object source = event.getSource();
                if (isTargetWindow(source)) {
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

    /**
     * Returns {@code true} only for top-level user windows that are safe to
     * watermark ({@link JFrame} or {@link JDialog}).
     *
     * <p>{@link JWindow} and other {@link RootPaneContainer} subtypes are
     * excluded because third-party UI libraries (e.g. dj-raven modal-dialog)
     * use them as overlay containers whose structure must not be altered.
     */
    private static boolean isTargetWindow(Object source) {
        return source instanceof JFrame || source instanceof JDialog;
    }
}
