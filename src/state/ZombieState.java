package state;

import Dialogue.DialogueLine;
import Dialogue.DialogueManager;
import entity.Enemy;
import entity.Weapon;
import main.*;
import collision.Obstacle;
import collision.ObstacleManager;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.ArrayList;

public class ZombieState extends GameState {
    private boolean debugMode = false;
    private static final String MAP_IMAGE_PATH = "/maps/destroyed-c1.png";
    private static final String OBSTACLE_TXT_PATH = "/maps/destroyed-c1.txt";
    private static final Font HUD_FONT = new Font("Arial", Font.BOLD, 20);

    private DialogueManager dialogueBox;
    private BufferedImage mapImage;
    private List<Obstacle> obstacles;
    private Weapon weapon;
    double scale;
    BufferedImage vuFace;

    // Quản lý trạng thái nhiệm vụ và hội thoại giống các map trước
    private boolean isQuestCompleted = false;
    private boolean isPhase2DialoguePlayed = false;
    private DialogueLine[] introScript;
    private DialogueLine[] afterQuestScript;

    // --- HIỆU ỨNG CHUỖI HẠ GỤC (First Blood / Double Kill / Triple Kill) ---
    private String killText = null;
    private int killTextTimer = 0;
    private static final int KILL_TEXT_DURATION = 110; // ~1.8s ở 60FPS
    private int lastAnnouncedKill = 0;
    private boolean pendingWinDialogue = false;
    private static Font killFont;
    private static boolean killFontLoaded = false;

    public ZombieState(GamePanel gp) {
        super(gp);
        this.obstacles = new ArrayList<>();
        this.weapon = new Weapon(gp, gp.mouseH, gp.player);

        // Khởi tạo hộp thoại đối thoại
        dialogueBox = new DialogueManager() {
            @Override
            public void onDialogueComplete() {
                if (isQuestCompleted) {
                    // Sau khi bắn hết quái và đọc xong hội thoại ăn mừng -> chuyển sang Level 2 qua LoadingState
                    gp.setState(new Ending(gp, new MenuState(gp)));
                }
            }
        };
    }

