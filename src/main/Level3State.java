package main;

import entity.Enemy;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Level3State extends GameState {
    private Image mapImage;
    private static final String MAP_PATH = "/maps/giang-duong.png";
    private static final Font HUD_FONT_BIG = new Font("Arial", Font.BOLD, 20);
    private static final Font HUD_FONT_SMALL = new Font("Arial", Font.BOLD, 15);
    private static final Color HUD_COLOR = new Color(255, 220, 80);
    private static final Color HUD_COLOR_FADED = new Color(255, 220, 80, 210);
    private static final Color FALLBACK_BG = new Color(55, 48, 38);

    public Level3State(GamePanel gp) {
        super(gp);
    }

    @Override
    public void enter() {
        try {
            InputStream is = getClass().getResourceAsStream(MAP_PATH);
            if (is != null) {
                BufferedImage src = ImageIO.read(is);
                int srcW = src.getWidth();
                int srcH = src.getHeight();

                // Giữ nguyên tỉ lệ ảnh gốc (contain): scale theo chiều bé hơn để toàn bộ ảnh đều hiện
                double scale = Math.min(
                        (double) gp.worldWidth / srcW,
                        (double) gp.worldHeight / srcH);
                int scaledW = (int) Math.round(srcW * scale);
                int scaledH = (int) Math.round(srcH * scale);
                int offsetX = (gp.worldWidth - scaledW) / 2;
                int offsetY = (gp.worldHeight - scaledH) / 2;

                GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice().getDefaultConfiguration();
                BufferedImage compat = gc.createCompatibleImage(gp.worldWidth, gp.worldHeight, Transparency.OPAQUE);
                Graphics2D mg = compat.createGraphics();
                // Nền viền quanh ảnh khi tỉ lệ không khớp world (màu tường giảng đường)
                mg.setColor(new Color(232, 226, 210));
                mg.fillRect(0, 0, gp.worldWidth, gp.worldHeight);
                mg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                mg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                mg.drawImage(src, offsetX, offsetY, scaledW, scaledH, null);
                mg.dispose();
                mapImage = compat;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        gp.enemies.clear();
        gp.killCount = 0;
        gp.player.health = gp.player.maxHealth;
        gp.player.bullets.clear();
        spawnEnemies();
    }

    @Override
    public void exit() {
        gp.enemies.clear();
    }

    @Override
    public void update() {
        gp.player.update();
        for (int i = 0; i < gp.enemies.size(); i++) {
            gp.enemies.get(i).update();
        }
        gp.checkCollisions();

        if (gp.player.health <= 0) {
            gp.setState(new GameOverState(gp));
            return;
        }

        if (gp.enemies.isEmpty()) {
            gp.setState(new LevelCompleteState(gp, 3, new MenuState(gp)));
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        if (mapImage != null) {
            int cameraX = gp.player.worldX - (gp.screenWidth / 2);
            int cameraY = gp.player.worldY - (gp.screenHeight / 2);
            int[] clamped = gp.clampCameraPosition(cameraX, cameraY);
            cameraX = clamped[0];
            cameraY = clamped[1];

            g2.drawImage(mapImage,
                0, 0, gp.screenWidth, gp.screenHeight,
                cameraX, cameraY, cameraX + gp.screenWidth, cameraY + gp.screenHeight,
                null);
        } else {
            g2.setColor(FALLBACK_BG);
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        }

        gp.player.draw(g2);
        for (Enemy enemy : gp.enemies) {
            enemy.draw(g2);
        }

        // HUD chặng 3
        g2.setColor(HUD_COLOR);
        g2.setFont(HUD_FONT_BIG);
        g2.drawString("CHẶNG 3 - Giảng đường: " + gp.enemies.size() + " kẻ phá rối", 10, 30);

        g2.setFont(HUD_FONT_SMALL);
        g2.setColor(HUD_COLOR_FADED);
        String sub = "Tiêu diệt tất cả để tập trung học!";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(sub, (gp.screenWidth - fm.stringWidth(sub)) / 2, 30);
    }

    private void spawnEnemies() {
        gp.enemies.add(new Enemy(gp, gp.player, 300,  400, 0));
        gp.enemies.add(new Enemy(gp, gp.player, 700,  300, 1));
        gp.enemies.add(new Enemy(gp, gp.player, 1100, 600, 2));
        gp.enemies.add(new Enemy(gp, gp.player, 500,  1200, 1));
        gp.enemies.add(new Enemy(gp, gp.player, 1500, 900, 0));
        gp.enemies.add(new Enemy(gp, gp.player, 1800, 400, 2));
    }

    @Override
    public void handleMouseClick(MouseEvent e) {}
}
