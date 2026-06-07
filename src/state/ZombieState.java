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

    // Quản lý trạng thái nhiệm vụ và hội thoại
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
                    // SỬA LỖI: Chỉ chuyển sang Ending state khi người chơi đọc xong hội thoại
                    gp.setState(new Ending(gp, new MenuState(gp)));
                }
            }
        };
    }

    @Override
    public void enter() {
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
        this.weapon.automaticFire = true;
        gp.player.equipWeapon(this.weapon);
        weapon.clearBullets();

        // Tải vật cản
        this.obstacles = ObstacleManager.loadObstacles(OBSTACLE_TXT_PATH, this.scale);

        // TÍNH NĂNG MỚI: Chỉ sinh con quái đầu tiên khi bắt đầu màn
        spawnSingleEnemy(0);

        // Nạp ảnh nhân vật Vũ
        try {
            vuFace = ImageIO.read(getClass().getResourceAsStream("/player/down1.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        introScript = new DialogueLine[] {
                new DialogueLine("Giặc đến Bách Khoa rồi! Mình nhất định phải bảo vệ ngôi trường này.", vuFace),
                new DialogueLine("Quyết tử cho Tổ quốc quyết sinh!", vuFace)
        };

        afterQuestScript = new DialogueLine[] {
                new DialogueLine("Phù... Tạm thời khu vực này đã an toàn. \nPhải di chuyển tiếp sang khu nhà bên cạnh thôi!", vuFace)
        };

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

        // Chặn tương tác khi hội thoại mở
        if (dialogueBox.isActive()) {
            dialogueBox.update();
            if (gp.keyH.spacePressed) {
                dialogueBox.advanceDialogue();
                gp.keyH.spacePressed = false;
            }
            return;
        }

        if (isQuestCompleted) return;

        gp.player.update();
        for (int i = 0; i < gp.enemies.size(); i++) {
            gp.enemies.get(i).update();
        }

        gp.checkCollisions();

        if (gp.player.health <= 0) {
            gp.setState(new GameOverState(gp));
            return;
        }

        // TÍNH NĂNG MỚI: Nếu trên map hết quái và chưa diệt đủ 8 con thì cho sinh con tiếp theo
        if (gp.enemies.isEmpty() && gp.killCount < 8) {
            spawnSingleEnemy(gp.killCount);
        }

        // SỬA LỖI: Kiểm tra hoàn thành (Diệt đủ 8 quái) và bật hội thoại một lần duy nhất
        if (gp.killCount >= 8 && !isQuestCompleted) {
            isQuestCompleted = true;
            if (!isPhase2DialoguePlayed) {
                dialogueBox.startDialogue(afterQuestScript);
                isPhase2DialoguePlayed = true;
                // Đoạn logic chuyển Ending lập tức ở đây đã bị xóa, việc chuyển do onDialogueComplete() lo.
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        int cameraX = (int) (gp.player.worldX - gp.screenWidth / 2.0);
        int cameraY = (int) (gp.player.worldY - gp.screenHeight / 2.0);
        int[] clamped = gp.clampCameraPosition(cameraX, cameraY);
        cameraX = clamped[0];
        cameraY = clamped[1];

        // 1. Vẽ Map
        if (mapImage != null) {
            g2.drawImage(mapImage, 0, 0, gp.screenWidth, gp.screenHeight, cameraX, cameraY, cameraX + gp.screenWidth, cameraY + gp.screenHeight, null);
        }

        // 2. Vẽ Vật cản & Debug Mode
        for (Obstacle obs : obstacles) {
            obs.draw(g2, cameraX, cameraY);
            if (debugMode) {
                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke(2));
                int screenObsX = obs.worldX - cameraX;
                int screenObsY = obs.worldY - cameraY;
                g2.drawRect(screenObsX, screenObsY, obs.width, obs.height);
            }
        }

        // 3. Vẽ Lũ quái vật
        for (Enemy enemy : gp.enemies) {
            enemy.draw(g2, cameraX, cameraY);
            if (debugMode) {
                g2.setColor(Color.RED);
                g2.drawRect((int)enemy.worldX - cameraX, (int)enemy.worldY - cameraY, 48, 48);
            }
        }

        // 4. Vẽ Vũ
        gp.player.draw(g2, cameraX, cameraY);

        // 5. Vẽ Súng
        int screenX = (int) (gp.player.worldX - cameraX);
        int screenY = (int) (gp.player.worldY - cameraY);
        if (weapon != null) {
            weapon.draw(g2, screenX, screenY, cameraX, cameraY);
        }

        // 6. Vẽ HUD
        g2.setColor(Color.RED);
        g2.setFont(HUD_FONT);
        g2.drawString("Tiêu diệt kẻ địch: " + gp.killCount + " / 8", 10, 30);

        // 7. Vẽ Hộp thoại
        dialogueBox.draw(g2, gp.screenWidth, gp.screenHeight);
    }

    // TÍNH NĂNG MỚI: Hàm sinh từng con quái dựa trên thứ tự (chỉ số killCount)
    private void spawnSingleEnemy(int index) {
        Enemy e = null;
        switch (index) {
            case 0: e = new Enemy(gp, gp.player, 300, 300); e.speed = 1.5; break;
            case 1: e = new Enemy(gp, gp.player, 800, 500); e.speed = 1.5; break;
            case 2: e = new Enemy(gp, gp.player, 1200, 700); e.speed = 1.5; break;
            case 3: e = new Enemy(gp, gp.player, 500, 500); e.speed = 2.5; break; // Quái bỏ chạy
            case 4: e = new Enemy(gp, gp.player, 400, 400); e.speed = 1.5; break;
            case 5: e = new Enemy(gp, gp.player, 900, 600); e.speed = 1.5; break;
            case 6: e = new Enemy(gp, gp.player, 1300, 800); e.speed = 1.5; break;
            case 7: e = new Enemy(gp, gp.player, 600, 600); e.speed = 2.5; break;
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