    @Override
    public void enter() {
        gp.sound.playMusic("nhac_nen_mainmenu"); // Nhạc nền nhẹ nhàng cho màn bắn quái
        // Pre-scale map một lần duy nhất
        try {
            BufferedImage src = ImageIO.read(getClass().getResourceAsStream(MAP_IMAGE_PATH));

            this.scale = 1.0 / 1.0;
            int mapWidth = (int) (src.getWidth() * scale);
            int mapHeight = (int) (src.getHeight() * scale);

            gp.worldWidth = mapWidth;
            gp.worldHeight = mapHeight;

            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            BufferedImage compat = gc.createCompatibleImage(mapWidth, mapHeight, Transparency.OPAQUE);
            Graphics2D mg = compat.createGraphics();

            mg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            mg.drawImage(src, 0, 0, mapWidth, mapHeight, null);
            mg.dispose();
            mapImage = compat;
        } catch (IOException e) {
            System.err.println("Lỗi nạp ảnh bản đồ tại: " + MAP_IMAGE_PATH);
            e.printStackTrace();
        }

        // Reset hệ thống màn chơi
        gp.enemies.clear();
        gp.killCount = 0;
        isQuestCompleted = false;
        isPhase2DialoguePlayed = false;
        lastAnnouncedKill = 0;
        killText = null;
        killTextTimer = 0;
        pendingWinDialogue = false;

        gp.player.setDefaultValues();
        gp.player.spawnAtCenter();
        gp.player.health = gp.player.maxHealth;

        this.weapon = new Weapon(gp, gp.mouseH, gp.player);
        this.weapon.automaticFire = true; // Bật chế độ sấy
        this.weapon.playShootSound = true; // Bật tiếng súng cho màn bắn quái
        gp.player.equipWeapon(this.weapon);
        weapon.clearBullets();

        // Tải vật cản từ hằng số path đã định nghĩa
        this.obstacles = ObstacleManager.loadObstacles(OBSTACLE_TXT_PATH, this.scale);

        // TÍNH NĂNG MỚI: Chỉ sinh con quái đầu tiên khi bắt đầu màn
        spawnSingleEnemy(0);

        // Nạp ảnh nhân vật Vũ
        try {
            vuFace = ImageIO.read(getClass().getResourceAsStream("/player/vuQuyetTam.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Kịch bản hội thoại mở đầu và kết thúc
        introScript = new DialogueLine[] {
                new DialogueLine("Giặc đến Bách Khoa rồi! Mình nhất định phải bảo vệ ngôi trường này.", vuFace),
                new DialogueLine("Vừa học môn bắn súng xong, áp dụng thực chiến luôn nào!", vuFace),
                new DialogueLine("Quyết tử cho Tổ quốc quyết sinh!", vuFace)
        };

        afterQuestScript = new DialogueLine[] {
                new DialogueLine("Mặt trận này đã sạch bóng quân thù!\nVì Bách Khoa thân yêu, ta quyết không lùi một bước!", vuFace)
        };

        // Kích hoạt chuỗi hội thoại đầu game
        dialogueBox.startDialogue(introScript);
    }

    @Override
    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    @Override
    public void exit() {
        gp.enemies.clear();
        obstacles.clear();
        if (weapon != null) {
            weapon.clearBullets();
        }
    }

    @Override
    public void update() {
        // Phím P để nhảy cấp nhanh
        if (gp.keyH.pPressed) {
            gp.keyH.pPressed = false;
            gp.setState(new Ending(gp, new MenuState(gp)));
            return;
        }

        // Giảm bộ đếm hiệu ứng chữ kill-streak (chạy kể cả khi đã hoàn thành màn)
        if (killTextTimer > 0) killTextTimer--;
        // Sau khi chữ "Triple Kill" chạy xong mới mở lời thoại kết màn
        if (pendingWinDialogue && killTextTimer <= 0 && !isPhase2DialoguePlayed && !dialogueBox.isActive()) {
            dialogueBox.startDialogue(afterQuestScript);
            isPhase2DialoguePlayed = true;
            pendingWinDialogue = false;
        }

        // Nếu hộp thoại đang mở, chặn mọi tương tác di chuyển/bắn súng, chỉ cho bấm Space tua chữ
        if (dialogueBox.isActive()) {
            dialogueBox.update();
            if (gp.keyH.spacePressed) {
                dialogueBox.advanceDialogue();
                gp.keyH.spacePressed = false;
            }
            return;
        }

        // Nếu đã thắng hoàn toàn và đọc xong đối thoại thì dừng update logic màn chơi
        if (isQuestCompleted) return;

        // Cập nhật người chơi và lũ Zombie
        gp.player.update();
        for (int i = 0; i < gp.enemies.size(); i++) {
            gp.enemies.get(i).update();
        }

        gp.checkCollisions();

        // Hiện chữ + đọc tên chuỗi hạ gục mỗi khi vừa diệt thêm một quái
        if (gp.killCount > lastAnnouncedKill && gp.killCount <= 3) {
            lastAnnouncedKill = gp.killCount;
            announceKill(gp.killCount);
        }

        // Kiểm tra điều kiện Thua cuộc (Vũ hết máu)
        if (gp.player.health <= 0) {
            gp.setState(new GameOverState(gp));
            return;
        }

        // Nếu trên map hết quái và chưa diệt đủ 3 con thì cho sinh con tiếp theo
        if (gp.enemies.isEmpty() && gp.killCount < 3) {
            spawnSingleEnemy(gp.killCount);
        }

        // Kiểm tra hoàn thành (Diệt đủ 3 quái); lời thoại kết màn mở sau khi chữ Triple Kill chạy xong
        if (gp.killCount >= 3 && !isQuestCompleted) {
            isQuestCompleted = true;
            pendingWinDialogue = true;
            // Đoạn logic chuyển Ending lập tức ở đây đã bị xóa, việc chuyển do onDialogueComplete() lo.
        }
    }

    // Hiện chữ kill-streak + phát giọng đọc tương ứng
    private void announceKill(int k) {
        switch (k) {
            case 1: killText = "First Blood"; gp.sound.playSE("first_blood"); break;
            case 2: killText = "Double Kill"; gp.sound.playSE("double_kill"); break;
            case 3: killText = "Triple Kill"; gp.sound.playSE("triple_kill"); break;
            default: return;
        }
        killTextTimer = KILL_TEXT_DURATION;
    }

    // Nạp font brush (res/fonts/killstreak.otf hoặc .ttf); thiếu thì dùng font script dự phòng
    private Font getKillFont(float size) {
        if (!killFontLoaded) {
            killFontLoaded = true;
            for (String path : new String[] {"/fonts/killstreak.otf", "/fonts/killstreak.ttf"}) {
                try (java.io.InputStream is = getClass().getResourceAsStream(path)) {
                    if (is != null) {
                        killFont = Font.createFont(Font.TRUETYPE_FONT, is);
                        break;
                    }
                } catch (Exception e) {
                    killFont = null;
                }
            }
        }
        if (killFont != null) return killFont.deriveFont(Font.BOLD, size);
        return new Font("Segoe Script", Font.BOLD | Font.ITALIC, (int) size);
    }

    // Vẽ chữ kill-streak: chữ trắng, viền đỏ (theo ảnh mẫu)
    private void drawKillStreak(Graphics2D g2, String text, int timer) {
        int elapsed = KILL_TEXT_DURATION - timer;
        float life = timer / (float) KILL_TEXT_DURATION;
        float alpha;
        if (elapsed < 8) alpha = elapsed / 8f;            // hiện nhanh
        else alpha = Math.min(1f, life / 0.4f);           // tan dần ở cuối
        if (alpha < 0f) alpha = 0f;
        if (alpha > 1f) alpha = 1f;

        float size = 78f;
        if (elapsed < 10) size *= 1.15f - 0.15f * (elapsed / 10f); // "pop" hơi to lúc đầu

        Font font = getKillFont(size);
        java.awt.font.FontRenderContext frc = g2.getFontRenderContext();
        java.awt.font.GlyphVector gv = font.createGlyphVector(frc, text);
        Rectangle bounds = gv.getPixelBounds(frc, 0, 0);
        int x = (gp.screenWidth - bounds.width) / 2 - bounds.x;
        int y = (int) (gp.screenHeight * 0.30);
        java.awt.Shape shape = gv.getOutline(x, y);

        Composite oldComp = g2.getComposite();
        Object oldAA = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Quầng đỏ mờ bên ngoài
        g2.setColor(new Color(140, 0, 0));
        g2.setStroke(new BasicStroke(13, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(shape);
        // Viền đỏ rõ
        g2.setColor(new Color(225, 20, 20));
        g2.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(shape);
        // Tô trắng bên trong
        g2.setColor(Color.WHITE);
        g2.fill(shape);

        g2.setComposite(oldComp);
        if (oldAA != null) g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
    }

    @Override
    public void draw(Graphics2D g2) {
        int cameraX = (int) (gp.player.worldX - gp.screenWidth / 2.0);
        int cameraY = (int) (gp.player.worldY - gp.screenHeight / 2.0);
        int[] clamped = gp.clampCameraPosition(cameraX, cameraY);
        cameraX = clamped[0];
        cameraY = clamped[1];

        // 1. Vẽ Map nền
        if (mapImage != null) {
            g2.drawImage(mapImage, 0, 0, gp.screenWidth, gp.screenHeight, cameraX, cameraY, cameraX + gp.screenWidth, cameraY + gp.screenHeight, null);
        }

        // 2. VẼ CÁC VẬT CẢN & XỬ LÝ ĐIỀU KIỆN DEBUG MODE
        for (Obstacle obs : obstacles) {
            // Đầu tiên vẫn vẽ hình ảnh vật cản bình thường để chơi game
            obs.draw(g2, cameraX, cameraY);

            // Cải tiến: Nếu bật debugMode lên đầu class (= true), vẽ thêm khung viền màu đỏ đè lên
            if (debugMode) {
                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke(2)); // Độ dày viền khung debug
                // Tính tọa độ hiển thị trên màn hình dựa vào Camera
                int screenObsX = obs.worldX - cameraX;
                int screenObsY = obs.worldY - cameraY;
                g2.drawRect(screenObsX, screenObsY, obs.width, obs.height);
            }
        }

        // 3. Vẽ Lũ quái vật Zombie
        for (Enemy enemy : gp.enemies) {
            enemy.draw(g2, cameraX, cameraY);

            // Vẽ thêm khung đỏ cho cả quái vật luôn nếu muốn soi vị trí va chạm
            if (debugMode) {
                g2.setColor(Color.RED);
                g2.drawRect((int)enemy.worldX - cameraX, (int)enemy.worldY - cameraY, 48, 48);
            }
        }

        // 4. Vẽ Nhân vật Vũ
        gp.player.draw(g2, cameraX, cameraY);

        // 5. Vẽ Súng và Đạn
        int screenX = (int) (gp.player.worldX - cameraX);
        int screenY = (int) (gp.player.worldY - cameraY);
        if (weapon != null) {
            weapon.draw(g2, screenX, screenY, cameraX, cameraY);
        }

        // 6. Vẽ HUD
        String hudText = "Tiêu diệt kẻ địch: " + gp.killCount + " / 3";
        g2.setColor(Color.RED);
        g2.setFont(HUD_FONT);
        FontMetrics fm = g2.getFontMetrics();
        int hudWidth = fm.stringWidth(hudText) + 20;
        int hudHeight = fm.getHeight() + 10;

        // Vẽ khung nền trong suốt
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(10, 10, hudWidth, hudHeight, 10, 10);

        // Vẽ viền trắng
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(10, 10, hudWidth, hudHeight, 10, 10);

        // Vẽ chữ
        g2.setColor(Color.RED);
        g2.drawString(hudText, 20, 10 + fm.getAscent() + 5);

        // 7. Vẽ hiệu ứng chữ chuỗi hạ gục (First Blood / Double Kill / Triple Kill)
        if (killText != null && killTextTimer > 0) {
            drawKillStreak(g2, killText, killTextTimer);
        }

        // 8. Vẽ Hộp thoại
        dialogueBox.draw(g2, gp.screenWidth, gp.screenHeight);
    }

    // TÍNH NĂNG MỚI: Hàm sinh từng con quái dựa trên thứ tự (chỉ số killCount)
    private void spawnSingleEnemy(int index) {
        Enemy e = null;
        switch (index) {
            case 0: e = new Enemy(gp, gp.player, 900, 300); e.speed = 1.5; break;
            case 1: e = new Enemy(gp, gp.player, 800, 1200); e.speed = 1.5; break;
            case 2: e = new Enemy(gp, gp.player, 1200, 700); e.speed = 2.5; break;
            case 3: e = new Enemy(gp, gp.player, 400, 900); e.speed = 2.0; break;
            case 4: e = new Enemy(gp, gp.player, 1400, 1100); e.speed = 2.5; break;
        }

        if (e != null) {
            e.canDodge = true;
            e.damage = 5;
            e.minDistance = 150;
            e.maxHealth *= 5;
            e.health = e.maxHealth;
            gp.enemies.add(e);
        }
    }

    @Override
    public void handleMouseClick(MouseEvent e) {}
}