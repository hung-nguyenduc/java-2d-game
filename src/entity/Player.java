package entity;

import main.GamePanel;
import main.KeyHandler;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

public class Player extends Entity {
    GamePanel gp;
    KeyHandler keyH;
    List<Enemy> enemies;

    public BufferedImage playerImage;
    private int shootCooldown = 0;
    private final int shootInterval = 30;

    // Accumulate position as double để tránh mất precision khi normalize chéo
    private double accX = 1000, accY = 1000;

    private static final double DIAGONAL_FACTOR = 1.0 / Math.sqrt(2);
    private static final BasicStroke AIM_STROKE = new BasicStroke(2);

    // Constructor: Khởi tạo Player với GamePanel và KeyHandler
    public Player(GamePanel gp, KeyHandler keyH, List<Enemy> enemies) {
        this.gp = gp;
        this.keyH = keyH;
        this.enemies = enemies;

        setDefaultValues();
        getPlayerImage();
    }

    // Thiết lập giá trị mặc định cho Player
    public void setDefaultValues() {
        worldX = 1000;
        worldY = 1000;
        accX = 1000;
        accY = 1000;
        speed = 4;
        aimAngle = 0;
        health = maxHealth;
    }

    // Tải hình ảnh của Player
    public void getPlayerImage() {
        try {
            var is = getClass().getResourceAsStream("/player/player.png");
            if (is == null) {
                System.out.println("LỖI: Không tìm thấy ảnh nhân vật!");
                return;
            }
            playerImage = ImageIO.read(is);
        } catch (IOException e) {
            System.out.println("LỖI: Không đọc được ảnh nhân vật!");
            e.printStackTrace();
        }
    }

    // Cập nhật trạng thái của Player mỗi frame
    public void update() {
        vx = 0;
        vy = 0;

        if (keyH.upPressed)    vy -= speed;
        if (keyH.downPressed)  vy += speed;
        if (keyH.leftPressed)  vx -= speed;
        if (keyH.rightPressed) vx += speed;

        // Normalize diagonal: giữ tốc độ bằng nhau mọi hướng
        if (vx != 0 && vy != 0) {
            vx *= DIAGONAL_FACTOR;
            vy *= DIAGONAL_FACTOR;
        }

        // Tích lũy bằng double, gán int sau để tránh mất precision
        accX += vx;
        accY += vy;
        worldX = (int) accX;
        worldY = (int) accY;

        // Giới hạn vị trí nhân vật trong map
        clampPlayerPosition();
        // Sync accumulator sau khi clamp để tránh drift vào tường
        accX = worldX;
        accY = worldY;

        // Find nearest enemy and aim at it
        Enemy nearestEnemy = findNearestEnemy();
        if (nearestEnemy != null) {
            double dx = nearestEnemy.worldX - worldX;
            double dy = nearestEnemy.worldY - worldY;
            aimAngle = Math.toDegrees(Math.atan2(dy, dx));
        }

        // Automatic shooting at nearest enemy
        shootCooldown++;
        if (shootCooldown >= shootInterval && nearestEnemy != null) {
            shoot();
            shootCooldown = 0;
        }

        // Update bullets
        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).update();
            if (bullets.get(i).isOutOfRange()) {
                bullets.remove(i);
                i--;
            }
        }
    }

    // Giới hạn vị trí nhân vật không cho phép vượt ra ngoài map
    private void clampPlayerPosition() {
        // Player size is 80x80
        int playerSize = 80;

        // Clamp X position
        if (worldX < 0) {
            worldX = 0;
        }
        if (worldX + playerSize > gp.worldWidth) {
            worldX = gp.worldWidth - playerSize;
        }

        // Clamp Y position
        if (worldY < 0) {
            worldY = 0;
        }
        if (worldY + playerSize > gp.worldHeight) {
            worldY = gp.worldHeight - playerSize;
        }
    }

    // Tìm enemy gần nhất (so sánh bình phương khoảng cách → bỏ sqrt)
    private Enemy findNearestEnemy() {
        Enemy nearest = null;
        double minDistanceSq = Double.MAX_VALUE;
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            double dx = enemy.worldX - worldX;
            double dy = enemy.worldY - worldY;
            double distanceSq = dx * dx + dy * dy;
            if (distanceSq < minDistanceSq) {
                minDistanceSq = distanceSq;
                nearest = enemy;
            }
        }
        return nearest;
    }

    // Bắn đạn nếu cooldown cho phép
    public void shoot() {
        // Create bullet at player position
        Bullet bullet = new Bullet(worldX + 40, worldY + 40, aimAngle);
        bullets.add(bullet);
    }

    // Vẽ Player và các thành phần liên quan
    public void draw(Graphics2D g2) {
        int screenX = gp.screenWidth / 2 - (gp.tileSize / 2);
        int screenY = gp.screenHeight / 2 - (gp.tileSize / 2);

        g2.drawImage(playerImage, screenX, screenY, 80, 80, null);

        // Draw health bar
        drawHealthBar(g2, screenX, screenY - 10, 80, 10);

        // Draw aiming direction indicator
        drawAimingIndicator(g2, screenX + 40, screenY + 40);

        // Draw bullets
        for (Bullet bullet : bullets) {
            bullet.draw(g2, worldX, worldY, gp.screenWidth, gp.screenHeight, gp.tileSize);
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

    // Vẽ chỉ báo hướng nhắm
    private void drawAimingIndicator(Graphics2D g2, int centerX, int centerY) {
        int indicatorLength = 30;
        double radians = Math.toRadians(aimAngle);
        int endX = (int)(centerX + indicatorLength * Math.cos(radians));
        int endY = (int)(centerY + indicatorLength * Math.sin(radians));

        g2.setColor(Color.RED);
        g2.setStroke(AIM_STROKE);
        g2.drawLine(centerX, centerY, endX, endY);
    }
}