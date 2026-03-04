package io.github.phonchaidev.watermark;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * A LayerUI implementation that paints a repeating watermark over a component.
 * Supports text (single-line and multi-line), image watermarks, dynamic
 * placeholders,
 * and runtime enable/disable toggle.
 */
public class WatermarkLayerUI extends LayerUI<JComponent> {

    private final WatermarkConfig config;

    public WatermarkLayerUI(WatermarkConfig config) {
        this.config = config != null ? config : new WatermarkConfig();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);

        if (!config.isEnabled()) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();

        // Anti-Aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Opacity
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, config.getOpacity()));

        int width = c.getWidth();
        int height = c.getHeight();
        int maxDim = (int) Math.sqrt(width * width + height * height) + 200;

        // Rotate around center
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

        // Calculate the bounding box for multi-line text
        double maxLineWidth = 0;
        double totalTextHeight = 0;
        double lineHeight = font.getStringBounds("Ag", frc).getHeight();

        for (String line : lines) {
            Rectangle2D bounds = font.getStringBounds(line, frc);
            maxLineWidth = Math.max(maxLineWidth, bounds.getWidth());
        }
        totalTextHeight = lineHeight * lines.length;

        int startX = -maxDim / 2;
        int endX = width + maxDim / 2;
        int startY = -maxDim / 2;
        int endY = height + maxDim / 2;

        int stepX = (int) maxLineWidth + config.getHorizontalSpacing();
        int stepY = (int) totalTextHeight + config.getVerticalSpacing();

        int rowIndex = 0;
        for (int y = startY; y < endY; y += stepY) {
            int offset = (rowIndex % 2 == 0) ? 0 : stepX / 2;
            for (int x = startX - offset; x < endX; x += stepX) {
                // Draw each line
                for (int i = 0; i < lines.length; i++) {
                    g2d.drawString(lines[i], x, (int) (y + (i * lineHeight)));
                }
            }
            rowIndex++;
        }
    }

    private void paintImageWatermark(Graphics2D g2d, int width, int height, int maxDim) {
        java.awt.image.BufferedImage img = config.getImage();
        if (img == null)
            return;

        int imgW = config.getImageWidth();
        int imgH = config.getImageHeight();

        int startX = -maxDim / 2;
        int endX = width + maxDim / 2;
        int startY = -maxDim / 2;
        int endY = height + maxDim / 2;

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
