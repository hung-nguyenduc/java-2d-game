package main;

import collision.CollisionChecker;
import entity.Player;
import entity.Enemy;
import entity.Checkpoint;
import state.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements Runnable, MouseListener {
    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize = originalTileSize * scale;
    final int maxScreenCol = 16;
    final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    // World dimensions
    public int worldWidth = 2000;
    public int worldHeight = 2000;

    // Enemy management
    public List<Enemy> enemies = new ArrayList<>();

    public KeyHandler keyH = new KeyHandler();
    public MouseHandler mouseH = new MouseHandler();
    Thread gameThread;
    public Player player = new Player(this, keyH, mouseH);
    CollisionChecker cChecker = new CollisionChecker(this);

    Checkpoint checkpoint = null;

    // Game over flag
    public boolean gameOver = false;

    // Kill counter (reset mỗi level)
    public int killCount = 0;

    // Game state management
    private GameState currentState;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);

        // Initialize state management - start with MenuState
        currentState = new ZombieState(this);
        currentState.enter();

        // Add mouse listener for button clicks
        this.addMouseListener(this);
        // Theo dõi vị trí chuột để ngắm bắn
        this.addMouseMotionListener(mouseH);
        // Xử lý sự kiện click chuột (bắn)
        this.addMouseListener(mouseH);

        this.setFocusable(true);
        this.requestFocusInWindow();
        this.setFocusTraversalKeysEnabled(false);
        this.addKeyListener(keyH);

        // Global dispatcher: bắt key events dù focus ở bất cứ đâu trong JVM
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED)
                keyH.keyPressed(e);
            else if (e.getID() == KeyEvent.KEY_RELEASED)
                keyH.keyReleased(e);
            return false;
        });

        // Spawn checkpoint at map center
        // spawnCheckpoint();
    }

    // Khởi động luồng game
    public void startGameThread() {
        // Trick Windows: 1 daemon thread sleep mãi → buộc OS giữ timer resolution ở 1ms
        // (mặc định Windows ~15.6ms khiến Thread.sleep ms-level cực kỳ kém chính xác →
        // giật frame)
        Thread timerHack = new Thread(() -> {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ignored) {
            }
        }, "WindowsTimerHack");
        timerHack.setDaemon(true);
        timerHack.start();

        gameThread = new Thread(this);
        gameThread.start();
        requestFocusInWindow();
    }

    // Vòng lặp game chính (chạy ở 60 FPS)
    @Override
    public void run() {
        final double drawInterval = 1_000_000_000.0 / 60; // nanosecond mỗi frame
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            update();

            // Vẽ ĐỒNG BỘ trên EDT: chặn game thread tới khi paint xong → không có race
            // condition giữa update() và paintComponent() (state đọc giữa chừng), và biết
            // chính
            // xác lúc nào sync() flush sẽ có hiệu lực
            try {
                SwingUtilities.invokeAndWait(() -> {
                    if (isShowing())
                        paintImmediately(0, 0, getWidth(), getHeight());
                });
            } catch (InterruptedException e) {
                break;
            } catch (java.lang.reflect.InvocationTargetException e) {
                e.printStackTrace();
            }
            // Sau khi EDT đã vẽ xong, flush GDI/back-buffer xuống màn hình → giảm tearing
            Toolkit.getDefaultToolkit().sync();

            // Sleep đúng phần thời gian còn lại đến frame kế tiếp (không busy-wait, không
            // drift)
            long remainingNs = (long) (nextDrawTime - System.nanoTime());
            if (remainingNs > 0) {
                try {
                    Thread.sleep(remainingNs / 1_000_000L, (int) (remainingNs % 1_000_000L));
                } catch (InterruptedException e) {
                    break;
                }
            }
            nextDrawTime += drawInterval;

            // Nếu bị tụt quá xa (GC pause, OS treo) → reset để tránh catch-up dồn dập
            long now = System.nanoTime();
            if (now > nextDrawTime + drawInterval) {
                nextDrawTime = now + drawInterval;
            }
        }
    }

    // Cập nhật trạng thái game mỗi frame
    public void update() {
        currentState.update();
        checkCollisions();
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
        // KHÔNG dispose Graphics do Swing cấp — đó là lỗi, dispose sẽ làm hỏng các vẽ
        // tiếp theo
    }

    // Giới hạn camera không được nhìn thấy ngoài phạm vi map
    // Clamp thẳng — không có dead zone, tránh camera "nhảy" hàng chục pixel khi tới
    // rìa map
    public int[] clampCameraPosition(int cameraX, int cameraY) {
        if (cameraX < 0)
            cameraX = 0;
        if (cameraX > worldWidth - screenWidth)
            cameraX = worldWidth - screenWidth;
        if (cameraY < 0)
            cameraY = 0;
        if (cameraY > worldHeight - screenHeight)
            cameraY = worldHeight - screenHeight;
        return new int[] { cameraX, cameraY };
    }

    // Sinh checkpoint tại vị trí giữa map
    public void spawnCheckpoint() {
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

    // Trong GamePanel.java
    public void checkCollisions() {
        cChecker.checkAllCollisions();
    }

    // MouseListener methods
    @Override
    public void mouseClicked(MouseEvent e) {
        currentState.handleMouseClick(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public GameState getCurrentState() {
        return currentState;
    }
}
