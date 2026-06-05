package state;

import Dialogue.DialogueLine;
import Dialogue.DialogueManager;
import collision.CollisionChecker;
import entity.Enemy;
import entity.Weapon;
import entity.Bullet;
import main.MouseHandler;
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
                    gp.setState(new LevelCompleteState(gp, 1, new Level2State(gp)));
                }
            }
        };
    }

    @Override
    public void enter() {
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

        gp.player.setDefaultValues();
        gp.player.spawnAtCenter();
        gp.player.health = gp.player.maxHealth;

        this.weapon = new Weapon(gp, gp.mouseH, gp.player);
        this.weapon.automaticFire = true; // Bật chế độ sấy
        gp.player.equipWeapon(this.weapon);
        weapon.clearBullets();

        // Tải vật cản từ hằng số path đã định nghĩa
        this.obstacles = ObstacleManager.loadObstacles(OBSTACLE_TXT_PATH, this.scale);



        spawnEnemies();

        // Nạp ảnh chân dung nhân vật Vũ cho hội thoại
        try {
            vuFace = ImageIO.read(getClass().getResourceAsStream("/player/down1.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Kịch bản hội thoại mở đầu và kết thúc
        introScript = new DialogueLine[] {
                new DialogueLine("Cảnh báo! Khu vực KTX đã bị thế lực Zombie tàn phá!", null),
                new DialogueLine("Vũ: Chuyện gì thế này... Mình phải dọn sạch lũ Zombie xung quanh \nđể tìm đường máu thoát ra ngoài thôi!", vuFace)
        };

        afterQuestScript = new DialogueLine[] {
                new DialogueLine("Vũ: Phù... Tạm thời khu vực này đã an toàn. \nPhải di chuyển tiếp sang khu nhà bên cạnh thôi!", vuFace)
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
            gp.setState(new LevelCompleteState(gp, 1, new Level2State(gp)));
            return;
        }

        // Nếu hộp thoại đang mở, chặn mọi tương tác di chuyển/bắn súng, chỉ cho bấm Space tua chữ
        if (dialogueBox.isActive()) {
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

        // Kiểm tra điều kiện Thua cuộc (Vũ hết máu)
        if (gp.player.health <= 0) {
            gp.setState(new GameOverState(gp));
            return;
        }

        // Kiểm tra điều kiện Hoàn thành (Diệt đủ 8 quái)
        if (gp.killCount >= 8 && !isQuestCompleted) {
            isQuestCompleted = true;
        }

        // Kích hoạt hội thoại kết màn ngay sau khi diệt đủ số lượng quái yêu cầu
        if (isQuestCompleted && !isPhase2DialoguePlayed) {
            dialogueBox.startDialogue(afterQuestScript);
            isPhase2DialoguePlayed = true;
        }
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

        // 6. Vẽ Giao diện HUD
        g2.setColor(Color.RED);
        g2.setFont(HUD_FONT);
        g2.drawString("Man 1 - Giet quai: " + gp.killCount + " / 8", 10, 30);

        // 7. Vẽ Hộp thoại
        dialogueBox.draw(g2, gp.screenWidth, gp.screenHeight);
    }

    private void spawnEnemies() {
        Enemy e1 = new Enemy(gp, gp.player, 300, 300, 0);
        e1.speed = 1.5; e1.canDodge = false; e1.damage = 1; e1.minDistance = 50;
        gp.enemies.add(e1);

        Enemy e2 = new Enemy(gp, gp.player, 800, 500, 1);
        e2.speed = 1.5; e2.canDodge = false; e2.damage = 1; e2.minDistance = 50;
        gp.enemies.add(e2);

        Enemy e3 = new Enemy(gp, gp.player, 1200, 700, 2);
        e3.speed = 1.5; e3.canDodge = false; e3.damage = 1; e3.minDistance = 50;
        gp.enemies.add(e3);

        Enemy e4 = new Enemy(gp, gp.player, 500, 500, 3); // Thêm 1 quái bỏ chạy
        e4.speed = 2.5; e4.canDodge = false; e4.damage = 1; e4.minDistance = 50;
        gp.enemies.add(e4);
        
        // Gấp đôi số lượng quái
        Enemy e5 = new Enemy(gp, gp.player, 400, 400, 0);
        e5.speed = 1.5; e5.canDodge = false; e5.damage = 1; e5.minDistance = 50;
        gp.enemies.add(e5);

        Enemy e6 = new Enemy(gp, gp.player, 900, 600, 1);
        e6.speed = 1.5; e6.canDodge = false; e6.damage = 1; e6.minDistance = 50;
        gp.enemies.add(e6);

        Enemy e7 = new Enemy(gp, gp.player, 1300, 800, 2);
        e7.speed = 1.5; e7.canDodge = false; e7.damage = 1; e7.minDistance = 50;
        gp.enemies.add(e7);

        Enemy e8 = new Enemy(gp, gp.player, 600, 600, 3); 
        e8.speed = 2.5; e8.canDodge = false; e8.damage = 1; e8.minDistance = 50;
        gp.enemies.add(e8);
    }

    @Override
    public void handleMouseClick(MouseEvent e) {}
}