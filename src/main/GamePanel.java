package main;

import entity.Player; // Nhớ import package entity
import entity.Enemy; // Import Enemy class
import entity.Bullet; // Import Bullet class
import entity.Checkpoint; // Import Checkpoint class

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

// Lớp chính
//quản lý panel game, vòng lặp game, và rendering
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
    MouseHandler mouseH = new MouseHandler();
    Thread gameThread;
    Player player = new Player(this, keyH, mouseH); // Truyền Panel, Bàn phím, Chuột cho Player

    // Checkpoint
    Checkpoint checkpoint = null;

    // Game over flag
    public boolean gameOver = false;

    // Kill counter (reset mỗi level)
    public int killCount = 0;

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
        // Theo dõi vị trí chuột để ngắm bắn
        this.addMouseMotionListener(mouseH);

        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false); // Tắt Tab/Shift-Tab cướp focus
        this.addKeyListener(keyH);
        // Global dispatcher: bắt key events dù focus ở bất cứ đâu trong JVM
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED)  keyH.keyPressed(e);
            else if (e.getID() == KeyEvent.KEY_RELEASED) keyH.keyReleased(e);
            return false;
        });

        // Spawn checkpoint at map center
        spawnCheckpoint();
    }

    // Khởi động luồng game
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
        requestFocusInWindow();
    }

    // Vòng lặp game chính (chạy ở 60 FPS)
    @Override
    public void run() {
        final long drawInterval = 1_000_000_000L / 60;
        long lastTime = System.nanoTime();

        while (gameThread != null) {
            long now = System.nanoTime();
            if (now - lastTime >= drawInterval) {
                lastTime += drawInterval;
                // Tránh vòng lặp catch-up khi bị chậm quá nhiều
                if (lastTime < now - drawInterval) lastTime = now;
                update();
                repaint();
            } else {
                // Sleep 1ms — vừa nhường CPU cho EDT (xử lý phím), vừa giữ Windows timer ở 1ms
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    break;
                }
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
        // Render hint cho text mượt, không cần đặt mỗi state
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        currentState.draw(g2);
        // KHÔNG dispose Graphics do Swing cấp — đó là lỗi, dispose sẽ làm hỏng các vẽ tiếp theo
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
        // Lấy lại focus bàn phím sau mỗi lần chuyển state
        requestFocusInWindow();
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

        // Remove dead enemies and count kills
        for (int i = 0; i < enemies.size(); i++) {
            if (enemies.get(i).health <= 0) {
                enemies.remove(i);
                killCount++;
                i--;
            }
        }
    }

}
