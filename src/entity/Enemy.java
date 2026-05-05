package entity;

import main.GamePanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

// Lớp đại diện cho enemy trong game
public class Enemy extends Entity {
    GamePanel gp;
    Player player;
    public BufferedImage enemyImage;
    private int shootCooldown = 0;
    private final int shootInterval = 60; // Shoot every 60 frames (1 second at 60 FPS)
    private final double minDistance = 50; // Minimum distance from player
    private int enemyType; // 0, 1, 2 for 3 different enemy types

    // Constructor: Khởi tạo Enemy với vị trí ban đầu
    public Enemy(GamePanel gp, Player player, int startX, int startY, int enemyType) {
        this.gp = gp;
        this.player = player;
        this.enemyType = enemyType;
        worldX = startX;
        worldY = startY;
        speed = 3; // Slower than player
        aimAngle = 0;
        health = maxHealth; // Đặt máu ban đầu

        getEnemyImage();
    }

    // Tải hình ảnh của Enemy dựa trên loại enemy
    public void getEnemyImage() {
        try {
            // Load 3 different enemy images from enemy folder
            switch(enemyType) {
                case 0:
                    enemyImage = ImageIO.read(getClass().getResourceAsStream("/enemy/enemy1.png"));
                    break;
                case 1:
                    enemyImage = ImageIO.read(getClass().getResourceAsStream("/enemy/enemy2.png"));
                    break;
                case 2:
                    enemyImage = ImageIO.read(getClass().getResourceAsStream("/enemy/enemy3.png"));
                    break;
                default:
                    enemyImage = ImageIO.read(getClass().getResourceAsStream("/enemy/enemy1.png"));
            }
        } catch (IOException e) {
            System.out.println("LỖI: Không tìm thấy ảnh quái vật!");
            e.printStackTrace();
        }
    }

    // Cập nhật trạng thái của Enemy mỗi frame
    public void update() {
        // Calculate direction towards player
        double dx = player.worldX - worldX;
        double dy = player.worldY - worldY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > minDistance) { // Only move if not too close
            // Normalize direction
            vx = (dx / distance) * speed;
            vy = (dy / distance) * speed;

            // Calculate aim angle towards player
            aimAngle = Math.toDegrees(Math.atan2(dy, dx));

            // Apply movement (worldX/worldY là double → tránh mất precision khi normalize)
            worldX += vx;
            worldY += vy;
        }

        // Giới hạn enemy trong phạm vi map
        worldX = Math.max(0, Math.min(worldX, gp.worldWidth - 80));
        worldY = Math.max(0, Math.min(worldY, gp.worldHeight - 80));

        // Update bullets
        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).update();
            if (bullets.get(i).isOutOfRange()) {
                bullets.remove(i);
                i--;
            }
        }

        // Periodic shooting
        shootCooldown++;
        if (shootCooldown >= shootInterval) {
            shoot();
            shootCooldown = 0;
        }
    }

    // Bắn đạn về phía Player
    public void shoot() {
        // Create bullet towards player
        Bullet bullet = new Bullet(worldX + 40, worldY + 40, aimAngle);
        bullets.add(bullet);
    }

    // Vẽ Enemy theo camera đã clamp (tránh enemy "trượt" khi player tới rìa map)
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int) (worldX - cameraX);
        int screenY = (int) (worldY - cameraY);

        if (screenX > -80 && screenX < gp.screenWidth + 80 && screenY > -80 && screenY < gp.screenHeight + 80) {
            g2.drawImage(enemyImage, screenX, screenY, 80, 80, null);
            drawHealthBar(g2, screenX, screenY - 10, 80, 10);
        }

        // Vẽ đạn ngoài khối culling: đạn đã ra khỏi enemy nhưng có thể vẫn trong screen
        for (Bullet bullet : bullets) {
            bullet.draw(g2, cameraX, cameraY);
        }
    }

    // Vẽ thanh máu
    private void drawHealthBar(Graphics2D g2, int x, int y, int width, int height) {
        // Background (red)
        g2.setColor(Color.RED);
        g2.fillRect(x, y, width, height);
        // Foreground (green)
        g2.setColor(Color.GREEN);
        int healthWidth = (int)((double)health / maxHealth * width);
        g2.fillRect(x, y, healthWidth, height);
        // Border
        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, width, height);
    }
}
