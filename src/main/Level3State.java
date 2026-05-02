package main;

import entity.Enemy;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Level3State extends GameState {
    private BufferedImage mapImage;
    private static final String MAP_PATH = "/maps/giang-duong.png";

    public Level3State(GamePanel gp) {
        super(gp);
    }

    @Override
    public void enter() {
        try {
            InputStream is = getClass().getResourceAsStream(MAP_PATH);
            if (is != null) {
                BufferedImage src = ImageIO.read(is);
                mapImage = new BufferedImage(gp.worldWidth, gp.worldHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D mg = mapImage.createGraphics();
                mg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                mg.drawImage(src, 0, 0, gp.worldWidth, gp.worldHeight, null);
                mg.dispose();
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
            // Fallback: nền giảng đường đơn giản
            g2.setColor(new Color(55, 48, 38));
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        }

        // Overlay nhẹ (bầu không khí lớp học buổi sáng)
        g2.setColor(new Color(20, 15, 5, 40));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        gp.player.draw(g2);
        for (Enemy enemy : gp.enemies) {
            enemy.draw(g2);
        }

        // HUD chặng 3
        g2.setColor(new Color(255, 220, 80));
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("CHẶNG 3 - Giảng đường: " + gp.enemies.size() + " kẻ phá rối", 10, 30);

        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.setColor(new Color(255, 220, 80, 210));
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
