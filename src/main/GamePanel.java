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
import java.awt.image.BufferedImage; // Import lớp để xử lý ảnh
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Lớp chính quản lý panel game, vòng lặp game, và rendering
public class GamePanel extends JPanel implements Runnable {

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

    // Constructor: Khởi tạo GamePanel
    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLUE);
        this.setDoubleBuffered(true);

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
        double drawInterval = 1000000000 / 60; // 1 giây chia cho 60 FPS
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
        if (!gameOver) {
            // Gọi hàm update của nhân vật
            player.update();

            // Update enemies
            for (Enemy enemy : enemies) {
                enemy.update();
            }

            // Check checkpoint collision
            if (checkpoint != null) {
                checkCheckpointCollision();
            }

            // Check collisions
            checkCollisions();

            // Check game over
            if (player.health <= 0) {
                gameOver = true;
            }
        }
    }

    // Sinh checkpoint tại vị trí trung tâm map
    private void spawnCheckpoint() {
        checkpoint = new Checkpoint(this, worldWidth / 2 - 40, worldHeight / 2 - 40);
    }

    // Kiểm tra va chạm với checkpoint
    private void checkCheckpointCollision() {
        if (player.worldX + 80 >= checkpoint.worldX && player.worldX <= checkpoint.worldX + checkpoint.sizeX &&
            player.worldY + 80 >= checkpoint.worldY && player.worldY <= checkpoint.worldY + checkpoint.sizeY) {
            // Chuyển map
            nextMap();
        }
    }

    // Chuyển sang map tiếp theo
    private void nextMap() {
        currentMap++;
        if (currentMap >= mapPaths.length) {
            // Nếu hết map, thắng game
            gameOver = true;
            return;
        }
        loadMap();
        // Reset enemies và checkpoint
        enemies.clear();
        checkpoint = null;
        spawnEnemies();
        // Có thể reset vị trí player nếu cần
        // player.worldX = 1000; player.worldY = 1000;
    }

    // Kiểm tra va chạm giữa đạn và thực thể
    private void checkCollisions() {
        // Player bullets hitting enemies
        Iterator<Bullet> playerBulletIter = player.bullets.iterator();
        while (playerBulletIter.hasNext()) {
            Bullet b = playerBulletIter.next();
            for (Enemy e : enemies) {
                if (b.worldX >= e.worldX && b.worldX <= e.worldX + 80 &&
                    b.worldY >= e.worldY && b.worldY <= e.worldY + 80) {
                    e.health -= 10; // Damage to enemy
                    playerBulletIter.remove();
                    break;
                }
            }
        }

        // Enemy bullets hitting player
        for (Enemy e : enemies) {
            Iterator<Bullet> enemyBulletIter = e.bullets.iterator();
            while (enemyBulletIter.hasNext()) {
                Bullet b = enemyBulletIter.next();
                if (b.worldX >= player.worldX && b.worldX <= player.worldX + 80 &&
                    b.worldY >= player.worldY && b.worldY <= player.worldY + 80) {
                    player.health -= 10; // Damage to player
                    enemyBulletIter.remove();
                    break;
                }
            }
        }

        // Remove dead enemies
        enemies.removeIf(e -> e.health <= 0);
    }

    // Vẽ tất cả các thành phần game
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Tính toán vị trí camera dựa trên vị trí nhân vật
        int cameraX = player.worldX  - (screenWidth / 2);
        int cameraY = player.worldY  - (screenHeight / 2);

        // Giới hạn camera không được nhìn thấy ngoài phạm vi map
        clampCameraPosition(cameraX, cameraY);
        
        int mapScreenX = -cameraX;
        int screenY = -cameraY;

        g2.drawImage(mapImage, mapScreenX, screenY, worldWidth, worldHeight, null);

        if (!gameOver) {
            // Gọi hàm draw của nhân vật
            player.draw(g2);

            // Draw enemies
            for (Enemy enemy : enemies) {
                enemy.draw(g2);
            }

            // Draw checkpoint
            if (checkpoint != null) {
                checkpoint.draw(g2, player.worldX, player.worldY);
            }
        } else {
            // Draw game over screen
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, screenWidth, screenHeight);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 48));
            FontMetrics fm = g2.getFontMetrics();
            String text = currentMap >= mapPaths.length ? "YOU WIN!" : "GAME OVER";
            int x = (screenWidth - fm.stringWidth(text)) / 2;
            int y = screenHeight / 2;
            g2.drawString(text, x, y);
        }

        g2.dispose();
    }

    // Giới hạn camera không được nhìn thấy ngoài phạm vi map
    private int[] clampCameraPosition(int cameraX, int cameraY) {
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
}
