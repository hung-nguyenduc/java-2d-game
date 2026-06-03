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
import entity.Grenade;

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
    private final int TOTAL_TARGETS = 10;
    
    // GIAO ĐOẠN 2 (LỰU ĐẠN)
    private boolean isPhase2 = false;
    private List<Grenade> grenades = new ArrayList<>();
    private int targetsPhase2Destroyed = 0;

    // CÁC BIẾN ĐIỀU KHIỂN BIA DI ĐỘNG "LỪA"
    private int targetSpeedY = 3;
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
                } else if (isQuestCompleted && !isPhase2) {
                    // Nếu thắng Phase 1, chuyển sang Phase 2
                    isPhase2 = true;
                    isQuestCompleted = false; // Reset to wait for phase 2 win
                    isPhase2DialoguePlayed = false;
                    
                    dialogueBox.startDialogue(new DialogueLine[]{
                        new DialogueLine("Giảng viên: Khá lắm! Nhưng giờ mới là bài kiểm tra thực sự.", null),
                        new DialogueLine("Vũ: Gì cơ? Còn bài thi ném lựu đạn nữa sao?", null),
                        new DialogueLine("Giảng viên: Dùng CHUỘT PHẢI để ném lựu đạn. Chỉ ném tối đa 4 quả cùng lúc. \nHãy phá hủy 4 bia di động kia đi!", null)
                    });
                    spawnPhase2Targets();
                } else if (isPhase2 && isQuestCompleted) {
                    // Thắng Phase 2
                    System.out.println("QUA MON QUAN SU!");
                    // Chuyển sang màn tiếp theo hoặc OpenWorld ở đây
                    gp.setState(new OpenWorldState(gp));
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
                new DialogueLine("Vũ: Xuất sắc! 10/10 phát trúng đích. \nThiên tài bắn súng B7 chính là mình!", null),
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
    
    private void spawnPhase2Targets() {
        targetBia.clear();
        for (int i = 0; i < 4; i++) {
            int targetX = gp.worldWidth - 350 + random.nextInt(250);
            int startY = gp.worldHeight / 4 + random.nextInt(gp.worldHeight / 2);
            Item bia = new Item("Bia Ma Quai", "/items/calculus.png", targetX, startY);
            bia.solidArea = new Rectangle(0, 0, 48, 48);
            targetBia.add(bia);
        }
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

        // 2. Chặn không cho bắn nếu đã hết đạn dự trữ (Chỉ ở Phase 1)
        if (!isPhase2 && bulletsLeft <= 0 && ak47.bullets.isEmpty() && !isQuestCompleted) {
            isGameOver = true;
            dialogueBox.startDialogue(failScript);
            return;
        }

        // Cập nhật Vũ khí & Đạn bay
        if (!isPhase2) {
            int prevBulletCount = ak47.bullets.size();
            ak47.update();
            if (ak47.bullets.size() > prevBulletCount) {
                bulletsLeft--; 
            }
        }
        
        // Cập nhật lựu đạn (Phase 2)
        if (isPhase2) {
            if (gp.mouseH.rightMousePressed && grenades.size() < 4) {
                double targetX = gp.mouseH.mouseX + (gp.player.worldX - gp.screenWidth / 2.0);
                double targetY = gp.mouseH.mouseY + (gp.player.worldY - gp.screenHeight / 2.0);
                grenades.add(new Grenade(gp, gp.player.worldX, gp.player.worldY, targetX, targetY));
                gp.mouseH.rightMousePressed = false; // reset
            }
            
            Iterator<Grenade> it = grenades.iterator();
            while (it.hasNext()) {
                Grenade g = it.next();
                g.update();
                
                if (g.isExploding && !g.damageDealt) {
                    // Check collision with targets
                    Iterator<Item> targetIt = targetBia.iterator();
                    while(targetIt.hasNext()) {
                        Item bia = targetIt.next();
                        double dx = bia.worldX + bia.solidArea.width/2 - g.worldX;
                        double dy = bia.worldY + bia.solidArea.height/2 - g.worldY;
                        double dist = Math.sqrt(dx*dx + dy*dy);
                        if (dist <= g.explosionRadius) {
                            targetIt.remove();
                            targetsPhase2Destroyed++;
                        }
                    }
                    g.damageDealt = true;
                }
                
                if (!g.isActive) {
                    it.remove();
                }
            }
        }

        // 3. THUẬT TOÁN DI CHUYỂN "LỪA" CỦA BIA ĐẠN
        for (Item bia : targetBia) {
            bia.worldY += targetSpeedY;

            if (bia.worldY <= targetMinY) {
                bia.worldY = targetMinY;
                targetSpeedY = Math.abs(targetSpeedY);
            } else if (bia.worldY >= targetMaxY) {
                bia.worldY = targetMaxY;
                targetSpeedY = -Math.abs(targetSpeedY);
            } else {
                int centerY = (targetMinY + targetMaxY) / 2;
                int zoneSize = (targetMaxY - targetMinY) / 4; 

                if (Math.abs(bia.worldY - centerY) < zoneSize) {
                    if (random.nextInt(100) < 3) {
                        targetSpeedY = -targetSpeedY;
                    }
                }
            }
        }

        // 4. XỬ LÝ ĐẠN BAY VÀ CHECK TRƯỢT/TRÚNG (Chỉ Phase 1)
        if (!isPhase2 && ak47 != null) {
            Iterator<Bullet> bulletIterator = ak47.bullets.iterator();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                Rectangle bulletRect = new Rectangle((int)bullet.worldX, (int)bullet.worldY, bullet.bulletSize, bullet.bulletSize);

                boolean bulletHit = false;

                if (!targetBia.isEmpty()) {
                    Item bia = targetBia.get(0);
                    Rectangle biaRect = new Rectangle(bia.worldX, bia.worldY, bia.solidArea.width, bia.solidArea.height);

                    if (bulletRect.intersects(biaRect)) {
                        System.out.println("TRÚNG ĐÍCH!");
                        bulletIterator.remove(); 
                        targetsDestroyed++;
                        gp.killCount = targetsDestroyed;
                        bulletHit = true;
                    }
                }

                if (!bulletHit && bullet.isOutOfRange()) {
                    System.out.println("Mất 1 viên đạn trượt!");
                }
            }
        }

        // Kiểm tra điều kiện Thắng môn (Phase 1)
        if (!isPhase2 && targetsDestroyed >= TOTAL_TARGETS && !isQuestCompleted) {
            isQuestCompleted = true;
            targetBia.clear();
        }

        if (!isPhase2 && isQuestCompleted && !isPhase2DialoguePlayed) {
            dialogueBox.startDialogue(afterQuestScript);
            isPhase2DialoguePlayed = true;
            return;
        }
        
        // Kiểm tra điều kiện Thắng môn (Phase 2)
        if (isPhase2 && targetBia.isEmpty() && !isQuestCompleted && !dialogueBox.isActive()) {
            isQuestCompleted = true; // Mark phase 2 win
            isPhase2DialoguePlayed = false; // re-use flag to show win dialog
            dialogueBox.startDialogue(new DialogueLine[]{
                new DialogueLine("Vũ: Tuyệt vời! 4 bia đã bị phá hủy.", null),
                new DialogueLine("Giảng viên: Quá xuất sắc! Vũ đã chính thức qua môn Quân Sự!", null)
            });
        }

        if (!isPhase2 && bulletsLeft <= 0 && ak47.bullets.isEmpty() && targetsDestroyed < TOTAL_TARGETS && !isQuestCompleted && !dialogueBox.isActive()) {
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

        // Tầng 4: Vẽ Lựu đạn (Phase 2) hoặc Súng AK47 (Phase 1)
        if (isPhase2) {
            for (Grenade g : grenades) {
                g.draw(g2, cameraX, cameraY);
            }
        } else if (ak47 != null) {
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
        if (!isPhase2) {
            g2.drawString("SỐ ĐẠN CÒN LẠI: " + bulletsLeft, 25, 38);
            g2.drawString("ĐIỂM TRÚNG BIA: " + targetsDestroyed + " / " + TOTAL_TARGETS, 25, 60);
        } else {
            g2.drawString("SỐ LỰU ĐẠN KHẢ DỤNG: " + (4 - grenades.size()) + " / 4", 25, 38);
            g2.drawString("SỐ BIA ĐÃ PHÁ HỦY: " + targetsPhase2Destroyed + " / 4", 25, 60);
        }

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