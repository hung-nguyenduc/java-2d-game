package main;

import collision.CollisionChecker;
import entity.Player;
import entity.Enemy;
import state.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements Runnable, MouseListener, java.awt.event.MouseMotionListener {
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


    // Game over flag
    public boolean gameOver = false;
    public boolean isPaused = false;
    private boolean showInstructions = false;
    
    // Nút Menu Tạm dừng
    private Rectangle pauseButtonRect;
    private Rectangle resumeButtonRect;
    private Rectangle exitButtonRect;
    private Rectangle helpButtonRect;
    private Rectangle backButtonRect;

    // Kill counter (reset mỗi level)
    public int killCount = 0;

    // Game state management
    private GameState currentState;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        
        pauseButtonRect = new Rectangle(screenWidth - 60, 10, 50, 50);
        resumeButtonRect = new Rectangle(screenWidth / 2 - 100, screenHeight / 2 - 100, 200, 50);
        helpButtonRect = new Rectangle(screenWidth / 2 - 100, screenHeight / 2 - 30, 200, 50);
        exitButtonRect = new Rectangle(screenWidth / 2 - 100, screenHeight / 2 + 40, 200, 50);
        backButtonRect = new Rectangle(screenWidth / 2 - 100, screenHeight / 2 + 150, 200, 50);

        // Initialize state management - start with MenuState
        currentState = new MenuState(this);
        currentState.enter();

        // Thêm listener cho chuột qua GanmePanel để có thể xử lý tọa độ khi scale
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

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
        if (!isPaused) {
            currentState.update();
            checkCollisions();
        }
    }

    public double getScaleRatio() {
        return Math.min((double) getWidth() / screenWidth, (double) getHeight() / screenHeight);
    }

    public int getXOffset() {
        return (int) ((getWidth() - screenWidth * getScaleRatio()) / 2);
    }

    public int getYOffset() {
        return (int) ((getHeight() - screenHeight * getScaleRatio()) / 2);
    }

    public MouseEvent translateMouseEvent(MouseEvent e) {
        double scaleRatio = getScaleRatio();
        int newX = (int) ((e.getX() - getXOffset()) / scaleRatio);
        int newY = (int) ((e.getY() - getYOffset()) / scaleRatio);
        return new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(), newX, newY,
                e.getClickCount(), e.isPopupTrigger(), e.getButton());
    }

    // Vẽ tất cả các thành phần game
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Scale màn hình
        double scaleRatio = getScaleRatio();
        int xOffset = getXOffset();
        int yOffset = getYOffset();

        g2.translate(xOffset, yOffset);
        g2.scale(scaleRatio, scaleRatio);

        // Cắt bớt phần bên ngoài để tránh rác (nếu có)
        g2.setClip(0, 0, screenWidth, screenHeight);

        // Render hint cho text mượt, không cần đặt mỗi state
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        currentState.draw(g2);
        
        // --- DRAW PAUSE BUTTON & MENU ---
        if (!(currentState instanceof MenuState) && !(currentState instanceof InstructionsState)) {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRoundRect(pauseButtonRect.x, pauseButtonRect.y, pauseButtonRect.width, pauseButtonRect.height, 10, 10);
            g2.setColor(Color.WHITE);
            g2.fillRect(pauseButtonRect.x + 15, pauseButtonRect.y + 12, 6, 26);
            g2.fillRect(pauseButtonRect.x + 29, pauseButtonRect.y + 12, 6, 26);
        }

        if (isPaused) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, screenWidth, screenHeight);
            
            if (showInstructions) {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 36));
                g2.drawString("Hướng dẫn", 280, 100);

                g2.setFont(new Font("Arial", Font.PLAIN, 24));
                g2.drawString("Sử dụng các phím mũi tên để di chuyển:", 150, 200);
                g2.drawString("↑: Lên", 200, 250);
                g2.drawString("↓: Xuống", 200, 280);
                g2.drawString("←: Trái", 200, 310);
                g2.drawString("→: Phải", 200, 340);
                g2.drawString("Click chuột để bắn (Tùy màn bắn sấy hoặc phát một)", 150, 390);
                g2.drawString("K: Đá lùi quái | G: Ném lựu đạn", 150, 420);
                
                drawButton(g2, backButtonRect, "Quay lại");
            } else {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 40));
                g2.drawString("PAUSED", screenWidth / 2 - 80, screenHeight / 2 - 150);
                
                drawButton(g2, resumeButtonRect, "Tiếp tục");
                drawButton(g2, helpButtonRect, "Hướng dẫn chơi");
                drawButton(g2, exitButtonRect, "Thoát (Menu)");
            }
        }
        
        // KHÔNG dispose Graphics do Swing cấp — đó là lỗi, dispose sẽ làm hỏng các vẽ
        // tiếp theo
    }
    
    private void drawButton(Graphics2D g2, Rectangle rect, String text) {
        g2.setColor(Color.GRAY);
        g2.fillRect(rect.x, rect.y, rect.width, rect.height);
        g2.setColor(Color.BLACK);
        g2.drawRect(rect.x, rect.y, rect.width, rect.height);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g2.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + (rect.height + fm.getAscent()) / 2 - fm.getDescent() + 2;
        g2.drawString(text, textX, textY);
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


    // Set a new game state
    public void setState(GameState newState) {
        if (currentState != null) {
            currentState.exit();
        }
        newState.enter(); // Gọi enter() trước để khởi tạo dữ liệu an toàn
        currentState = newState; // Sau đó mới gán để EDT paintComponent() không dính NPE
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
        MouseEvent translated = translateMouseEvent(e);
        Point p = translated.getPoint();

        if (!(currentState instanceof MenuState) && !(currentState instanceof InstructionsState)) {
            if (!isPaused && pauseButtonRect.contains(p)) {
                isPaused = true;
                return;
            }
        }
        
        if (isPaused) {
            if (showInstructions) {
                if (backButtonRect.contains(p)) {
                    showInstructions = false;
                }
            } else {
                if (resumeButtonRect.contains(p)) {
                    isPaused = false;
                } else if (helpButtonRect.contains(p)) {
                    showInstructions = true;
                } else if (exitButtonRect.contains(p)) {
                    isPaused = false;
                    showInstructions = false;
                    setState(new MenuState(this));
                }
            }
            return;
        }

        currentState.handleMouseClick(translated);
        mouseH.mouseClicked(translated);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        MouseEvent translated = translateMouseEvent(e);
        mouseH.mousePressed(translated);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        MouseEvent translated = translateMouseEvent(e);
        mouseH.mouseReleased(translated);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        MouseEvent translated = translateMouseEvent(e);
        mouseH.mouseEntered(translated);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        MouseEvent translated = translateMouseEvent(e);
        mouseH.mouseExited(translated);
    }

    // MouseMotionListener methods
    @Override
    public void mouseDragged(MouseEvent e) {
        MouseEvent translated = translateMouseEvent(e);
        mouseH.mouseDragged(translated);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        MouseEvent translated = translateMouseEvent(e);
        mouseH.mouseMoved(translated);
    }

    public GameState getCurrentState() {
        return currentState;
    }
}
