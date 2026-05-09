package entity;

import main.GameConfig;
import manager.CollisionManager;
import manager.AssetManager;
import java.awt.image.BufferedImage;
import java.awt.Color;  // ← THÊM DÒNG NÀY
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Bullet {
    private float x, y;
    private float speedX, speedY;
    private boolean fromEnemy;
    private Rectangle hitbox;
    private BufferedImage bulletImage;
    private CollisionManager collisionManager;
    
    public Bullet(float x, float y, double angle, boolean fromEnemy, CollisionManager cm) {
        this.x = x;
        this.y = y;
        this.fromEnemy = fromEnemy;
        this.collisionManager = cm;
        
        this.speedX = (float)(Math.cos(angle) * GameConfig.BULLET_SPEED);
        this.speedY = (float)(Math.sin(angle) * GameConfig.BULLET_SPEED);
        
        this.hitbox = new Rectangle((int)x, (int)y, GameConfig.BULLET_SIZE, GameConfig.BULLET_SIZE);
        
        // Load ảnh đạn
        if (fromEnemy) {
            bulletImage = AssetManager.getImage("bullet_enemy");
        } else {
            bulletImage = AssetManager.getImage("bullet_player");
        }
    }
    
    public void update() {
        x += speedX;
        y += speedY;
        hitbox.x = (int)x;
        hitbox.y = (int)y;
    }
    
    public boolean isOutOfBounds() {
        return x < -100 || x > GameConfig.WORLD_WIDTH + 100 ||
               y < -100 || y > GameConfig.WORLD_HEIGHT + 100;
    }
    
    public Rectangle getHitbox() {
        return hitbox;
    }
    
    public boolean isFromEnemy() {
        return fromEnemy;
    }
    
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int)x - cameraX;
        int screenY = (int)y - cameraY;
        
        if (bulletImage != null && AssetManager.hasImages()) {
            g2.drawImage(bulletImage, screenX, screenY, GameConfig.BULLET_SIZE, GameConfig.BULLET_SIZE, null);
        } else {
            g2.setColor(fromEnemy ? GameConfig.BULLET_ENEMY_COLOR : GameConfig.BULLET_PLAYER_COLOR);
            g2.fillOval(screenX, screenY, GameConfig.BULLET_SIZE, GameConfig.BULLET_SIZE);
        }
    }
}