package main;

import input.KeyHandler;
import entity.Player;
import entity.Enemy;
import entity.Bullet;
import manager.AssetManager;
import manager.MapManager;
import manager.ObjectManager;
import manager.CollisionManager;
import java.awt.Color;  // ← THÊM DÒNG NÀY
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable {
    private Thread gameThread;
    private KeyHandler keyHandler;
    
    // Các manager
    private MapManager mapManager;
    private ObjectManager objectManager;
    private CollisionManager collisionManager;
    
    // Entity
    private Player player;
    private ArrayList<Enemy> enemies;
    private ArrayList<Bullet> bullets;
    
    // Camera (cho map lớn)
    private int cameraX = 0, cameraY = 0;
    
    // Mouse để bắn
    private int mouseX = 0, mouseY = 0;
    private boolean mousePressed = false;
    
    private Random random;
    private boolean gameRunning = true;
    
    public GamePanel() {
        this.setPreferredSize(new Dimension(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT));
        this.setBackground(GameConfig.BACKGROUND_COLOR);
        this.setFocusable(true);
        
        // Khởi tạo input
        keyHandler = new KeyHandler();
        this.addKeyListener(keyHandler);
        
        // Mouse listener
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                mousePressed = true;
                mouseX = e.getX();
                mouseY = e.getY();
            }
            public void mouseReleased(java.awt.event.MouseEvent e) {
                mousePressed = false;
            }
        });
        
        this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });
        
        // Load tài nguyên (nếu có ảnh)
        AssetManager.loadAllImages();
        
        // Khởi tạo game
        initGame();
    }
    
    private void initGame() {
        // Khởi tạo các manager
        mapManager = new MapManager();
        objectManager = new ObjectManager();
        collisionManager = new CollisionManager(mapManager, objectManager);
        
        // Khởi tạo player ở giữa map
        player = new Player(GameConfig.WORLD_WIDTH / 2, GameConfig.WORLD_HEIGHT / 2, collisionManager);
        
        // Khởi tạo quái vật
        enemies = new ArrayList<>();
        random = new Random();
        
        // Thêm 5 con quái ở các vị trí khác nhau
        enemies.add(new Enemy(500, 500, player, collisionManager));
        enemies.add(new Enemy(800, 300, player, collisionManager));
        enemies.add(new Enemy(300, 800, player, collisionManager));
        enemies.add(new Enemy(1200, 600, player, collisionManager));
        enemies.add(new Enemy(1500, 400, player, collisionManager));
        
        // Khởi tạo đạn
        bullets = new ArrayList<>();
        
        gameRunning = true;
    }
    
    public void startGame() {
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    @Override
    public void run() {
        double drawInterval = 1000000000.0 / GameConfig.FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        
        while (gameThread != null) {
            long now = System.nanoTime();
            delta += (now - lastTime) / drawInterval;
            lastTime = now;
            
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }
    
    private void update() {
        if (!gameRunning) {
            // Xử lý restart
            if (keyHandler.restartPressed) {
                initGame();
            }
            return;
        }
        
        // ===== 1. XỬ LÝ DI CHUYỂN PLAYER =====
        int dx = 0, dy = 0;
        if (keyHandler.upPressed) dy -= 1;
        if (keyHandler.downPressed) dy += 1;
        if (keyHandler.leftPressed) dx -= 1;
        if (keyHandler.rightPressed) dx += 1;
        
        // Cập nhật hướng player dựa trên vị trí chuột
        if (mouseX >= 0 && mouseX <= GameConfig.SCREEN_WIDTH) {
            int centerX = (int)player.getX() + GameConfig.PLAYER_SIZE/2 - cameraX;
            int centerY = (int)player.getY() + GameConfig.PLAYER_SIZE/2 - cameraY;
            
            if (Math.abs(mouseX - centerX) > Math.abs(mouseY - centerY)) {
                player.setDirection(mouseX > centerX ? "right" : "left");
            } else {
                player.setDirection(mouseY > centerY ? "down" : "up");
            }
        }
        
        // Di chuyển player
        if (dx != 0 || dy != 0) {
            double length = Math.sqrt(dx*dx + dy*dy);
            dx = (int)Math.round(dx / length * GameConfig.PLAYER_SPEED);
            dy = (int)Math.round(dy / length * GameConfig.PLAYER_SPEED);
            player.move(dx, dy);
        }
        
        // ===== 2. XỬ LÝ BẮN ĐẠN =====
        if (mousePressed && player.canShoot() && gameRunning) {
            // Tính góc từ player đến chuột
            int playerCenterX = (int)player.getX() + GameConfig.PLAYER_SIZE/2;
            int playerCenterY = (int)player.getY() + GameConfig.PLAYER_SIZE/2;
            int targetX = mouseX + cameraX;
            int targetY = mouseY + cameraY;
            
            double angle = Math.atan2(targetY - playerCenterY, targetX - playerCenterX);
            
            Bullet bullet = new Bullet(
                player.getX() + GameConfig.PLAYER_SIZE/2 - GameConfig.BULLET_SIZE/2,
                player.getY() + GameConfig.PLAYER_SIZE/2 - GameConfig.BULLET_SIZE/2,
                angle, false, collisionManager
            );
            bullets.add(bullet);
            player.shotFired();
        }
        
        // ===== 3. CẬP NHẬT ĐẠN =====
        for (int i = 0; i < bullets.size(); i++) {
            Bullet bullet = bullets.get(i);
            bullet.update();
            
            // Xóa đạn ra khỏi map
            if (bullet.isOutOfBounds()) {
                bullets.remove(i);
                i--;
                continue;
            }
            
            // Đạn player chạm quái
            if (!bullet.isFromEnemy()) {
                for (int j = 0; j < enemies.size(); j++) {
                    Enemy enemy = enemies.get(j);
                    if (bullet.getHitbox().intersects(enemy.getHitbox())) {
                        enemy.takeDamage(25);
                        bullets.remove(i);
                        i--;
                        break;
                    }
                }
            }
            
            // Đạn quái chạm player
            if (bullet.isFromEnemy()) {
                if (bullet.getHitbox().intersects(player.getHitbox())) {
                    player.takeDamage(10);
                    bullets.remove(i);
                    i--;
                }
            }
        }
        
        // ===== 4. XÓA QUÁI CHẾT =====
        for (int i = 0; i < enemies.size(); i++) {
            if (!enemies.get(i).isAlive()) {
                enemies.remove(i);
                i--;
            }
        }
        
        // ===== 5. CẬP NHẬT QUÁI VẬT =====
        for (Enemy enemy : enemies) {
            enemy.update();
            
            // Quái bắn đạn
            if (enemy.canShoot() && enemy.isInRange(player) && gameRunning) {
                double angle = Math.atan2(
                    player.getY() - enemy.getY(),
                    player.getX() - enemy.getX()
                );
                
                Bullet bullet = new Bullet(
                    enemy.getX() + GameConfig.ENEMY_SIZE/2 - GameConfig.BULLET_SIZE/2,
                    enemy.getY() + GameConfig.ENEMY_SIZE/2 - GameConfig.BULLET_SIZE/2,
                    angle, true, collisionManager
                );
                bullets.add(bullet);
                enemy.shotFired();
            }
        }
        
        // ===== 6. CẬP NHẬT COOLDOWN =====
        player.updateCooldown();
        for (Enemy enemy : enemies) {
            enemy.updateCooldown();
        }
        
        // ===== 7. KIỂM TRA GAME OVER =====
        if (!player.isAlive()) {
            gameRunning = false;
        }
        
        // ===== 8. CẬP NHẬT CAMERA =====
        cameraX = (int)player.getX() - GameConfig.SCREEN_WIDTH / 2;
        cameraY = (int)player.getY() - GameConfig.SCREEN_HEIGHT / 2;
        
        // Giới hạn camera trong map
        cameraX = Math.max(0, Math.min(cameraX, GameConfig.WORLD_WIDTH - GameConfig.SCREEN_WIDTH));
        cameraY = Math.max(0, Math.min(cameraY, GameConfig.WORLD_HEIGHT - GameConfig.SCREEN_HEIGHT));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Vẽ map (nền và tường)
        mapManager.draw(g2, cameraX, cameraY);
        
        // Vẽ vật thể (bàn, ghế, tủ)
        objectManager.draw(g2, cameraX, cameraY);
        
        // Vẽ quái vật
        for (Enemy enemy : enemies) {
            enemy.draw(g2, cameraX, cameraY);
        }
        
        // Vẽ đạn
        for (Bullet bullet : bullets) {
            bullet.draw(g2, cameraX, cameraY);
        }
        
        // Vẽ player
        player.draw(g2, cameraX, cameraY);
        
        // Vẽ UI
        drawUI(g2);
        
        g2.dispose();
    }
    
    private void drawUI(Graphics2D g2) {
        // Khung UI
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, GameConfig.SCREEN_WIDTH, 100);
        
        // Thanh máu
        g2.setColor(Color.RED);
        g2.fillRect(10, 15, 200, 20);
        g2.setColor(Color.GREEN);
        int healthPercent = (player.getHealth() * 200) / player.getMaxHealth();
        g2.fillRect(10, 15, healthPercent, 20);
        g2.setColor(Color.WHITE);
        g2.drawRect(10, 15, 200, 20);
        
        // Text máu
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(Color.WHITE);
        g2.drawString("Health: " + player.getHealth() + "/" + player.getMaxHealth(), 15, 32);
        
        // Số quái còn lại
        g2.drawString("Enemies: " + enemies.size(), 10, 60);
        
        // Hướng dẫn
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString("WASD: Move | Mouse: Aim | Left Click: Shoot | R: Restart", 10, 85);
        
        // Tọa độ (debug)
        if (GameConfig.DEBUG_MODE) {
            g2.setColor(Color.YELLOW);
            g2.drawString("Position: " + (int)player.getX() + ", " + (int)player.getY(), 
                         GameConfig.SCREEN_WIDTH - 200, 20);
            g2.drawString("Camera: " + cameraX + ", " + cameraY, 
                         GameConfig.SCREEN_WIDTH - 200, 40);
            g2.drawString("Bullets: " + bullets.size(), 
                         GameConfig.SCREEN_WIDTH - 200, 60);
        }
        
        // Game Over
        if (!gameRunning) {
            g2.setFont(new Font("Arial", Font.BOLD, 48));
            g2.setColor(Color.RED);
            String gameOver = "GAME OVER! Press R to restart";
            int width = g2.getFontMetrics().stringWidth(gameOver);
            g2.drawString(gameOver, GameConfig.SCREEN_WIDTH/2 - width/2, GameConfig.SCREEN_HEIGHT/2);
        }
    }
}