package main;

import entity.Enemy;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ZombieState extends GameState {
    private Image mapImage; // GPU-friendly compatible image
    private static final String MAP_PATH = "/maps/c1.png";
    private static final Font HUD_FONT = new Font("Arial", Font.BOLD, 20);

    public ZombieState(GamePanel gp) {
        super(gp);
    }

    @Override
    public void enter() {
        // Pre-scale map một lần duy nhất → mỗi frame chỉ blit vùng nhìn thấy (GPU accelerated)
        try {
            BufferedImage src = ImageIO.read(getClass().getResourceAsStream(MAP_PATH));
            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            BufferedImage compat = gc.createCompatibleImage(gp.worldWidth, gp.worldHeight, Transparency.OPAQUE);
            Graphics2D mg = compat.createGraphics();
            mg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            mg.drawImage(src, 0, 0, gp.worldWidth, gp.worldHeight, null);
            mg.dispose();
            mapImage = compat;
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

        if (gp.killCount >= 3) {
            gp.setState(new LevelCompleteState(gp, 1, new Level2State(gp)));
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        int cameraX = gp.player.worldX - (gp.screenWidth / 2);
        int cameraY = gp.player.worldY - (gp.screenHeight / 2);
        int[] clamped = gp.clampCameraPosition(cameraX, cameraY);
        cameraX = clamped[0];
        cameraY = clamped[1];

        // Chỉ copy vùng camera nhìn thấy (~768×576) thay vì toàn bộ 2000×2000
        g2.drawImage(mapImage,
            0, 0, gp.screenWidth, gp.screenHeight,
            cameraX, cameraY, cameraX + gp.screenWidth, cameraY + gp.screenHeight,
            null);

        gp.player.draw(g2);
        for (Enemy enemy : gp.enemies) {
            enemy.draw(g2);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(HUD_FONT);
        g2.drawString("Man 1 - Giet quai: " + gp.killCount + " / 3", 10, 30);
    }

    private void spawnEnemies() {
        gp.enemies.add(new Enemy(gp, gp.player, 300, 300, 0));
        gp.enemies.add(new Enemy(gp, gp.player, 800, 500, 1));
        gp.enemies.add(new Enemy(gp, gp.player, 1200, 700, 2));
    }

    @Override
    public void handleMouseClick(MouseEvent e) {}
}
