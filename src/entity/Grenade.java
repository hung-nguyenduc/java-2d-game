package entity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Grenade {
    public double worldX, worldY;
    public double vx, vy; // Vận tốc theo 2 trục
    public double gravity = 0.35; // Lực hút trái đất làm lựu đạn rơi xuống
    public int size = 24;
    public BufferedImage grenadeImg;
    public boolean isExploded = false;

    // Đếm ngược thời gian nổ sau khi chạm đất hoặc bay hết tầm
    public int lifeTime = 0;
    public final int MAX_LIFETIME = 120; // 2 giây ở 60FPS

    public Grenade(double startX, double startY, double angle, double power) {
        this.worldX = startX;
        this.worldY = startY;

        // Tính vận tốc ban đầu dựa trên góc ném và LỰC ném (Power) giống Angry Birds
        this.vx = Math.cos(Math.toRadians(angle)) * power;
        this.vy = Math.sin(Math.toRadians(angle)) * power;

        try {
            grenadeImg = ImageIO.read(getClass().getResourceAsStream("/weapon/bullet.png"));
        } catch (IOException e) {
            System.err.println("Chưa có ảnh lựu đạn, sẽ vẽ hình tròn tạm thời.");
        }
    }

    public void update() {
        // Thuật toán vật lý lý thuyết Parabol
        worldX += vx;
        vy += gravity; // Vận tốc Y tăng dần theo thời gian khiến quả lựu đạn cắm xuống
        worldY += vy;

        lifeTime++;
        if (lifeTime >= MAX_LIFETIME) {
            isExploded = true; // Hết giờ tự nổ
        }
    }

    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int) (worldX - cameraX);
        int screenY = (int) (worldY - cameraY);

        if (grenadeImg != null) {
            g2.drawImage(grenadeImg, screenX - size/2, screenY - size/2, size, size, null);
        } else {
            // Khử lỗi bằng hình tròn màu xanh quân lục nếu thiếu ảnh
            g2.setColor(new Color(34, 139, 34));
            g2.fillOval(screenX - size/2, screenY - size/2, size, size);
        }
    }
}