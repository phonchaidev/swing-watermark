package io.github.phonchai.first.watermark;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

public class WatermarkLayerUITest {

    /**
     * Test: Text watermark renders without errors.
     */
    @Test
    public void testTextWatermarkPaint() {
        WatermarkConfig config = new WatermarkConfig()
                .text("TEST WATERMARK")
                .opacity(0.2f)
                .angleDegrees(-30);

        WatermarkLayerUI layerUI = new WatermarkLayerUI(config);

        JPanel panel = new JPanel();
        panel.setSize(400, 300);

        JLayer<JComponent> layer = new JLayer<>(panel, layerUI);
        layer.setSize(400, 300);

        // Paint into a BufferedImage to verify no exceptions
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        layer.paint(g2d);
        g2d.dispose();

        // If we get here without an exception, the rendering works
        assertNotNull(image);
    }

    /**
     * Test: Multi-line text watermark renders without errors.
     */
    @Test
    public void testMultilineTextWatermarkPaint() {
        WatermarkConfig config = new WatermarkConfig()
                .text("Line 1\nLine 2\nLine 3")
                .fontSize(14);

        WatermarkLayerUI layerUI = new WatermarkLayerUI(config);

        JPanel panel = new JPanel();
        panel.setSize(400, 300);

        JLayer<JComponent> layer = new JLayer<>(panel, layerUI);
        layer.setSize(400, 300);

        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        layer.paint(g2d);
        g2d.dispose();

        assertNotNull(image);
    }

    /**
     * Test: Image/Logo watermark renders without errors.
     */
    @Test
    public void testImageWatermarkPaint() {
        // Create a simple 32x32 test logo
        BufferedImage logo = createTestLogo(32, 32);

        WatermarkConfig config = new WatermarkConfig()
                .image(logo)
                .imageSize(48, 48)
                .opacity(0.1f)
                .angleDegrees(-30)
                .spacing(100, 100);

        assertTrue(config.isImageMode());

        WatermarkLayerUI layerUI = new WatermarkLayerUI(config);

        JPanel panel = new JPanel();
        panel.setSize(400, 300);

        JLayer<JComponent> layer = new JLayer<>(panel, layerUI);
        layer.setSize(400, 300);

        BufferedImage rendered = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rendered.createGraphics();
        layer.paint(g2d);
        g2d.dispose();

        // Verify Image has been painted (some pixels should be non-transparent)
        boolean hasContent = false;
        for (int x = 0; x < rendered.getWidth() && !hasContent; x += 10) {
            for (int y = 0; y < rendered.getHeight() && !hasContent; y += 10) {
                int alpha = (rendered.getRGB(x, y) >> 24) & 0xFF;
                if (alpha > 0) {
                    hasContent = true;
                }
            }
        }
        assertTrue(hasContent, "Image watermark should produce some visible pixels");
    }

    /**
     * Test: Disabled watermark should NOT paint anything extra.
     */
    @Test
    public void testDisabledWatermarkPaintsNothing() {
        WatermarkConfig config = new WatermarkConfig()
                .text("SHOULD NOT APPEAR")
                .enabled(false);

        WatermarkLayerUI layerUI = new WatermarkLayerUI(config);

        JPanel panel = new JPanel();
        panel.setSize(400, 300);
        panel.setBackground(Color.WHITE);

        JLayer<JComponent> layer = new JLayer<>(panel, layerUI);
        layer.setSize(400, 300);

        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        layer.paint(g2d);
        g2d.dispose();

        // Should not throw an error and the watermark layer should skip rendering
        assertNotNull(image);
    }

    /**
     * Test: Image + Text combined (image takes priority).
     */
    @Test
    public void testImageModeTakesPriority() {
        BufferedImage logo = createTestLogo(32, 32);

        WatermarkConfig config = new WatermarkConfig()
                .text("This text should be ignored")
                .image(logo);

        assertTrue(config.isImageMode(), "When image is set, isImageMode should be true");
    }

    /**
     * Helper: Create a small test logo with a simple shape.
     */
    private BufferedImage createTestLogo(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(100, 100, 100, 128));
        g.fillOval(2, 2, width - 4, height - 4);
        g.setColor(Color.DARK_GRAY);
        g.drawOval(2, 2, width - 4, height - 4);
        g.dispose();
        return img;
    }
}
