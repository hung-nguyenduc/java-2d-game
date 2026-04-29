package main;

import entity.Enemy;
import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;

public class Level2State extends GameState {
    private Image mapImage;
    private static final String MAP_PATH = "/maps/test.png";

    public Level2State(GamePanel gp) {
        super(gp);
    }

    @Override
    public void enter() {
        mapImage = new ImageIcon(getClass().getResource(MAP_PATH)).getImage();
        gp.enemies.clear();
        gp.killCount = 0;
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
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        int cameraX = gp.player.worldX - (gp.screenWidth / 2);
        int cameraY = gp.player.worldY - (gp.screenHeight / 2);
        int[] clamped = gp.clampCameraPosition(cameraX, cameraY);
        cameraX = clamped[0];
        cameraY = clamped[1];

        // Phủ màu tối lên map để tạo cảm giác khác biệt với màn 1
        g2.drawImage(mapImage, -cameraX, -cameraY, gp.worldWidth, gp.worldHeight, null);
        g2.setColor(new Color(0, 0, 60, 80));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        gp.player.draw(g2);
        for (Enemy enemy : gp.enemies) {
            enemy.draw(g2);
        }

        // HUD màn 2
        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("MAN 2 - Quan quai: " + gp.enemies.size(), 10, 30);

        // Tiêu đề màn 2 ở góc trên giữa
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        String sub = "Tieu diet tat ca de chien thang!";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(sub, (gp.screenWidth - fm.stringWidth(sub)) / 2, 30);
    }

    private void spawnEnemies() {
        // 5 quái, vị trí khác hoàn toàn so với màn 1
        gp.enemies.add(new Enemy(gp, gp.player, 200,  800, 0));
        gp.enemies.add(new Enemy(gp, gp.player, 600,  200, 1));
        gp.enemies.add(new Enemy(gp, gp.player, 1400, 400, 2));
        gp.enemies.add(new Enemy(gp, gp.player, 900, 1500, 1));
        gp.enemies.add(new Enemy(gp, gp.player, 1700, 1200, 0));
    }

    @Override
    public void handleMouseClick(MouseEvent e) {}
}