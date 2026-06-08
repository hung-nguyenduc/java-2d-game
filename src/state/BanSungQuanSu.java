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
    private static final String MAP_IMAGE_PATH = "/maps/b7-updated.png";
    private static final String OBSTACLE_TXT_PATH = "/maps/b7.txt";
    private static final double MAP_SCALE = 1.0 / 3.0;
    private DialogueManager dialogueBox;
    private Image mapImage;
    private List<Obstacle> obstacles = new ArrayList<>();
    BufferedImage vuFace;
    BufferedImage playerBanSung;
    BufferedImage thayGiao;
    private boolean isQuestCompleted = false;
    private boolean isPhase2DialoguePlayed = false;
    private DialogueLine[] introScript;
    private DialogueLine[] afterQuestScript;   // Trúng đúng 8 viên
    private DialogueLine[] afterQuest9Script;  // Trúng 9 viên
    private DialogueLine[] afterQuest10Script; // Trúng 10 viên
    private DialogueLine[] failScript; // Kịch bản khi trượt môn

    // Quản lý bia đỡ đạn
    private List<Item> targetBia = new ArrayList<>();
    private int targetsDestroyed = 0;
    private final int TOTAL_TARGETS = 8;   // Số đạn tối thiểu phải bắn TRÚNG để qua môn
    private final int TOTAL_BULLETS = 10;  // Tổng số đạn bắt buộc phải bắn hết trước khi chấm điểm

    // CÁC BIẾN ĐIỀU KHIỂN BIA DI ĐỘNG "LỪA"
    private int targetSpeedY = 2;
    private int targetMinY;
    private int targetMaxY;
    private Random random = new Random();

    // HỆ THỐNG GIỚI HẠN ĐẠN TRƯỢT
    private int bulletsLeft = TOTAL_BULLETS;   // Số đạn dự trữ còn lại
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
        gp.sound.playMusic("nhac_nen_mainmenu"); // Nhạc nền nhẹ nhàng cho màn Bắn Súng
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
            playerBanSung = ImageIO.read(getClass().getResourceAsStream("/player/ban-sung-updated.png"));
            mapImage = compatibleMap;
            thayGiao =  ImageIO.read(getClass().getResourceAsStream("/NPC/thay-giao.png"));
        } catch (Exception e) {
            System.err.println("Lỗi nạp ảnh bản đồ tại: " + MAP_IMAGE_PATH);
            e.printStackTrace();
        }

        this.obstacles = ObstacleManager.loadObstacles(OBSTACLE_TXT_PATH, MAP_SCALE);

        // Reset hệ thống
        gp.killCount = 0;
        gp.player.health = 100;
        gp.enemies.clear();
        bulletsLeft = TOTAL_BULLETS;
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
        targetMinY = gp.worldHeight / 5 + 100;
        targetMaxY = (gp.worldHeight * 4) / 5 -10;

        spawnSingleBiaMucTieu();

        try {
            vuFace = ImageIO.read(getClass().getResourceAsStream("/player/down3.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        introScript = new DialogueLine[] {
                new DialogueLine("Học phần bắn súng Kỹ thuật chiến đấu bộ binh và chiến thuật bắt đầu!", thayGiao),
                new DialogueLine("Bắn hết cả " + TOTAL_BULLETS + " viên, trúng từ " + TOTAL_TARGETS + " viên trở lên để qua môn!", thayGiao),
        };

        // Lời thoại khi qua môn với đúng 8 viên trúng
        afterQuestScript = new DialogueLine[] {
                new DialogueLine("Ngon! Trúng được 8 viên, vừa đủ điểm", vuFace),
        };

        // Lời thoại khi trúng 9 viên
        afterQuest9Script = new DialogueLine[] {
                new DialogueLine("Ngon luôn, bắn trúng 9 đạn!!!", vuFace),
        };

        // Lời thoại khi trúng trọn vẹn 10 viên
        afterQuest10Script = new DialogueLine[] {
                new DialogueLine("Bắn trúng 10 đạn, em thật xuất sắc!!!", vuFace),
        };

        failScript = new DialogueLine[] {
                new DialogueLine("Thầy giáo: Bắn trượt rồi Vũ ơi! Thế này mà ra trận thì chết à", thayGiao),
                new DialogueLine("Thầy giáo: Chuẩn bị tiền học lại đi em", thayGiao)
        };

        dialogueBox.startDialogue(introScript);
    }

    private void spawnSingleBiaMucTieu() {
        targetBia.clear();
        int targetX = gp.worldWidth - 250;
        int startY = gp.worldHeight / 2;

        Item bia = new Item("Bia Ma Quai", "/items/bia.png", targetX, startY);
        bia.solidArea = new Rectangle(0, 0, 48, 48);
        targetBia.add(bia);
    }

    @Override
    public List<Obstacle> getObstacles() {
        return this.obstacles;
    }

    @Override
    public void update() {
        // Phím P để nhảy cấp nhanh
        if (gp.keyH.pPressed) {
            gp.keyH.pPressed = false;
            gp.setState(new LoadingState(gp, new NemLuuDanQuanSu(gp)));
            return;
        }

        if (dialogueBox.isActive()) {
            dialogueBox.update();
            if (gp.keyH.spacePressed) {
                dialogueBox.advanceDialogue();
                gp.keyH.spacePressed = false;
            }
            return;
        }

        if (isGameOver || isQuestCompleted) return;

        // 1. Cập nhật Player
        //gp.player.update();

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
                        gp.sound.playSE("hit_bia"); // Tiếng "ding" khi bắn trúng bia
                        ak47.bullets.remove(bullet); // Xóa viên đạn vừa trúng (Dùng remove trên collection để tránh lỗi với CopyOnWriteArrayList)
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

        // TRƯỢT SỚM: nếu số đạn còn lại không đủ để đạt 8 viên trúng (lỡ trượt quá 2 viên,
        // tối đa chỉ còn 7) thì cho trượt luôn, không bắt bắn hết vô ích.
        int soVienToiDaConCoThe = targetsDestroyed + bulletsLeft + ak47.bullets.size();
        if (soVienToiDaConCoThe < TOTAL_TARGETS && !isQuestCompleted && !isGameOver) {
            isGameOver = true;
            dialogueBox.startDialogue(failScript);
            return;
        }

        // CHẤM ĐIỂM: chỉ xét khi đã bắn HẾT cả 10 viên (hết đạn dự trữ và không còn đạn đang bay)
        boolean banHetDan = bulletsLeft <= 0 && ak47.bullets.isEmpty();
        if (banHetDan && !isQuestCompleted && !isGameOver) {
            if (targetsDestroyed >= TOTAL_TARGETS) {
                // Trúng từ 8 viên trở lên -> QUA MÔN, chọn lời thoại theo số viên trúng
                isQuestCompleted = true;
                isPhase2DialoguePlayed = true;
                targetBia.clear();

                DialogueLine[] winScript;
                if (targetsDestroyed >= TOTAL_BULLETS) {
                    winScript = afterQuest10Script;       // trúng trọn 10 viên
                } else if (targetsDestroyed == 9) {
                    winScript = afterQuest9Script;        // trúng 9 viên
                } else {
                    winScript = afterQuestScript;         // trúng đúng 8 viên
                }
                dialogueBox.startDialogue(winScript);
                return;
            } else {
                // Trúng dưới 8 viên -> TRƯỢT MÔN
                isGameOver = true;
                dialogueBox.startDialogue(failScript);
                return;
            }
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
        //gp.player.draw(g2, cameraX, cameraY);

            g2.drawImage(playerBanSung, 100, 290, 100, 60, null);


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
        g2.drawString("ĐIỂM TRÚNG BIA: " + targetsDestroyed + " / " + TOTAL_BULLETS, 25, 60);

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