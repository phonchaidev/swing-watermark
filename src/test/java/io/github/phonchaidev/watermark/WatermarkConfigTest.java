package io.github.phonchaidev.watermark;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

public class WatermarkConfigTest {

    @Test
    public void testDefaultValues() {
        WatermarkConfig config = new WatermarkConfig();
        assertNotNull(config.getText());
        assertTrue(config.getOpacity() > 0 && config.getOpacity() <= 1.0);
        assertNotNull(config.getFont());
        assertTrue(config.isEnabled());
        assertFalse(config.isImageMode());
    }

    @Test
    public void testFluidAPI() {
        WatermarkConfig config = new WatermarkConfig()
                .text("TEST")
                .opacity(0.5f)
                .color(Color.RED)
                .angleDegrees(45);

        assertEquals("TEST", config.getText());
        assertEquals(0.5f, config.getOpacity());
        assertEquals(Color.RED, config.getColor());
        assertEquals(Math.toRadians(45), config.getAngle(), 0.001);
    }

    @Test
    public void testFontShortcuts() {
        WatermarkConfig config = new WatermarkConfig()
                .fontName("Tahoma")
                .fontSize(20)
                .fontStyle(Font.BOLD | Font.ITALIC);

        Font font = config.getFont();
        assertEquals("Tahoma", font.getName());
        assertEquals(20, font.getSize());
        assertEquals(Font.BOLD | Font.ITALIC, font.getStyle());
    }

    @Test
    public void testFontOverride() {
        Font customFont = new Font("Arial", Font.PLAIN, 30);
        WatermarkConfig config = new WatermarkConfig()
                .fontName("Tahoma")
                .fontSize(16)
                .font(customFont); // Override

        assertSame(customFont, config.getFont());
    }

    @Test
    public void testFontShortcutClearsOverride() {
        Font customFont = new Font("Arial", Font.PLAIN, 30);
        WatermarkConfig config = new WatermarkConfig()
                .font(customFont)
                .fontSize(20); // Should clear override

        assertNotSame(customFont, config.getFont());
        assertEquals(20, config.getFont().getSize());
    }

    @Test
    public void testPlaceholders() {
        WatermarkConfig config = new WatermarkConfig()
                .text("{username} - {role}")
                .placeholder("username", "Admin")
                .placeholder("role", "Manager");

        assertEquals("Admin - Manager", config.getResolvedText());
    }

    @Test
    public void testDatetimePlaceholder() {
        WatermarkConfig config = new WatermarkConfig()
                .text("Login: {datetime}");

        String resolved = config.getResolvedText();
        assertFalse(resolved.contains("{datetime}"));
        assertTrue(resolved.startsWith("Login: "));
    }

    @Test
    public void testDatePlaceholder() {
        WatermarkConfig config = new WatermarkConfig()
                .text("Date: {date}");

        String resolved = config.getResolvedText();
        assertFalse(resolved.contains("{date}"));
        assertTrue(resolved.startsWith("Date: "));
    }

    @Test
    public void testMultilineText() {
        WatermarkConfig config = new WatermarkConfig()
                .text("Line 1\nLine 2\nLine 3");

        String[] lines = config.getTextLines();
        assertEquals(3, lines.length);
        assertEquals("Line 1", lines[0]);
        assertEquals("Line 2", lines[1]);
        assertEquals("Line 3", lines[2]);
    }

    @Test
    public void testImageMode() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        WatermarkConfig config = new WatermarkConfig()
                .image(img)
                .imageSize(128, 128);

        assertTrue(config.isImageMode());
        assertEquals(128, config.getImageWidth());
        assertEquals(128, config.getImageHeight());
    }

    @Test
    public void testEnableDisable() {
        WatermarkConfig config = new WatermarkConfig();
        assertTrue(config.isEnabled());

        config.enabled(false);
        assertFalse(config.isEnabled());

        config.enabled(true);
        assertTrue(config.isEnabled());
    }

    @Test
    public void testOpacityClamping() {
        WatermarkConfig config = new WatermarkConfig();

        config.opacity(2.0f);
        assertEquals(1.0f, config.getOpacity());

        config.opacity(-0.5f);
        assertEquals(0.0f, config.getOpacity());
    }
}
