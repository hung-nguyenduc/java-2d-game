package entity;

import main.GamePanel;
import java.awt.*;
import javax.swing.ImageIcon;

public class SkillGrenade extends Entity {
    GamePanel gp;
    public double targetX, targetY;
    public double startX, startY;
    
    private int travelTime = 40;
    private int currentFrame = 0;
    
    public boolean exploded = false;
    public int explosionTimer = 0;
    private int explosionRadius = 150;
    public int damage = 200;
    public boolean isDead = false;
    
    private static Image explosionImage;
    
    public SkillGrenade(GamePanel gp, double startX, double startY, double targetX, double targetY) {
        this.gp = gp;
        this.startX = startX;
        this.startY = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        
        this.worldX = startX;
        this.worldY = startY;
        
        if (explosionImage == null) {
            explosionImage = new ImageIcon(getClass().getResource("/weapon/bum.gif")).getImage();
        }
    }
    
    public void update() {
        if (exploded) {
            explosionTimer++;
            if (explosionTimer > 15) {
                isDead = true;
            }
            return;
        }
        
        currentFrame++;
        if (currentFrame >= travelTime) {
            explode();
        } else {
            // Tính toán vị trí hiện tại
            double progress = (double) currentFrame / travelTime;
            worldX = startX + (targetX - startX) * progress;
            worldY = startY + (targetY - startY) * progress;
        }
    }
    
    private void explode() {
        exploded = true;
        gp.sound.playSE("o_cai_tam_chat"); // Tiếng nổ lựu đạn
        // Gây sát thương diện rộng
        for (Enemy e : gp.enemies) {
            double dx = e.worldX - targetX;
            double dy = e.worldY - targetY;
            double dist = Math.sqrt(dx*dx + dy*dy);
            if (dist <= explosionRadius) {
                e.health -= damage;
            }
        }
    }
    
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int) (worldX - cameraX);
        int screenY = (int) (worldY - cameraY);
        
        if (exploded) {
            // Vẽ hiệu ứng nổ bằng bum.gif
            int drawSize = explosionRadius * 2;
            if (explosionImage != null) {
                // Sử dụng gp (GamePanel) làm ImageObserver để gif có thể tự animate
                g2.drawImage(explosionImage, screenX + 40 - drawSize/2, screenY + 40 - drawSize/2, drawSize, drawSize, gp);
            }
            return;
        }
        
        // Vẽ quỹ đạo cong (parabol)
        double progress = (double) currentFrame / travelTime;
        double height = 4 * 100 * progress * (1 - progress); 
        
        // Vẽ bóng lựu đạn dưới đất
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillOval(screenX + 35, screenY + 45, 15, 8);
        
        // Vẽ lựu đạn bay trên không
        g2.setColor(new Color(30, 80, 30));
        g2.fillOval(screenX + 35, (int) (screenY + 40 - height), 15, 15);
        g2.setColor(Color.BLACK);
        g2.drawOval(screenX + 35, (int) (screenY + 40 - height), 15, 15);
    }
}
