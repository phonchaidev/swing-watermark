package io.github.phonchaidev.watermark;

import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Demo: Enterprise Watermark with ALL features.
 * - Text mode (multi-line + placeholders)
 * - Image/Logo mode (with visible sample logo)
 * - Toggle on/off at runtime
 * - Global mode (auto-apply to Dialogs)
 */
public class MainApplication {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Use FlatLaf as required by modal-dialog
            try {
                com.formdev.flatlaf.FlatLightLaf.setup();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // --- TEXT CONFIG --- //
            WatermarkConfig textConfig = new WatermarkConfig()
                    .text("{username}\n{datetime}")
                    .placeholder("username", "ณัฏฐวุฒิ พรหมบุตร (ผู้ดูแลระบบ SFIS)")
                    .fontName("Tahoma")
                    .fontSize(14)
                    .fontStyle(Font.PLAIN)
                    .color(Color.GRAY)
                    .opacity(0.15f)
                    .angleDegrees(-30)
                    .spacing(80, 120);

            // --- LOGO CONFIG --- //
            BufferedImage logo = createSampleLogo(80);
            WatermarkConfig logoConfig = new WatermarkConfig()
                    .image(logo)
                    .imageSize(80, 80)
                    .opacity(0.20f) // Higher opacity so logo is clearly visible
                    .angleDegrees(-25)
                    .spacing(100, 100);

            // Track which mode is active
            final WatermarkConfig[] activeConfig = { textConfig };

            // --- BUILD THE FRAME --- //
            JFrame frame = new JFrame("Watermark Enterprise Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(850, 550);
            frame.setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(Color.WHITE);

            // Header
            JPanel header = new JPanel();
            header.setBackground(new Color(25, 60, 110));
            header.setPreferredSize(new Dimension(850, 60));
            JLabel titleLabel = new JLabel("CRIMES ONLINE — Enterprise Watermark Demo", SwingConstants.CENTER);
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            header.add(titleLabel);
            mainPanel.add(header, BorderLayout.NORTH);

            // Buttons
            JPanel content = new JPanel(new GridLayout(2, 2, 15, 15));
            content.setBackground(Color.WHITE);
            content.setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30));

            // Button 1: Open Dialog
            JButton dialogBtn = new JButton(
                    "<html><center>📋 Open Dialog<br><small>Auto-watermark</small></center></html>");
            dialogBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
            dialogBtn.addActionListener(e -> {
                JDialog dialog = new JDialog(frame, "Sub Window", true);
                dialog.setSize(400, 300);
                dialog.setLocationRelativeTo(frame);
                JPanel dp = new JPanel(new BorderLayout());
                dp.setBackground(Color.WHITE);
                dp.add(new JLabel("<html><center><b>Dialog Window</b><br>Watermark auto-applied!</center></html>",
                        SwingConstants.CENTER), BorderLayout.CENTER);
                dialog.setContentPane(dp);
                dialog.setVisible(true);
            });

            // Button 2: Switch between Text ↔ Logo
            JButton logoBtn = new JButton(
                    "<html><center>🖼️ Switch Mode<br><small>Text ↔ Logo</small></center></html>");
            logoBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
            logoBtn.addActionListener(e -> {
                if (activeConfig[0] == textConfig) {
                    activeConfig[0] = logoConfig;
                    logoBtn.setText("<html><center>🖼️ Switch Mode<br><small>Current: LOGO</small></center></html>");
                } else {
                    activeConfig[0] = textConfig;
                    logoBtn.setText("<html><center>🖼️ Switch Mode<br><small>Current: TEXT</small></center></html>");
                }
                // Re-install globally with new config
                WatermarkInstaller.installGlobal(activeConfig[0]);
            });

            // Button 3: Toggle on/off
            JButton toggleBtn = new JButton("<html><center>⚡ Toggle<br><small>ON / OFF</small></center></html>");
            toggleBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
            toggleBtn.addActionListener(e -> {
                activeConfig[0].enabled(!activeConfig[0].isEnabled());
                frame.repaint();
                toggleBtn.setText(activeConfig[0].isEnabled()
                        ? "<html><center>⚡ Toggle<br><small>Currently: ON</small></center></html>"
                        : "<html><center>⚡ Toggle<br><small>Currently: OFF</small></center></html>");
            });

