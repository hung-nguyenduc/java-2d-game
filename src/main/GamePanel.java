package main;

import entity.Player; // Nhớ import package entity
import entity.Enemy; // Import Enemy class
import entity.Bullet; // Import Bullet class
import entity.Checkpoint; // Import Checkpoint class

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage; // Import lớp để xử lý ảnh
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Lớp chính quản lý panel game, vòng lặp game, và rendering
public class GamePanel extends JPanel implements Runnable, MouseListener {

    // -- CẤU HÌNH MÀN HÌNH (Giữ nguyên như cũ) --
    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize = originalTileSize * scale;
    final int maxScreenCol = 16;
    final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    // World dimensions
    public final int worldWidth = 2000;
    public final int worldHeight = 2000;

    // Enemy management
    public List<Enemy> enemies = new ArrayList<>();

    // -- THÊM VÀO 3 ÔNG TƯỚNG NÀY --
    KeyHandler keyH = new KeyHandler();
    Thread gameThread;
    Player player = new Player(this, keyH, enemies); // Truyền Panel và Bàn phím cho Player

    // Map management
    String[] mapPaths = {"/maps/c1.png", "/res/maps/test.png"};
    int currentMap = 0;
    Image mapImage;

    // Checkpoint
    Checkpoint checkpoint = null;

    // Game over flag
    public boolean gameOver = false;

    // Game state management
    private GameState currentState;

    // Constructor: Khởi tạo GamePanel
    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLUE);
        this.setDoubleBuffered(true);

        // Initialize state management - start with MenuState
        currentState = new MenuState(this);
        currentState.enter();

        // Add mouse listener for button clicks
        this.addMouseListener(this);

        // Load initial map
        loadMap();

        // HAI DÒNG NÀY CỰC KỲ QUAN TRỌNG ĐỂ NHẬN PHÍM
        this.addKeyListener(keyH);
        this.setFocusable(true); // Để GamePanel tập trung nhận input từ bàn phím

        // Spawn initial enemies
        spawnEnemies();

        // Spawn checkpoint at map center
        spawnCheckpoint();
    }

    // Load map dựa trên currentMap
    private void loadMap() {
        mapImage = new ImageIcon(getClass().getResource(mapPaths[currentMap])).getImage();
    }

    // Khởi động luồng game
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // Vòng lặp game chính (chạy ở 60 FPS)
    @Override
    public void run() {
        // Game Loop 60 FPS chuẩn
        double drawInterval = 1000000000.0 / 60; // 1 giây chia cho 60 FPS
        double nextDrawTime = System.nanoTime() + drawInterval;

        while(gameThread != null) {
            update();
            repaint();

            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime = remainingTime / 1000000;

                if(remainingTime < 0) remainingTime = 0;
                Thread.sleep((long) remainingTime);

                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Cập nhật trạng thái game mỗi frame
    public void update() {
        currentState.update();
    }

    // Vẽ tất cả các thành phần game
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        currentState.draw(g2);

        g2.dispose();
    }

    // Giới hạn camera không được nhìn thấy ngoài phạm vi map
    public int[] clampCameraPosition(int cameraX, int cameraY) {
        // Clamp camera X
        if (cameraX < 50) {
            cameraX = 0;
        }
        if (cameraX + screenWidth - 40 > worldWidth) {
            cameraX = worldWidth - screenWidth;
        }

        // Clamp camera Y
        if (cameraY < 50) {
            cameraY = 0;
        }
        if (cameraY + screenHeight -40 > worldHeight) {
            cameraY = worldHeight - screenHeight;
        }

        return new int[]{cameraX, cameraY};
    }

    // Sinh các enemy ban đầu
    private void spawnEnemies() {
        // Spawn 3 enemies with different types
        enemies.add(new Enemy(this, player, 300, 300, 0));
        enemies.add(new Enemy(this, player, 800, 500, 1));
        enemies.add(new Enemy(this, player, 1200, 700, 2));
    }

    // Sinh checkpoint tại vị trí giữa map
    private void spawnCheckpoint() {
        checkpoint = new Checkpoint(this, worldWidth / 2 - 40, worldHeight / 2 - 40);
    }

    // Set a new game state
    public void setState(GameState newState) {
        if (currentState != null) {
            currentState.exit();
        }
        currentState = newState;
        currentState.enter();
    }

    // MouseListener methods
    @Override
    public void mouseClicked(MouseEvent e) {
        currentState.handleMouseClick(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    // Kiểm tra va chạm giữa player và enemies
    public void checkCollisions() {
        // Check player-enemy collisions
        for (Enemy enemy : enemies) {
            if (player.worldX + 80 > enemy.worldX &&
                player.worldX < enemy.worldX + 80 &&
                player.worldY + 80 > enemy.worldY &&
                player.worldY < enemy.worldY + 80) {
                // Player hit by enemy
                player.health -= 1;
            }

            // Check player bullets hitting enemy
            for (int i = 0; i < player.bullets.size(); i++) {
                Bullet bullet = player.bullets.get(i);
                if (bullet.worldX + 10 > enemy.worldX &&
                    bullet.worldX < enemy.worldX + 80 &&
                    bullet.worldY + 10 > enemy.worldY &&
                    bullet.worldY < enemy.worldY + 80) {
                    // Enemy hit by player bullet
                    enemy.health -= 25;
                    player.bullets.remove(i);
                    i--;
                }
            }

            // Check enemy bullets hitting player
            for (int i = 0; i < enemy.bullets.size(); i++) {
                Bullet bullet = enemy.bullets.get(i);
                if (bullet.worldX + 10 > player.worldX &&
                    bullet.worldX < player.worldX + 80 &&
                    bullet.worldY + 10 > player.worldY &&
                    bullet.worldY < player.worldY + 80) {
                    // Player hit by enemy bullet
                    player.health -= 10;
                    enemy.bullets.remove(i);
                    i--;
                }
            }
        }

        // Remove dead enemies
        for (int i = 0; i < enemies.size(); i++) {
            if (enemies.get(i).health <= 0) {
                enemies.remove(i);
                i--;
            }
        }
    }

    // Chuyển sang map tiếp theo
    public void nextMap() {
        currentMap++;
        if (currentMap < mapPaths.length) {
            gameOver = false;
            player.health = player.maxHealth;
            // Transition to next level
            setState(new ZombieState(this));
        } else {
            gameOver = true;
        }
    }
}
