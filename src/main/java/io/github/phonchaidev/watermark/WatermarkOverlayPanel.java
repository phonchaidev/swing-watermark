package io.github.phonchaidev.watermark;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * A transparent overlay panel that paints a repeating watermark above ALL
 * Swing components — including third-party modal dialogs (e.g. dj-raven
 * modal-dialog) — without blocking mouse or keyboard interaction.
 *
 * <p>This panel is added to the window's {@link JLayeredPane} at z-order
 * {@code DRAG_LAYER + 100} (= 500), which places it above every standard
 * Swing layer:
 * <ul>
 *   <li>DEFAULT_LAYER  = 0</li>
 *   <li>PALETTE_LAYER  = 100</li>
 *   <li>MODAL_LAYER    = 200  ← dj-raven modal</li>
 *   <li>POPUP_LAYER    = 300</li>
 *   <li>DRAG_LAYER     = 400</li>
 *   <li><b>WatermarkOverlayPanel = 500</b></li>
 * </ul>
 *
 * <p>Mouse events pass through because {@link #contains(int, int)} always
 * returns {@code false}.
 */
public class WatermarkOverlayPanel extends JPanel {

    private final WatermarkConfig config;

    public WatermarkOverlayPanel(WatermarkConfig config) {
        this.config = config != null ? config : new WatermarkConfig();
        setOpaque(false);
        setFocusable(false);
    }

    /**
     * Always returns {@code false} so that every mouse event is forwarded to
     * the component below this overlay.
     */
    @Override
    public boolean contains(int x, int y) {
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!config.isEnabled()) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, config.getOpacity()));

        int width = getWidth();
        int height = getHeight();
        int maxDim = (int) Math.sqrt((double) width * width + (double) height * height) + 200;

        AffineTransform oldTransform = g2d.getTransform();
        g2d.rotate(config.getAngle(), width / 2.0, height / 2.0);

        if (config.isImageMode()) {
            paintImageWatermark(g2d, width, height, maxDim);
        } else {
            paintTextWatermark(g2d, width, height, maxDim);
        }

        g2d.setTransform(oldTransform);
        g2d.dispose();
    }

    private void paintTextWatermark(Graphics2D g2d, int width, int height, int maxDim) {
        g2d.setColor(config.getColor());
        g2d.setFont(config.getFont());

        String[] lines = config.getTextLines();
        if (lines.length == 0 || (lines.length == 1 && lines[0].trim().isEmpty())) {
            return;
        }

        FontRenderContext frc = g2d.getFontRenderContext();
        Font font = config.getFont();

        double maxLineWidth = 0;
        double lineHeight = font.getStringBounds("Ag", frc).getHeight();
        for (String line : lines) {
            Rectangle2D bounds = font.getStringBounds(line, frc);
            maxLineWidth = Math.max(maxLineWidth, bounds.getWidth());
        }
        double totalTextHeight = lineHeight * lines.length;

        int startX = -maxDim / 2;
        int endX   = width + maxDim / 2;
        int startY = -maxDim / 2;
        int endY   = height + maxDim / 2;

        int stepX = (int) maxLineWidth + config.getHorizontalSpacing();
        int stepY = (int) totalTextHeight + config.getVerticalSpacing();

        int rowIndex = 0;
        for (int y = startY; y < endY; y += stepY) {
            int offset = (rowIndex % 2 == 0) ? 0 : stepX / 2;
            for (int x = startX - offset; x < endX; x += stepX) {
                for (int i = 0; i < lines.length; i++) {
                    g2d.drawString(lines[i], x, (int) (y + (i * lineHeight)));
                }
            }
            rowIndex++;
        }
    }

    private void paintImageWatermark(Graphics2D g2d, int width, int height, int maxDim) {
        BufferedImage img = config.getImage();
        if (img == null) {
            return;
        }

        int imgW = config.getImageWidth();
        int imgH = config.getImageHeight();

        int startX = -maxDim / 2;
        int endX   = width + maxDim / 2;
        int startY = -maxDim / 2;
        int endY   = height + maxDim / 2;

        int stepX = imgW + config.getHorizontalSpacing();
        int stepY = imgH + config.getVerticalSpacing();

        int rowIndex = 0;
        for (int y = startY; y < endY; y += stepY) {
            int offset = (rowIndex % 2 == 0) ? 0 : stepX / 2;
            for (int x = startX - offset; x < endX; x += stepX) {
                g2d.drawImage(img, x, y, imgW, imgH, null);
            }
            rowIndex++;
        }
    }
}