            content.add(dialogBtn);
            content.add(logoBtn);
            content.add(toggleBtn);

            // Button 4: Open dj-raven ModalDialog (test watermark in modal)
            JButton modalBtn = new JButton(
                    "<html><center>🪟 ModalDialog<br><small>dj-raven test</small></center></html>");
            modalBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
            modalBtn.setBackground(new Color(60, 120, 80));
            modalBtn.setForeground(Color.WHITE);
            modalBtn.setOpaque(true);
            modalBtn.addActionListener(e -> {
                // Content panel inside the modal
                JPanel modalContent = new JPanel(new BorderLayout(10, 10));
                modalContent.setBackground(Color.WHITE);
                modalContent.setPreferredSize(new Dimension(420, 200));

                JLabel info = new JLabel(
                        "<html><center><b>ModalDialog (dj-raven)</b><br><br>"
                                + "ถ้าลายน้ำปรากฏที่นี่ = ✅ WatermarkInstaller ทำงานได้<br>"
                                + "ถ้าไม่ปรากฏ = ❌ modal layer บัง JLayer watermark"
                                + "</center></html>",
                        SwingConstants.CENTER);
                modalContent.add(info, BorderLayout.CENTER);

                JLabel hint = new JLabel(
                        "<html><center><small>modal-dialog v2.6.1 — SimpleModalBorder</small></center></html>",
                        SwingConstants.CENTER);
                hint.setForeground(Color.GRAY);
                modalContent.add(hint, BorderLayout.SOUTH);

                ModalDialog.showModal(frame,
                        new SimpleModalBorder(
                                modalContent,
                                "ทดสอบ Watermark ใน Modal (dj-raven)",
                                SimpleModalBorder.OK_OPTION,
                                // ตรวจ action ก่อน: library จะเรียก callback ด้วย OPENED ตอนเปิด
                                // ถ้าไม่ check จะปิด modal ทันที
                                (controller, action) -> {
                                    if (action == SimpleModalBorder.OK_OPTION) {
                                        controller.close();
                                    }
                                }),
                        "watermark-modal-test");
            });
            content.add(modalBtn);
            mainPanel.add(content, BorderLayout.CENTER);

            frame.setContentPane(mainPanel);
            frame.setVisible(true);

            // Install global watermark AFTER frame is shown
            WatermarkInstaller.installGlobal(activeConfig[0]);
        });
    }

    /**
     * Create a visible sample logo (police-style shield) for demo purposes.
     * Supports: PNG, JPG, GIF, BMP via ImageIO.read() in real usage.
     */
    private static BufferedImage createSampleLogo(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = size / 2;

        // Shield body
        int[] xShield = { cx, size / 8, size / 8, size / 4, cx, size * 3 / 4, size * 7 / 8, size * 7 / 8 };
        int[] yShield = { size / 10, size / 5, size / 2, size * 4 / 5, size * 9 / 10, size * 4 / 5, size / 2,
                size / 5 };
        g.setColor(new Color(50, 80, 130));
        g.fillPolygon(xShield, yShield, 8);

        // Shield border
        g.setStroke(new BasicStroke(2.5f));
        g.setColor(new Color(30, 50, 90));
        g.drawPolygon(xShield, yShield, 8);

        // Star in center
        g.setColor(new Color(200, 200, 220));
        int starSize = size / 4;
        int starCx = cx;
        int starCy = size / 2;
        int[] xStar = new int[10];
        int[] yStar = new int[10];
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 2 + i * Math.PI / 5;
            int r = (i % 2 == 0) ? starSize : starSize / 2;
            xStar[i] = starCx + (int) (r * Math.cos(angle));
            yStar[i] = starCy - (int) (r * Math.sin(angle));
        }
        g.fillPolygon(xStar, yStar, 10);

        g.dispose();
        return img;
    }
}
