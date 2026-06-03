package entity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Grenade {
    public double worldX, worldY;
    public double vx, vy;
    public double gravity = 0.4;
    public double bounceFactor = 0.45;
    public double friction = 0.90;

    public int size = 24;
    public BufferedImage grenadeImg;
    public boolean isExploded = false;

    // ĐIỀU CHỈNH: Thêm biến đếm số lần chạm đất
    public int bounceCount = 0;
    public boolean isFirstTouchHandled = false; // Đánh dấu đã check lượt chạm đầu chưa

    public int lifeTime = 0;
    public final int MAX_LIFETIME = 150;

    public Grenade(double startX, double startY, double angle, double power) {
        this.worldX = startX;
        this.worldY = startY;
        this.vx = Math.cos(Math.toRadians(angle)) * power;
        this.vy = Math.sin(Math.toRadians(angle)) * power;

        try {
            grenadeImg = ImageIO.read(getClass().getResourceAsStream("/weapon/bullet.png"));
        } catch (IOException e) {
            System.err.println("Chưa có ảnh lựu đạn, dùng hình tròn tạm thời.");
        }
    }

    public void update(int groundY) {
        lifeTime++;
        if (lifeTime >= MAX_LIFETIME) {
            isExploded = true;
            return;
        }

        if (worldY + size / 2.0 < groundY) {
            vy += gravity;
        }

        worldX += vx;
        worldY += vy;

        // XỬ LÝ VA CHẠM MẶT ĐẤT
        if (worldY + size / 2.0 >= groundY) {
            worldY = groundY - size / 2.0;

            // Nếu đây là tích tắc đầu tiên chạm đất, tăng biến đếm bounce
            if (vy > 0 && !isFirstTouchHandled) {
                bounceCount++;
            }

            vy = -vy * bounceFactor;
            vx *= friction;

            if (Math.abs(vy) < 0.8) {
                vy = 0;
            }
            if (Math.abs(vx) < 0.1) {
                vx = 0;
            }
        }
    }

    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int) (worldX - cameraX);
        int screenY = (int) (worldY - cameraY);

        if (grenadeImg != null) {
            g2.drawImage(grenadeImg, screenX - size/2, screenY - size/2, size, size, null);
        } else {
            g2.setColor(new Color(34, 139, 34));
            g2.fillOval(screenX - size/2, screenY - size/2, size, size);
        }
    }
}