# Swing Watermark 🛡️

[![JitPack](https://jitpack.io/v/phonchaidev/swing-watermark.svg)](https://jitpack.io/#phonchaidev/swing-watermark)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A premium, high-performance tiled watermark library for Java Swing applications. Designed to prevent unauthorized screen captures while remaining non-intrusive for the user.

## Features

- 🚀 **High Performance:** Uses `JLayer` for efficient overlay rendering.
- 🎨 **Fully Customizable:** Text, font, color, opacity, rotation angle, spacing, and more.
- 🖼️ **Image Watermark:** Use logo or image instead of text.
- 📝 **Multi-line Text:** Support for multi-line watermark text.
- 🔄 **Dynamic Placeholders:** `{username}`, `{datetime}`, `{date}`, and custom placeholders.
- 🌐 **Global Mode:** One-line setup for all windows (JFrame, JDialog).
- ⚡ **Runtime Toggle:** Enable/disable watermark at runtime.
- 🖱️ **Non-Intrusive:** Click-through overlay; no event interception.
- 📐 **Auto-Scaling:** Adjusts to window resizing automatically.

## Installation

### Maven
Add the JitPack repository and the dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>io.github.phonchaidev</groupId>
    <artifactId>swing-watermark</artifactId>
    <version>1.1.0</version>
</dependency>
```

---

## Quick Start

### 1. Basic Usage (Single Window)

```java
WatermarkConfig config = new WatermarkConfig()
        .text("CONFIDENTIAL")
        .opacity(0.15f)
        .angleDegrees(-30);

JLayer<JComponent> layer = new JLayer<>(mainPanel, new WatermarkLayerUI(config));
frame.add(layer);
```

### 2. Global Mode — All Windows (Recommended)

```java
WatermarkConfig config = new WatermarkConfig()
        .text("{username} - {datetime}")
        .placeholder("username", "Admin")
        .fontName("Tahoma")
        .fontSize(16)
        .opacity(0.15f)
        .angleDegrees(-30);

// One line → ALL JFrames and JDialogs get the watermark automatically!
WatermarkInstaller.installGlobal(config);
```

### 3. Font Shortcuts

```java
// Easy way (shortcuts)
new WatermarkConfig()
        .fontName("TH SarabunPSK")
        .fontSize(20)
        .fontStyle(Font.BOLD);

// Traditional way (still supported)
new WatermarkConfig()
        .font(new Font("TH SarabunPSK", Font.BOLD, 20));
```

### 4. Dynamic Placeholders

```java
new WatermarkConfig()
        .text("{username} ({role}) - {datetime}")
        .placeholder("username", currentUser.getName())
        .placeholder("role", currentUser.getRole());
// Output: "Admin (Manager) - 2026-02-27 14:00"
```

Built-in placeholders:
- `{datetime}` → `2026-02-27 14:00`
- `{date}` → `2026-02-27`

### 5. Multi-line Text

```java
new WatermarkConfig()
        .text("Line 1\nLine 2\nLine 3");
```

### 6. Image Watermark

```java
BufferedImage logo = ImageIO.read(new File("logo.png"));
new WatermarkConfig()
        .image(logo)
        .imageSize(64, 64)
        .opacity(0.1f);
```

### 7. Runtime Toggle

```java
WatermarkConfig config = new WatermarkConfig().text("SECRET");

// Disable watermark
config.enabled(false);
panel.repaint();

// Enable watermark
config.enabled(true);
panel.repaint();
```

---

## API Reference

| Method | Description | Default |
|---|---|---|
| `.text(String)` | Watermark text | `"CONFIDENTIAL"` |
| `.placeholder(key, value)` | Dynamic text replacement | — |
| `.fontName(String)` | Font family name | `"SansSerif"` |
| `.fontSize(int)` | Font size | `24` |
| `.fontStyle(int)` | `Font.PLAIN`, `BOLD`, `ITALIC` | `Font.BOLD` |
| `.font(Font)` | Full Font override | — |
| `.color(Color)` | Text/image tint color | `Color.GRAY` |
| `.opacity(float)` | Transparency (0.0–1.0) | `0.15f` |
| `.angleDegrees(double)` | Rotation angle | `-30` |
| `.spacing(int, int)` | Horizontal, Vertical gap | `150, 150` |
| `.image(BufferedImage)` | Image watermark | `null` |
| `.imageSize(int, int)` | Image display size | `64×64` |
| `.enabled(boolean)` | Runtime toggle | `true` |

## License
Distributed under the MIT License. See `LICENSE` for more information.
