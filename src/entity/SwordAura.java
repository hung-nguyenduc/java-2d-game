package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class SwordAura {
    public double worldX, worldY;
    public double speed = 10.0; // Đi nhanh hơn đạn
    public double angle;
    public double vx, vy;
    public boolean active = true;
    private int lifeTime = 0;
    
    public SwordAura(double x, double y, double angle) {
        this.worldX = x;
        this.worldY = y;
        this.angle = angle;
        
        // Tính vector vận tốc
        this.vx = Math.cos(Math.toRadians(angle)) * speed;
        this.vy = Math.sin(Math.toRadians(angle)) * speed;
    }
    
    public void update() {
        worldX += vx;
        worldY += vy;
        lifeTime++;
        
        // Tồn tại trong 60 frames (1 giây)
        if (lifeTime > 60) {
            active = false;
        }
    }
    
    public Rectangle getBounds() {
        return new Rectangle((int)worldX, (int)worldY, 40, 10);
    }
    
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int)worldX - cameraX;
        int screenY = (int)worldY - cameraY;
        
        java.awt.geom.AffineTransform old = g2.getTransform();
        g2.translate(screenX, screenY);
        g2.rotate(Math.toRadians(angle));
        
        // Vẽ hình lưỡi liềm hoặc đoạn thẳng đại diện cho kiếm khí
        g2.setColor(new Color(0, 255, 255, 200)); // Màu Cyan trong suốt
        g2.fillRoundRect(0, -5, 40, 10, 10, 10);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(0, -5, 40, 10, 10, 10);
        
        g2.setTransform(old);
    }
}
