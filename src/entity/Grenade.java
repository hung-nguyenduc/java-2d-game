package entity;

import main.GamePanel;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Font;

public class Grenade {
    GamePanel gp;
    public double worldX, worldY;
    public double targetX, targetY;
    private double startX, startY;
    
    private double speed = 6.0;
    private double distanceTraveled = 0;
    private double totalDistance;
    
    // Parabola effect
    private double maxZ = 80.0; // Peak height in pixels
    private double currentZ = 0;
    
    private int fuseTimer = 240; // 4 seconds at 60 FPS
    
    public boolean isExploding = false;
    private int explosionTimer = 30; // Explosion lasts 0.5 sec
    public int explosionRadius = 120;
    
    public boolean isActive = true;
    public boolean damageDealt = false; // To ensure we only apply damage once
    
    private BufferedImage image;
    
    public Grenade(GamePanel gp, double startX, double startY, double targetX, double targetY) {
        this.gp = gp;
        this.worldX = startX;
        this.worldY = startY;
        this.startX = startX;
        this.startY = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        
        double dx = targetX - startX;
        double dy = targetY - startY;
        this.totalDistance = Math.sqrt(dx * dx + dy * dy);
        
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/items/grenade.png"));
        } catch (Exception e) {}
    }
    
    public void update() {
        if (!isActive) return;
        
        if (isExploding) {
            explosionTimer--;
            if (explosionTimer <= 0) {
                isActive = false;
            }
            return;
        }
        
        fuseTimer--;
        if (fuseTimer <= 0) {
            isExploding = true;
            return;
        }
        
        // Move towards target if not there yet
        if (distanceTraveled < totalDistance) {
            double dx = targetX - startX;
            double dy = targetY - startY;
            double step = speed;
            if (distanceTraveled + step > totalDistance) {
                step = totalDistance - distanceTraveled;
            }
            distanceTraveled += step;
            
            double ratio = distanceTraveled / totalDistance;
            worldX = startX + dx * ratio;
            worldY = startY + dy * ratio;
            
            // Parabola formula
            currentZ = maxZ * 4 * ratio * (1 - ratio);
        } else {
            currentZ = 0; // Hit the ground
        }
    }
    
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        if (!isActive) return;
        
        int screenX = (int)worldX - cameraX;
        int screenY = (int)worldY - cameraY;
        
        if (isExploding) {
            // Draw explosion effect
            g2.setColor(new Color(255, 100, 0, 150));
            g2.fillOval(screenX - explosionRadius, screenY - explosionRadius, explosionRadius * 2, explosionRadius * 2);
            g2.setColor(new Color(255, 200, 0, 200));
            g2.fillOval(screenX - explosionRadius/2, screenY - explosionRadius/2, explosionRadius, explosionRadius);
        } else {
            // Draw shadow
            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillOval(screenX - 10, screenY - 5, 20, 10);
            
            // Draw grenade with Z offset
            if (image != null) {
                g2.drawImage(image, screenX - 16, screenY - 16 - (int)currentZ, 32, 32, null);
            } else {
                g2.setColor(new Color(0, 100, 0));
                g2.fillOval(screenX - 8, screenY - 8 - (int)currentZ, 16, 16);
            }
            
            // Draw fuse timer text (red countdown)
            if (currentZ == 0) { 
                g2.setColor(Color.RED);
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                g2.drawString(String.format("%.1f", fuseTimer / 60.0), screenX - 10, screenY - 20);
            }
        }
    }
}
