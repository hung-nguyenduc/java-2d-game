package entity;

import java.awt.*;

// Lớp đại diện cho đạn trong game
public class Bullet {
    public double worldX, worldY; // Vị trí trong thế giới
    public double vx, vy; // Vận tốc
    public double bulletSpeed = 8; // Tốc độ đạn
    public int bulletSize = 12; // Kích thước đạn
    public int maxRange = 1000; // Phạm vi tối đa
    public double travelDistance = 0; // Khoảng cách đã đi
    public Color color = Color.RED; // Màu đạn (mặc định đỏ — đạn quái)

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

    // Vẽ đạn theo camera đã clamp
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int) (worldX - cameraX);
        int screenY = (int) (worldY - cameraY);

        g2.setColor(color);
        g2.fillOval(screenX, screenY, bulletSize, bulletSize);
    }
}
