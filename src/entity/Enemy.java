package entity;

import main.GameConfig;
import manager.CollisionManager;
import manager.AssetManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Enemy {
    private float x, y;
    private int health;
    private int maxHealth;
    private float speed;
    private Rectangle hitbox;
    private boolean isAlive;
    private String direction;
    
    private Player player;
    private CollisionManager collisionManager;
    private Random random;
    
    // AI states
    private enum State { IDLE, CHASE }
    private State currentState = State.IDLE;
    
    private int idleTimer = 0;
    private String idleDirection = "down";
    
    // Bắn đạn
    private int shootCooldown = 0;
    private int shootDelay = 60;
    private int attackRange = 350;
    
    // Ảnh quái
    private BufferedImage enemyImage;
    
    public Enemy(float x, float y, Player player, CollisionManager cm) {
        this.x = x;
        this.y = y;
        this.health = 50;
        this.maxHealth = 50;
        this.speed = GameConfig.ENEMY_SPEED;
        this.isAlive = true;
        this.direction = "down";
        this.player = player;
        this.collisionManager = cm;
        this.random = new Random();
        this.hitbox = new Rectangle((int)x, (int)y, GameConfig.ENEMY_SIZE, GameConfig.ENEMY_SIZE);
        
        // Load ảnh quái
        enemyImage = AssetManager.getImage("enemy_red");
    }
    
    public void update() {
        if (!isAlive) return;
        
        float dx = player.getX() - x;
        float dy = player.getY() - y;
        float distance = (float)Math.sqrt(dx*dx + dy*dy);
        
        if (distance < attackRange) {
            currentState = State.CHASE;
        } else {
            currentState = State.IDLE;
        }
        
        switch(currentState) {
            case CHASE:
                chasePlayer(dx, dy);
                break;
            case IDLE:
                idleWander();
                break;
        }
        
        updateHitbox();
    }
    
    private void chasePlayer(float dx, float dy) {
        float length = (float)Math.sqrt(dx*dx + dy*dy);
        if (length > 0) {
            int moveX = (int)((dx / length) * speed);
            int moveY = (int)((dy / length) * speed);
            
            float newX = x + moveX;
            float newY = y + moveY;
            
            // Giới hạn trong map
            newX = Math.max(0, Math.min(newX, GameConfig.WORLD_WIDTH - GameConfig.ENEMY_SIZE));
            newY = Math.max(0, Math.min(newY, GameConfig.WORLD_HEIGHT - GameConfig.ENEMY_SIZE));
            
            Rectangle newHitbox = new Rectangle((int)newX, (int)y, GameConfig.ENEMY_SIZE, GameConfig.ENEMY_SIZE);
            if (!collisionManager.checkWallCollision(newHitbox) && !collisionManager.checkObjectCollision(newHitbox)) {
                x = newX;
            }
            
            newHitbox = new Rectangle((int)x, (int)newY, GameConfig.ENEMY_SIZE, GameConfig.ENEMY_SIZE);
            if (!collisionManager.checkWallCollision(newHitbox) && !collisionManager.checkObjectCollision(newHitbox)) {
                y = newY;
            }
        }
        
        if (Math.abs(dx) > Math.abs(dy)) {
            direction = dx > 0 ? "right" : "left";
        } else {
            direction = dy > 0 ? "down" : "up";
        }
    }
    
    private void idleWander() {
        idleTimer++;
        
        if (idleTimer > 60) {
            idleTimer = 0;
            int randDir = random.nextInt(4);
            switch(randDir) {
                case 0: idleDirection = "up"; break;
                case 1: idleDirection = "down"; break;
                case 2: idleDirection = "left"; break;
                case 3: idleDirection = "right"; break;
            }
        }
        
        int moveX = 0, moveY = 0;
        switch(idleDirection) {
            case "up": moveY = -1; break;
            case "down": moveY = 1; break;
            case "left": moveX = -1; break;
            case "right": moveX = 1; break;
        }
        
        float newX = x + moveX;
        float newY = y + moveY;
        
        newX = Math.max(0, Math.min(newX, GameConfig.WORLD_WIDTH - GameConfig.ENEMY_SIZE));
        newY = Math.max(0, Math.min(newY, GameConfig.WORLD_HEIGHT - GameConfig.ENEMY_SIZE));
        
        Rectangle newHitbox = new Rectangle((int)newX, (int)newY, GameConfig.ENEMY_SIZE, GameConfig.ENEMY_SIZE);
        if (!collisionManager.checkWallCollision(newHitbox) && !collisionManager.checkObjectCollision(newHitbox)) {
            x = newX;
            y = newY;
            direction = idleDirection;
        } else {
            idleTimer = 60;
        }
    }
    
    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
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
    
    public boolean isInRange(Player p) {
        float dx = p.getX() - x;
        float dy = p.getY() - y;
        float distance = (float)Math.sqrt(dx*dx + dy*dy);
        return distance < attackRange;
    }
    
    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isAlive() { return isAlive; }
    public Rectangle getHitbox() { return hitbox; }
    
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int)x - cameraX;
        int screenY = (int)y - cameraY;
        
        if (enemyImage != null && AssetManager.hasImages()) {
            g2.drawImage(enemyImage, screenX, screenY, GameConfig.ENEMY_SIZE, GameConfig.ENEMY_SIZE, null);
        } else {
            // Fallback
            g2.setColor(GameConfig.ENEMY_COLOR);
            g2.fillOval(screenX, screenY, GameConfig.ENEMY_SIZE, GameConfig.ENEMY_SIZE);
            
            g2.setColor(Color.WHITE);
            switch(direction) {
                case "up":
                    g2.fillOval(screenX + 12, screenY + 12, 6, 6);
                    g2.fillOval(screenX + 30, screenY + 12, 6, 6);
                    break;
                case "down":
                    g2.fillOval(screenX + 12, screenY + 30, 6, 6);
                    g2.fillOval(screenX + 30, screenY + 30, 6, 6);
                    break;
                case "left":
                    g2.fillOval(screenX + 10, screenY + 15, 6, 6);
                    g2.fillOval(screenX + 10, screenY + 27, 6, 6);
                    break;
                case "right":
                    g2.fillOval(screenX + 32, screenY + 15, 6, 6);
                    g2.fillOval(screenX + 32, screenY + 27, 6, 6);
                    break;
            }
        }
        
        // Vẽ thanh máu
        int barWidth = GameConfig.ENEMY_SIZE;
        int barHeight = 5;
        int healthPercent = (health * barWidth) / maxHealth;
        g2.setColor(Color.RED);
        g2.fillRect(screenX, screenY - 8, barWidth, barHeight);
        g2.setColor(Color.GREEN);
        g2.fillRect(screenX, screenY - 8, healthPercent, barHeight);
        
        if (GameConfig.DEBUG_MODE) {
            g2.setColor(Color.RED);
            g2.drawRect(screenX, screenY, GameConfig.ENEMY_SIZE, GameConfig.ENEMY_SIZE);
        }
    }
}