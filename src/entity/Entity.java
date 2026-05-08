package entity;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

// Lớp cơ sở cho tất cả các thực thể trong game (Player, Enemy, etc.)
public class Entity {
    // double để di chuyển subpixel mượt mà (tránh jitter khi đi chéo: speed*0.7071 không tròn)
    public double worldX, worldY; // Vị trí trong thế giới game
    public double speed; // Tốc độ di chuyển (double để hỗ trợ giá trị lẻ như 4.5)

    // Velocity for smooth diagonal movement
    public double vx = 0; // Vận tốc theo trục X
    public double vy = 0; // Vận tốc theo trục Y

    // Aiming direction (in degrees: 0=right, 90=down, 180=left, 270=up)
    public double aimAngle = 0; // Góc nhắm (độ)

    // Bullet list
    public List<Bullet> bullets = new ArrayList<>(); // Danh sách đạn của thực thể

    // Health
    public int maxHealth = 100; // Máu tối đa
    public int health = 100; // Máu hiện tại

    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    public String direction;

    public int spriteCounter = 0;
    public int spriteNum = 1;
}