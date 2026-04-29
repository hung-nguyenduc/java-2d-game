package entity;

import java.util.ArrayList;
import java.util.List;

// Lớp cơ sở cho tất cả các thực thể trong game (Player, Enemy, etc.)
public class Entity {
    public int worldX, worldY; // Vị trí trong thế giới game
    public int speed; // Tốc độ di chuyển

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
}