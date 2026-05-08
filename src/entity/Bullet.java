package entity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import entity.Player;

// Lớp đại diện cho đạn trong game
public class Bullet {
    public double worldX, worldY; // Vị trí trong thế giới
    public double vx, vy; // Vận tốc
    public double bulletSpeed = 8; // Tốc độ đạn
    public int bulletSize = 12; // Kích thước đạn
    public int maxRange = 1000; // Phạm vi tối đa
    public double travelDistance = 0; // Khoảng cách đã đi
    public Color color = Color.RED; // Màu đạn (mặc định đỏ — đạn quái)
    public BufferedImage bulletImg;
    public double angle; // Góc bắn của đạn

    // Constructor: Khởi tạo đạn với vị trí và góc bắn
    public Bullet(double startX, double startY, double angle) {
        this.worldX = startX;
        this.worldY = startY;
        this.angle = angle;

        // Convert angle to velocity
        this.vx = Math.cos(Math.toRadians(angle)) * bulletSpeed;
        this.vy = Math.sin(Math.toRadians(angle)) * bulletSpeed;
        getBulletImg();
    }
    public void getBulletImg() {
        try {
            bulletImg = ImageIO.read(getClass().getResourceAsStream("/weapon/bullet.png"));
        } catch (IOException e) {

        }
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

     //Vẽ đạn theo camera đã clamp
//    public void draw(Graphics2D g2, int cameraX, int cameraY) {
//        int screenX = (int) (worldX - cameraX);
//        int screenY = (int) (worldY - cameraY);
//
//        g2.setColor(color);
//        g2.fillOval(screenX, screenY, bulletSize, bulletSize);
//
//    }
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int) (worldX - cameraX);
        int screenY = (int) (worldY - cameraY);

        // Lưu trạng thái gốc
        AffineTransform original = g2.getTransform();

        // 1. Di chuyển đến tâm viên đạn
        g2.translate(screenX + bulletSize / 2.0, screenY + bulletSize / 2.0);

        // 2. Xoay hệ tọa độ theo góc của viên đạn
        g2.rotate(Math.toRadians(angle));

        // 3. Vẽ ảnh đạn (căn giữa)
        g2.drawImage(bulletImg, -bulletSize / 2, -bulletSize / 2, bulletSize, bulletSize, null);

        // Khôi phục trạng thái
        g2.setTransform(original);
    }
}
