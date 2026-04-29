package entity;

import java.awt.*;

// Lớp đại diện cho đạn trong game
public class Bullet {
    public double worldX, worldY; // Vị trí trong thế giới
    public double vx, vy; // Vận tốc
    public double bulletSpeed = 8; // Tốc độ đạn
    public int bulletSize = 8; // Kích thước đạn
    public int maxRange = 1000; // Phạm vi tối đa
    public double travelDistance = 0; // Khoảng cách đã đi

    // Constructor: Khởi tạo đạn với vị trí và góc bắn
    public Bullet(double startX, double startY, double angle) {
        this.worldX = startX;
        this.worldY = startY;

        // Convert angle to velocity
        this.vx = Math.cos(Math.toRadians(angle)) * bulletSpeed;
        this.vy = Math.sin(Math.toRadians(angle)) * bulletSpeed;
    }

    // Cập nhật vị trí đạn mỗi frame
    public void update() {
        worldX += vx;
        worldY += vy;
        travelDistance += bulletSpeed;
    }

    // Kiểm tra xem đạn có vượt quá phạm vi không
    public boolean isOutOfRange() {
        return travelDistance > maxRange;
    }

    // Vẽ đạn lên màn hình
    public void draw(Graphics2D g2, int playerWorldX, int playerWorldY, int screenWidth, int screenHeight, int tileSize) {
        int screenX = (int)(worldX - playerWorldX + screenWidth / 2);
        int screenY = (int)(worldY - playerWorldY + screenHeight / 2);

        // Only draw if on screen
        if(screenX > -20 && screenX < screenWidth + 20 && screenY > -20 && screenY < screenHeight + 20) {
            g2.setColor(Color.RED);
            g2.fillOval(screenX, screenY, bulletSize, bulletSize);
        }
    }
}
