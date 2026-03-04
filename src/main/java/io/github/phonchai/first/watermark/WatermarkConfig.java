package io.github.phonchai.first.watermark;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for the watermark. Supports fluent API (Builder Pattern).
 *
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * WatermarkConfig config = new WatermarkConfig()
 *         .text("{username} - {datetime}")
 *         .placeholder("username", "Admin")
 *         .fontName("Tahoma")
 *         .fontSize(16)
 *         .fontStyle(Font.BOLD)
 *         .color(Color.GRAY)
 *         .opacity(0.15f)
 *         .angleDegrees(-30)
 *         .spacing(80, 120);
 * }</pre>
 */
public class WatermarkConfig {

    // --- Text ---
    private String text = "CONFIDENTIAL";
    private final Map<String, String> placeholders = new HashMap<>();

    // --- Font ---
    private String fontName = "SansSerif";
    private int fontStyle = Font.BOLD;
    private int fontSize = 24;
    private Font fontOverride = null; // If user calls .font() directly

    // --- Appearance ---
    private Color color = Color.GRAY;
    private float opacity = 0.15f;
    private double angle = Math.toRadians(-30);
    private int horizontalSpacing = 150;
    private int verticalSpacing = 150;

    // --- Image Watermark ---
    private BufferedImage image = null;
    private int imageWidth = 64;
    private int imageHeight = 64;

    // --- State ---
    private boolean enabled = true;

    public WatermarkConfig() {
    }

    // ========== TEXT ==========

    /**
     * Set the watermark text. Supports placeholders like {username}, {datetime}.
     */
    public WatermarkConfig text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Register a placeholder key-value pair for dynamic text replacement.
     * <p>
     * Example:
     * {@code .text("{username} - {role}").placeholder("username", "Admin").placeholder("role", "Manager")}
     * </p>
     */
    public WatermarkConfig placeholder(String key, String value) {
        this.placeholders.put(key, value);
        return this;
    }

    // ========== FONT SHORTCUTS ==========

    /**
     * Set the font name (e.g., "Tahoma", "TH SarabunPSK", "SansSerif").
     */
    public WatermarkConfig fontName(String fontName) {
        this.fontName = fontName;
        this.fontOverride = null;
        return this;
    }

    /**
     * Set the font size.
     */
    public WatermarkConfig fontSize(int fontSize) {
        this.fontSize = fontSize;
        this.fontOverride = null;
        return this;
    }

    /**
     * Set the font style (Font.PLAIN, Font.BOLD, Font.ITALIC, or Font.BOLD |
     * Font.ITALIC).
     */
    public WatermarkConfig fontStyle(int fontStyle) {
        this.fontStyle = fontStyle;
        this.fontOverride = null;
        return this;
    }

    /**
     * Set the full Font object directly. This overrides fontName, fontSize, and
     * fontStyle.
     */
    public WatermarkConfig font(Font font) {
        this.fontOverride = font;
        return this;
    }

    // ========== APPEARANCE ==========

    /**
     * Set the watermark color.
     */
    public WatermarkConfig color(Color color) {
        this.color = color;
        return this;
    }

    /**
     * Set the opacity (0.0 = fully transparent, 1.0 = fully opaque).
     */
    public WatermarkConfig opacity(float opacity) {
        this.opacity = Math.max(0.0f, Math.min(1.0f, opacity));
        return this;
    }

    /**
     * Set the rotation angle in degrees (e.g., -30, -45).
     */
    public WatermarkConfig angleDegrees(double angleDegrees) {
        this.angle = Math.toRadians(angleDegrees);
        return this;
    }

    /**
     * Set spacing between watermark tiles.
     */
    public WatermarkConfig spacing(int horizontalSpacing, int verticalSpacing) {
        this.horizontalSpacing = horizontalSpacing;
        this.verticalSpacing = verticalSpacing;
        return this;
    }

    // ========== IMAGE WATERMARK ==========

    /**
     * Set an image to use as the watermark instead of text.
     */
    public WatermarkConfig image(BufferedImage image) {
        this.image = image;
        return this;
    }

    /**
     * Set the display size of the image watermark.
     */
    public WatermarkConfig imageSize(int width, int height) {
        this.imageWidth = width;
        this.imageHeight = height;
        return this;
    }

    // ========== STATE ==========

    /**
     * Enable or disable the watermark at runtime.
     */
    public WatermarkConfig enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    // ========== GETTERS ==========

    /**
     * Get the resolved text with placeholders replaced and {datetime} auto-filled.
     */
    public String getResolvedText() {
        if (text == null)
            return "";

        String resolved = text;

        // Replace custom placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            resolved = resolved.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        // Built-in: {datetime} → current date/time
        if (resolved.contains("{datetime}")) {
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            resolved = resolved.replace("{datetime}", now);
        }

        // Built-in: {date} → current date only
        if (resolved.contains("{date}")) {
            String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            resolved = resolved.replace("{date}", today);
        }

        return resolved;
    }

    /**
     * Check if the text contains multiple lines (separated by \n).
     */
    public String[] getTextLines() {
        String resolved = getResolvedText();
        return resolved.split("\n");
    }

    public String getText() {
        return text;
    }

    public Font getFont() {
        if (fontOverride != null)
            return fontOverride;
        return new Font(fontName, fontStyle, fontSize);
    }

    public Color getColor() {
        return color;
    }

    public float getOpacity() {
        return opacity;
    }

    public double getAngle() {
        return angle;
    }

    public int getHorizontalSpacing() {
        return horizontalSpacing;
    }

    public int getVerticalSpacing() {
        return verticalSpacing;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isImageMode() {
        return image != null;
    }
}
