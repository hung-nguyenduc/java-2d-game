package entity;

import java.awt.Color;
import java.awt.Graphics2D;

public class Bomb {
    public double worldX, worldY;
    public int lifeTime = 0;
    public int explodeTime = 120; // 2 giây
    public int explosionRadius = 150; // Phạm vi nổ
    public boolean exploded = false;
    public boolean active = true;
    public int explosionDuration = 15; // Nổ tồn tại 15 frames
    
    public Bomb(double x, double y) {
        this.worldX = x;
        this.worldY = y;
    }
    
    public void update() {
        if (!exploded) {
            lifeTime++;
            if (lifeTime >= explodeTime) {
                exploded = true;
                lifeTime = 0; // Tái sử dụng lifeTime để đếm tgian vụ nổ tồn tại
            }
        } else {
            lifeTime++;
            if (lifeTime >= explosionDuration) {
                active = false;
            }
        }
    }
    
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int)worldX - cameraX;
        int screenY = (int)worldY - cameraY;
        
        if (!exploded) {
            // Vẽ quả bom (nhấp nháy)
            if (lifeTime % 20 < 10) {
                g2.setColor(Color.RED);
            } else {
                g2.setColor(new Color(150, 0, 0));
            }
            g2.fillOval(screenX, screenY, 30, 30);
            g2.setColor(Color.BLACK);
            g2.drawOval(screenX, screenY, 30, 30);
        } else {
            // Vẽ vụ nổ
            g2.setColor(new Color(255, 100, 0, 180));
            g2.fillOval(screenX + 15 - explosionRadius/2, screenY + 15 - explosionRadius/2, explosionRadius, explosionRadius);
            g2.setColor(new Color(255, 200, 0, 200));
            g2.fillOval(screenX + 15 - explosionRadius/3, screenY + 15 - explosionRadius/3, explosionRadius*2/3, explosionRadius*2/3);
        }
    }
}
