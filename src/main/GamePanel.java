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

    Checkpoint checkpoint = null;

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

    // Quản lý âm thanh dùng chung cho toàn game
    public Sound sound = new Sound();

    // Theo dõi chuột có đang dí vào nút dừng không (để phát tiếng đúng 1 lần khi vừa chạm)
    private boolean pauseBtnHovered = false;

    // --- CÀI ĐẶT ÂM THANH (nút bánh răng góc phải + bảng chỉnh âm lượng) ---
    private Rectangle settingsButtonRect;
    private boolean showSettings = false;
    private Rectangle musicSliderTrack;
    private Rectangle seSliderTrack;
    private Rectangle muteToggleRect;
    private Rectangle settingsCloseRect;
    private int settingsPanelX, settingsPanelY, settingsPanelW, settingsPanelH;

    // --- HIỆU ỨNG KHI DÍNH ĐẠN (rung màn hình + viền đỏ) ---
    public int hurtFlashTimer = 0; // số frame còn lại để vẽ viền đỏ
    public int shakeTimer = 0;     // số frame còn lại để rung màn hình
    private static final int HURT_DURATION = 24;
    private final java.util.Random fxRandom = new java.util.Random();

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        
        pauseButtonRect = new Rectangle(screenWidth - 60, 10, 50, 50);
        resumeButtonRect = new Rectangle(screenWidth / 2 - 100, screenHeight / 2 - 100, 200, 50);
        helpButtonRect = new Rectangle(screenWidth / 2 - 100, screenHeight / 2 - 30, 200, 50);
        exitButtonRect = new Rectangle(screenWidth / 2 - 100, screenHeight / 2 + 40, 200, 50);
        backButtonRect = new Rectangle(screenWidth / 2 - 100, screenHeight / 2 + 150, 200, 50);

        // Nút bánh răng đặt ngay bên trái nút Dừng ở góc trên phải
        settingsButtonRect = new Rectangle(screenWidth - 120, 10, 50, 50);

        // Bảng cài đặt âm thanh (canh giữa màn hình)
        settingsPanelW = 380;
        settingsPanelH = 250;
        settingsPanelX = (screenWidth - settingsPanelW) / 2;
        settingsPanelY = (screenHeight - settingsPanelH) / 2;
        int trackX = settingsPanelX + 130;
        int trackW = 200;
        musicSliderTrack = new Rectangle(trackX, settingsPanelY + 80, trackW, 10);
        seSliderTrack = new Rectangle(trackX, settingsPanelY + 130, trackW, 10);
        muteToggleRect = new Rectangle(settingsPanelX + 130, settingsPanelY + 165, 140, 32);
        settingsCloseRect = new Rectangle(settingsPanelX + settingsPanelW - 110, settingsPanelY + settingsPanelH - 45, 90, 32);

        // Initialize state management - start with MenuState
        currentState = new MenuState(this);
        currentState.enter();

        // Bật nhạc nền menu ngay khi mở game
        sound.playMusic("nhac_nen_mainmenu");

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
        // Giảm dần các bộ đếm hiệu ứng dính đạn (kể cả khi dừng để hiệu ứng tự tắt)
        if (hurtFlashTimer > 0) hurtFlashTimer--;
        if (shakeTimer > 0) shakeTimer--;

        if (!isPaused && !showSettings) {
            currentState.update();
            checkCollisions();
        }
    }

    /** Kích hoạt hiệu ứng khi nhân vật dính đạn: rung màn hình + viền đỏ + tiếng "hự". */
    public void triggerHurtEffect() {
        hurtFlashTimer = HURT_DURATION;
        shakeTimer = 12;
        sound.playSE("dinh_dan");
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

        // Rung màn hình khi dính đạn: lệch nhẹ vị trí vẽ một cách ngẫu nhiên, giảm dần
        int shakeX = 0, shakeY = 0;
        if (shakeTimer > 0) {
            int mag = Math.max(1, shakeTimer / 2);
            shakeX = fxRandom.nextInt(mag * 2 + 1) - mag;
            shakeY = fxRandom.nextInt(mag * 2 + 1) - mag;
        }

        g2.translate(xOffset + shakeX, yOffset + shakeY);
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
        
        // --- VIỀN ĐỎ KHI DÍNH ĐẠN ---
        if (hurtFlashTimer > 0) {
            drawHurtVignette(g2, hurtFlashTimer / (float) HURT_DURATION);
        }

        // --- NÚT LOA CÀI ĐẶT (hiện ở mọi màn, kể cả Menu) ---
        boolean gearVisible = !isPaused && !showSettings && !(currentState instanceof InstructionsState);
        if (gearVisible) {
            drawSpeakerButton(g2, settingsButtonRect);
        }

        // --- BẢNG CÀI ĐẶT ÂM THANH ---
        if (showSettings) {
            drawSettingsPanel(g2);
        }

        // KHÔNG dispose Graphics do Swing cấp — đó là lỗi, dispose sẽ làm hỏng các vẽ
        // tiếp theo
    }

    // Vẽ viền đỏ mờ dần từ rìa màn hình vào trong (hiệu ứng trúng đạn)
    private void drawHurtVignette(Graphics2D g2, float intensity) {
        int bands = 40;
        int maxAlpha = (int) (170 * intensity);
        for (int i = 0; i < bands; i++) {
            int a = (int) (maxAlpha * (1 - (float) i / bands));
            if (a <= 0) continue;
            g2.setColor(new Color(200, 0, 0, a));
            g2.drawRect(i, i, screenWidth - 1 - i * 2, screenHeight - 1 - i * 2);
        }
    }

    // Vẽ biểu tượng cái loa (kèm sóng âm)
    private void drawSpeakerButton(Graphics2D g2, Rectangle r) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        int cx = r.x + r.width / 2;
        int cy = r.y + r.height / 2;

        g2.setColor(Color.WHITE);
        // Thân loa
        g2.fillRect(cx - 14, cy - 5, 7, 10);
        // Nón loa (hình thang mở rộng sang phải)
        Polygon cone = new Polygon();
        cone.addPoint(cx - 7, cy - 5);
        cone.addPoint(cx - 7, cy + 5);
        cone.addPoint(cx + 1, cy + 12);
        cone.addPoint(cx + 1, cy - 12);
        g2.fillPolygon(cone);
        // Sóng âm phát ra
        g2.setStroke(new BasicStroke(2));
        g2.drawArc(cx + 1, cy - 9, 9, 18, -60, 120);
        g2.drawArc(cx + 1, cy - 14, 17, 28, -55, 110);
    }

    // Vẽ bảng cài đặt âm thanh với 2 thanh trượt + nút tắt tiếng + nút đóng
    private void drawSettingsPanel(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(new Color(30, 30, 40));
        g2.fillRoundRect(settingsPanelX, settingsPanelY, settingsPanelW, settingsPanelH, 20, 20);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(settingsPanelX, settingsPanelY, settingsPanelW, settingsPanelH, 20, 20);

        // Tiêu đề
        g2.setFont(new Font("Arial", Font.BOLD, 26));
        FontMetrics fm = g2.getFontMetrics();
        String title = "ÂM THANH";
        g2.drawString(title, settingsPanelX + (settingsPanelW - fm.stringWidth(title)) / 2, settingsPanelY + 45);

        // Nhãn + 2 thanh trượt
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.setColor(Color.WHITE);
        g2.drawString("Nhạc nền", settingsPanelX + 30, musicSliderTrack.y + 12);
        drawSlider(g2, musicSliderTrack, sound.getMusicVolume());
        g2.setColor(Color.WHITE);
        g2.drawString("Hiệu ứng", settingsPanelX + 30, seSliderTrack.y + 12);
        drawSlider(g2, seSliderTrack, sound.getSeVolume());

        // Nút tắt/bật tiếng và nút đóng
        drawButton(g2, muteToggleRect, sound.isMuted() ? "Bật tiếng" : "Tắt tiếng");
        drawButton(g2, settingsCloseRect, "Đóng");
    }

    private void drawSlider(Graphics2D g2, Rectangle track, float value) {
        g2.setColor(new Color(80, 80, 90));
        g2.fillRoundRect(track.x, track.y, track.width, track.height, 6, 6);
        int fill = (int) (track.width * value);
        g2.setColor(new Color(80, 200, 120));
        g2.fillRoundRect(track.x, track.y, fill, track.height, 6, 6);
        int knobX = track.x + fill;
        g2.setColor(Color.WHITE);
        g2.fillOval(knobX - 8, track.y + track.height / 2 - 9, 18, 18);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString((int) (value * 100) + "%", track.x + track.width + 14, track.y + 11);
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

    // Sinh checkpoint tại vị trí giữa map
    public void spawnCheckpoint() {
        checkpoint = new Checkpoint(this, worldWidth / 2 - 40, worldHeight / 2 - 40);
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

        // Bảng cài đặt đang mở -> ưu tiên xử lý trong bảng
        if (showSettings) {
            handleSettingsClick(p);
            return;
        }

        // Nút bánh răng (hiện ở mọi màn trừ khi đang Dừng / màn Hướng dẫn)
        boolean gearVisible = !isPaused && !(currentState instanceof InstructionsState);
        if (gearVisible && settingsButtonRect.contains(p)) {
            showSettings = true;
            sound.playSE("click");
            return;
        }

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

    // Xử lý click bên trong bảng cài đặt âm thanh
    private void handleSettingsClick(Point p) {
        if (settingsCloseRect.contains(p)) {
            showSettings = false;
            return;
        }
        if (muteToggleRect.contains(p)) {
            sound.toggleMute();
            return;
        }
        if (nearTrack(musicSliderTrack, p)) {
            setSliderFromPoint(musicSliderTrack, p, true, false);
            return;
        }
        if (nearTrack(seSliderTrack, p)) {
            setSliderFromPoint(seSliderTrack, p, false, true);
            return;
        }
        // Click ra ngoài bảng -> đóng
        Rectangle panel = new Rectangle(settingsPanelX, settingsPanelY, settingsPanelW, settingsPanelH);
        if (!panel.contains(p)) {
            showSettings = false;
        }
    }

    // Vùng bấm rộng hơn thanh trượt một chút cho dễ trúng
    private boolean nearTrack(Rectangle t, Point p) {
        return p.x >= t.x - 12 && p.x <= t.x + t.width + 12
                && p.y >= t.y - 14 && p.y <= t.y + t.height + 14;
    }

    // Đặt âm lượng theo vị trí chuột trên thanh trượt
    private void setSliderFromPoint(Rectangle track, Point p, boolean music, boolean preview) {
        float v = (p.x - track.x) / (float) track.width;
        if (v < 0f) v = 0f;
        if (v > 1f) v = 1f;
        if (music) {
            sound.setMusicVolume(v);
        } else {
            sound.setSeVolume(v);
            if (preview) sound.playSE("hit_enemy"); // nghe thử mức âm lượng SE
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        MouseEvent translated = translateMouseEvent(e);
        Point p = translated.getPoint();
        if (showSettings) {
            if (nearTrack(musicSliderTrack, p)) setSliderFromPoint(musicSliderTrack, p, true, false);
            else if (nearTrack(seSliderTrack, p)) setSliderFromPoint(seSliderTrack, p, false, false);
            return;
        }

        // Không truyền sự kiện nhấn xuống game khi bấm vào nút loa / nút Dừng
        // (tránh bị bắn/ngắm một phát khi chỉ muốn mở cài đặt hoặc tạm dừng)
        boolean gearVisible = !isPaused && !(currentState instanceof InstructionsState);
        if (gearVisible && settingsButtonRect.contains(p)) return;
        boolean pauseVisible = !(currentState instanceof MenuState)
                && !(currentState instanceof InstructionsState) && !isPaused;
        if (pauseVisible && pauseButtonRect.contains(p)) return;

        mouseH.mousePressed(translated);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        MouseEvent translated = translateMouseEvent(e);
        if (showSettings) return;
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
        if (showSettings) {
            Point p = translated.getPoint();
            if (nearTrack(musicSliderTrack, p)) setSliderFromPoint(musicSliderTrack, p, true, false);
            else if (nearTrack(seSliderTrack, p)) setSliderFromPoint(seSliderTrack, p, false, false);
            return;
        }
        mouseH.mouseDragged(translated);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        MouseEvent translated = translateMouseEvent(e);

        // Phát tiếng đúng 1 lần khi chuột vừa dí vào nút dừng (chỉ khi nút đang hiển thị)
        boolean pauseVisible = !(currentState instanceof MenuState)
                && !(currentState instanceof InstructionsState) && !isPaused;
        boolean nowHover = pauseVisible && pauseButtonRect.contains(translated.getPoint());
        if (nowHover && !pauseBtnHovered) {
            sound.playSE("click");
        }
        pauseBtnHovered = nowHover;

        mouseH.mouseMoved(translated);
    }

    public GameState getCurrentState() {
        return currentState;
    }
}
