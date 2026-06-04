package state;

import Dialogue.DialogueLine;
import Dialogue.DialogueManager;
import entity.Enemy;
import main.GamePanel;
import collision.Obstacle;
import collision.ObstacleManager;
import entity.Item;
import entity.Weapon;
import entity.Bullet;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class BanSungQuanSu extends GameState {
    private boolean debugMode = false;
    private static final String MAP_IMAGE_PATH = "/maps/b7.png";
    private static final String OBSTACLE_TXT_PATH = "/maps/b7.txt";
    private static final double MAP_SCALE = 1.0 / 1.5;
    private DialogueManager dialogueBox;
    private Image mapImage;
    private List<Obstacle> obstacles = new ArrayList<>();
    BufferedImage vuFace;

    private boolean isQuestCompleted = false;
    private boolean isPhase2DialoguePlayed = false;
    private DialogueLine[] introScript;
    private DialogueLine[] afterQuestScript;
    private DialogueLine[] failScript; // Kịch bản khi trượt môn

    // Quản lý bia đỡ đạn
    private List<Item> targetBia = new ArrayList<>();
    private int targetsDestroyed = 0;
    private final int TOTAL_TARGETS = 5;

    // CÁC BIẾN ĐIỀU KHIỂN BIA DI ĐỘNG "LỪA"
    private int targetSpeedY = 2;
    private int targetMinY;
    private int targetMaxY;
    private Random random = new Random();

    // HỆ THỐNG GIỚI HẠN ĐẠN TRƯỢT
    private int bulletsLeft = 10;         // Chỉ có đúng 5 viên đạn
    private boolean isGameOver = false;

    private Weapon ak47;

    public BanSungQuanSu(GamePanel gp) {
        super(gp);
        dialogueBox = new DialogueManager() {
            @Override
            public void onDialogueComplete() {
                if (isGameOver) {
                    // Nếu thua (hết đạn), reset lại chính map này để Vũ thi lại
                    gp.setState(new BanSungQuanSu(gp));
                } else if (isQuestCompleted) {
                    // Nếu thắng, qua màn tiếp theo
                    gp.setState(new LoadingState(gp, new NemLuuDanQuanSu(gp)));
                }
            }
        };
    }

    @Override
    public void enter() {
        try {
            BufferedImage src = ImageIO.read(getClass().getResourceAsStream(MAP_IMAGE_PATH));
            gp.worldWidth = (int) (src.getWidth() * MAP_SCALE);
            gp.worldHeight = (int) (src.getHeight() * MAP_SCALE);

            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            BufferedImage compatibleMap = gc.createCompatibleImage(gp.worldWidth, gp.worldHeight, Transparency.OPAQUE);

            Graphics2D g2d = compatibleMap.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(src, 0, 0, gp.worldWidth, gp.worldHeight, null);
            g2d.dispose();

            mapImage = compatibleMap;
        } catch (Exception e) {
            System.err.println("Lỗi nạp ảnh bản đồ tại: " + MAP_IMAGE_PATH);
            e.printStackTrace();
        }

        this.obstacles = ObstacleManager.loadObstacles(OBSTACLE_TXT_PATH, MAP_SCALE);

        // Reset hệ thống
        gp.killCount = 0;
        gp.player.health = 100;
        gp.enemies.clear();
        bulletsLeft = 10;
        targetsDestroyed = 0;
        isGameOver = false;
        isQuestCompleted = false;
        isPhase2DialoguePlayed = false;

        ak47 = new Weapon(gp, gp.mouseH, gp.player);
        ak47.shotgunMode = false;
        ak47.clearBullets();

        gp.player.worldX = 150;
        gp.player.worldY = gp.worldHeight / 2 - 16;

        // Thiết lập biên di chuyển dọc
        targetMinY = gp.worldHeight / 5;
        targetMaxY = (gp.worldHeight * 4) / 5;

        spawnSingleBiaMucTieu();

        try {
            vuFace = ImageIO.read(getClass().getResourceAsStream("/player/down1.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        introScript = new DialogueLine[] {
                new DialogueLine("Học phần bắn súng tính điểm khắc nghiệt bắt đầu!", null),
                new DialogueLine("Vũ: Bia đợt này di chuyển cực kỳ lắt léo và mình chỉ có đúng " + bulletsLeft + " \nviên đạn. Bắn trượt một viên là coi như trượt môn!", null),
        };

        afterQuestScript = new DialogueLine[] {
                new DialogueLine("Vũ: Xuất sắc! 5/5 phát trúng đích. \nThiên tài bắn súng B7 chính là mình!", null),
        };

        failScript = new DialogueLine[] {
                new DialogueLine("Giảng viên: Bắn trượt rồi Vũ ơi! Hết đạn mà chưa đủ điểm, \nchuẩn bị tiền học lại quân sự đi em...", null),
        };

        dialogueBox.startDialogue(introScript);
    }

    private void spawnSingleBiaMucTieu() {
        targetBia.clear();
        int targetX = gp.worldWidth - 250;
        int startY = gp.worldHeight / 2;

        Item bia = new Item("Bia Ma Quai", "/items/calculus.png", targetX, startY);
        bia.solidArea = new Rectangle(0, 0, 48, 48);
        targetBia.add(bia);
    }

    @Override
    public List<Obstacle> getObstacles() {
        return this.obstacles;
    }

    @Override
    public void update() {
        if (dialogueBox.isActive()) {
            if (gp.keyH.spacePressed) {
                dialogueBox.advanceDialogue();
                gp.keyH.spacePressed = false;
            }
            return;
        }

        if (isGameOver || isQuestCompleted) return;

        // 1. Cập nhật Player
        gp.player.update();

        // 2. Chặn không cho bắn nếu đã hết đạn dự trữ
        if (bulletsLeft <= 0 && ak47.bullets.isEmpty() && !isQuestCompleted) {
            isGameOver = true;
            dialogueBox.startDialogue(failScript);
            return;
        }

        // Cập nhật Vũ khí & Đạn bay
        // Đồng thời kiểm tra nếu người chơi click bắn súng thành công thì trừ đạn dự trữ đi
        int prevBulletCount = ak47.bullets.size();
        ak47.update();
        if (ak47.bullets.size() > prevBulletCount) {
            bulletsLeft--; // Đạn vừa được bắn ra khỏi nòng súng -> trừ đi 1 viên
        }

        // 3. THUẬT TOÁN DI CHUYỂN "LỪA" CỦA BIA ĐẠN
        if (!targetBia.isEmpty()) {
            Item bia = targetBia.get(0);
            bia.worldY += targetSpeedY;

            // Kiểm tra đụng biên cứng -> Ép buộc phải quay đầu
            if (bia.worldY <= targetMinY) {
                bia.worldY = targetMinY;
                targetSpeedY = Math.abs(targetSpeedY); // Đi xuống
            } else if (bia.worldY >= targetMaxY) {
                bia.worldY = targetMaxY;
                targetSpeedY = -Math.abs(targetSpeedY); // Đi lên
            } else {
                // LOGIC LỪA: Khi bia đi vào khu vực giữa map (khoảng cách biên trên và biên dưới)
                int centerY = (targetMinY + targetMaxY) / 2;
                int zoneSize = (targetMaxY - targetMinY) / 4; // Vùng nguy hiểm ở giữa map

                if (Math.abs(bia.worldY - centerY) < zoneSize) {
                    // Cứ mỗi frame trôi qua trong khu vực giữa, có tỉ lệ 3% tự động bẻ lái quay đầu ngược lại
                    if (random.nextInt(100) < 3) {
                        targetSpeedY = -targetSpeedY;
                    }
                }
            }
        }

        // 4. XỬ LÝ ĐẠN BAY VÀ CHECK TRƯỢT/TRÚNG
        if (ak47 != null) {
            Iterator<Bullet> bulletIterator = ak47.bullets.iterator();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                Rectangle bulletRect = new Rectangle((int)bullet.worldX, (int)bullet.worldY, bullet.bulletSize, bullet.bulletSize);

                boolean bulletHit = false;

                // Check va chạm trúng bia
                if (!targetBia.isEmpty()) {
                    Item bia = targetBia.get(0);
                    Rectangle biaRect = new Rectangle(bia.worldX, bia.worldY, bia.solidArea.width, bia.solidArea.height);

                    if (bulletRect.intersects(biaRect)) {
                        System.out.println("TRÚNG ĐÍCH!");
                        bulletIterator.remove(); // Xóa viên đạn vừa trúng
                        targetsDestroyed++;
                        gp.killCount = targetsDestroyed;
                        bulletHit = true;
                    }
                }

                // NẾU KHÔNG TRÚNG: Check viên đạn này xem nó có bị trượt ngoài phạm vi (Out Of Range) không
                if (!bulletHit && bullet.isOutOfRange()) {
                    System.out.println("Mất 1 viên đạn trượt!");
                    // Viên đạn đã tự biến mất trong hàm update() của Weapon,
                    // Ở đây do ta đã cấu hình trừ đạn ngay từ khi click bắn, nên không cần xử lý trừ thêm, tránh hụt đúp.
                }
            }
        }

        // Kiểm tra điều kiện Thắng môn
        if (targetsDestroyed >= TOTAL_TARGETS && !isQuestCompleted) {
            isQuestCompleted = true;
            targetBia.clear();
        }

        if (isQuestCompleted && !isPhase2DialoguePlayed) {
            dialogueBox.startDialogue(afterQuestScript);
            isPhase2DialoguePlayed = true;
            return;
        }

        // Kiểm tra nếu người chơi bắn hết sạch cả 5 viên đạn mà điểm vẫn chưa đạt 5 -> Thua cuộc
        if (bulletsLeft <= 0 && ak47.bullets.isEmpty() && targetsDestroyed < TOTAL_TARGETS && !isQuestCompleted && !dialogueBox.isActive()) {
            isGameOver = true;
            dialogueBox.startDialogue(failScript);
        }

        gp.checkCollisions();
    }

    @Override
    public void draw(Graphics2D g2) {
        int cameraX = (int) (gp.player.worldX - gp.screenWidth / 2.0);
        int cameraY = (int) (gp.player.worldY - gp.screenHeight / 2.0);

        int[] clamped = gp.clampCameraPosition(cameraX, cameraY);
        cameraX = clamped[0];
        cameraY = clamped[1];

        // Tầng 1: Vẽ Map nền
        if (mapImage != null) {
            g2.drawImage(mapImage, 0, 0, gp.screenWidth, gp.screenHeight,
                    cameraX, cameraY, cameraX + gp.screenWidth, cameraY + gp.screenHeight, null);
        }

        // Tầng 2: Vẽ Bia đỡ đạn ma quái
        for (Item bia : targetBia) {
            bia.draw(g2, cameraX, cameraY);
        }

        // Tầng 3: Vẽ Nhân vật Vũ
        gp.player.draw(g2, cameraX, cameraY);

        // Tầng 4: Vẽ Súng AK47 và các viên đạn đang bay
        if (ak47 != null) {
            int screenX = (int) (gp.player.worldX - cameraX);
            int screenY = (int) (gp.player.worldY - cameraY);
            ak47.draw(g2, screenX, screenY, cameraX, cameraY);
        }

        // Tầng 5: Giao diện HUD hiển thị số đạn giới hạn & số điểm khắc nghiệt
        g2.setColor(Color.BLACK);
        g2.fillRect(15, 15, 260, 65);
        g2.setColor(isGameOver ? Color.RED : Color.YELLOW);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(15, 15, 260, 65);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 14));
        g2.drawString("SỐ ĐẠN CÒN LẠI: " + bulletsLeft, 25, 38);
        g2.drawString("ĐIỂM TRÚNG BIA: " + targetsDestroyed + " / " + TOTAL_TARGETS, 25, 60);

        if (debugMode) {
            for (Obstacle obs : obstacles) {
                obs.draw(g2, cameraX, cameraY);
            }
        }

        dialogueBox.draw(g2, gp.screenWidth, gp.screenHeight);
    }

    @Override
    public void exit() {
        targetBia.clear();
        obstacles.clear();
        if (ak47 != null) {
            ak47.clearBullets();
            ak47 = null;
        }
    }

    @Override
    public void handleMouseClick(MouseEvent e) {}
}