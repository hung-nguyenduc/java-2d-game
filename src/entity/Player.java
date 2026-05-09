package entity;

import main.GameConfig;
import manager.CollisionManager;
import manager.AssetManager;
import java.awt.image.BufferedImage;
import java.awt.Color;  // ← THÊM DÒNG NÀY
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Player {
    private float x, y;
    private int health;
    private int maxHealth;
    private Rectangle hitbox;
    private String direction;
    private boolean isAlive;
    
    // Bắn đạn
    private int shootCooldown = 0;
    private int shootDelay = 15;
    
    private CollisionManager collisionManager;
    
    // Ảnh player
    private BufferedImage currentImage;
    private BufferedImage[] playerImages;
    
    public Player(float startX, float startY, CollisionManager cm) {
        this.x = startX;
        this.y = startY;
        this.health = 100;
        this.maxHealth = 100;
        this.direction = "down";
        this.isAlive = true;
        this.collisionManager = cm;
        this.hitbox = new Rectangle((int)x, (int)y, GameConfig.PLAYER_SIZE, GameConfig.PLAYER_SIZE);
        
        // Load ảnh player (nếu có)
        playerImages = new BufferedImage[5];
        playerImages[0] = AssetManager.getImage("player_idle");
        playerImages[1] = AssetManager.getImage("player_up");
        playerImages[2] = AssetManager.getImage("player_down");
        playerImages[3] = AssetManager.getImage("player_left");
        playerImages[4] = AssetManager.getImage("player_right");
        currentImage = playerImages[0];
    }
    
    public void move(int dx, int dy) {
        if (!isAlive) return;
        
        float newX = x + dx;
        float newY = y + dy;
        
        // Giới hạn trong map
        newX = Math.max(0, Math.min(newX, GameConfig.WORLD_WIDTH - GameConfig.PLAYER_SIZE));
        newY = Math.max(0, Math.min(newY, GameConfig.WORLD_HEIGHT - GameConfig.PLAYER_SIZE));
        
        Rectangle newHitbox = new Rectangle((int)newX, (int)y, GameConfig.PLAYER_SIZE, GameConfig.PLAYER_SIZE);
        if (!collisionManager.checkWallCollision(newHitbox) && !collisionManager.checkObjectCollision(newHitbox)) {
            x = newX;
        }
        
        newHitbox = new Rectangle((int)x, (int)newY, GameConfig.PLAYER_SIZE, GameConfig.PLAYER_SIZE);
        if (!collisionManager.checkWallCollision(newHitbox) && !collisionManager.checkObjectCollision(newHitbox)) {
            y = newY;
        }
        
        updateHitbox();
    }
    
    public void takeDamage(int damage) {
        if (!isAlive) return;
        health -= damage;
        if (health <= 0) {
            health = 0;
            isAlive = false;
        }
    }
    
    private void updateHitbox() {
        hitbox.x = (int)x;
        hitbox.y = (int)y;
    }
    
    public void updateCooldown() {
        if (shootCooldown > 0) {
            shootCooldown--;
        }
    }
    
    public boolean canShoot() {
        return isAlive && shootCooldown <= 0;
    }
    
    public void shotFired() {
        shootCooldown = shootDelay;
    }
    
    public void setDirection(String dir) {
        this.direction = dir;
    }
    
    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isAlive() { return isAlive; }
    public Rectangle getHitbox() { return hitbox; }
    
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int)x - cameraX;
        int screenY = (int)y - cameraY;
        
        // Chọn ảnh theo hướng
        switch(direction) {
            case "up": currentImage = playerImages[1]; break;
            case "down": currentImage = playerImages[2]; break;
            case "left": currentImage = playerImages[3]; break;
            case "right": currentImage = playerImages[4]; break;
            default: currentImage = playerImages[0];
        }
        
        // Vẽ player
        if (currentImage != null && AssetManager.hasImages()) {
            g2.drawImage(currentImage, screenX, screenY, GameConfig.PLAYER_SIZE, GameConfig.PLAYER_SIZE, null);
        } else {
            // Fallback: vẽ hình chữ nhật
            g2.setColor(isAlive ? GameConfig.PLAYER_COLOR : Color.GRAY);
            g2.fillRect(screenX, screenY, GameConfig.PLAYER_SIZE, GameConfig.PLAYER_SIZE);
            
            // Vẽ mặt
            g2.setColor(Color.WHITE);
            switch(direction) {
                case "up":
                    g2.fillRect(screenX + 15, screenY + 10, 6, 6);
                    g2.fillRect(screenX + 27, screenY + 10, 6, 6);
                    break;
                case "down":
                    g2.fillRect(screenX + 15, screenY + 32, 6, 6);
                    g2.fillRect(screenX + 27, screenY + 32, 6, 6);
                    break;
                case "left":
                    g2.fillRect(screenX + 10, screenY + 15, 6, 6);
                    g2.fillRect(screenX + 10, screenY + 27, 6, 6);
                    break;
                case "right":
                    g2.fillRect(screenX + 32, screenY + 15, 6, 6);
                    g2.fillRect(screenX + 32, screenY + 27, 6, 6);
                    break;
            }
        }
        
        // Vẽ hitbox (debug)
        if (GameConfig.DEBUG_MODE) {
            g2.setColor(Color.RED);
            g2.drawRect(screenX, screenY, GameConfig.PLAYER_SIZE, GameConfig.PLAYER_SIZE);
        }
    }
}