package state;

import Dialogue.DialogueLine;
import Dialogue.DialogueManager;
import main.GamePanel;
import collision.Obstacle;
import collision.ObstacleManager;
import entity.Grenade;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NemLuuDanQuanSu extends GameState {
    private boolean debugMode = false;
    private static final String MAP_IMAGE_PATH = "/maps/b7.png";
    private static final String OBSTACLE_TXT_PATH = "/maps/b7.txt";
    private static final double MAP_SCALE = 1.0 / 1.5;

    private DialogueManager dialogueBox;
    private Image mapImage;
    private List<Obstacle> obstacles = new ArrayList<>();
    BufferedImage vuFace;
    BufferedImage player;
    // Cơ chế Angry Birds
    private boolean isAiming = false;
    private int dragStartX, dragStartY;
    private double launchAngle = 0;
    private double launchPower = 0;
    private final double MAX_POWER = 16.0;

    // Quản lý lựu đạn
    private List<Grenade> grenades = new ArrayList<>();

    // ĐIỀU CHỈNH: Tăng số lựu đạn lên 5 và yêu cầu trúng tối thiểu 3
    private int grenadesLeft = 5;
    private int targetsHit = 0;
    private final int REQUIRED_HITS = 3;

    private Rectangle targetZone;
    private int groundY;

    private boolean isQuestCompleted = false;
    private boolean isGameOver = false;
    private boolean isPhase2DialoguePlayed = false;

    private DialogueLine[] introScript;
    private DialogueLine[] afterQuestScript;
    private DialogueLine[] failScript;

    public NemLuuDanQuanSu(GamePanel gp) {
        super(gp);
        dialogueBox = new DialogueManager() {
            @Override
            public void onDialogueComplete() {
                if (isGameOver) {
                    gp.setState(new NemLuuDanQuanSu(gp));
                }
                else if (isQuestCompleted) {
                    gp.setState(new LoadingState2(gp, new ZombieState(gp)));
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
            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            BufferedImage compatibleMap = gc.createCompatibleImage(gp.worldWidth, gp.worldHeight, Transparency.OPAQUE);
            Graphics2D g2d = compatibleMap.createGraphics();
            g2d.drawImage(src, 0, 0, gp.worldWidth, gp.worldHeight, null);
            g2d.dispose();
            mapImage = compatibleMap;
            player = ImageIO.read(getClass().getResourceAsStream("/player/right1-ver2.png"));
        } catch (Exception e) {
            System.err.println("Lỗi nạp ảnh bản đồ, dùng nền đen dự phòng.");
        }

        this.obstacles = ObstacleManager.loadObstacles(OBSTACLE_TXT_PATH, MAP_SCALE);

        grenades.clear();
        grenadesLeft = 5;
        targetsHit = 0;
        isQuestCompleted = false;
        isGameOver = false;
        isPhase2DialoguePlayed = false;

        groundY = gp.worldHeight - 200;

        gp.player.worldX = 100;
        gp.player.worldY = groundY - 50;

        // Thiết lập ô cát mục tiêu
        int zoneWidth = 140;
        int zoneHeight = 40;
        int zoneX = 600;
        int zoneY = groundY - zoneHeight;
        targetZone = new Rectangle(zoneX, zoneY, zoneWidth, zoneHeight);

        try {
            vuFace = ImageIO.read(getClass().getResourceAsStream("/player/down1.png"));
        } catch (Exception e) {}

        introScript = new DialogueLine[] {
                new DialogueLine("Môn thi cuối cùng: Ném lựu đạn", null),
                new DialogueLine("Cần ném trúng " + REQUIRED_HITS + "/" + grenadesLeft + " quả", null)
        };
        afterQuestScript = new DialogueLine[] { new DialogueLine("Vũ: Đạt " + REQUIRED_HITS + " quả trúng mục tiêu rồi! Qua môn rồi hẹ hẹ hẹ", null) };
        failScript = new DialogueLine[] { new DialogueLine("Thầy giáo: Hết lựu đạn rồi Vũ ơi, ném trượt nhiều quá!", null),
                new DialogueLine("Thầy giáo: Trượt môn về học lại đi em", null)
        };

        dialogueBox.startDialogue(introScript);
    }

    @Override
    public List<Obstacle> getObstacles() { return this.obstacles; }

    @Override
    public void update() {
        // Phím P để nhảy cấp nhanh
        if (gp.keyH.pPressed) {
            gp.keyH.pPressed = false;
            gp.setState(new LoadingState2(gp, new ZombieState(gp)));
            return;
        }

        if (dialogueBox.isActive()) {
            if (gp.keyH.spacePressed) { dialogueBox.advanceDialogue(); gp.keyH.spacePressed = false; }
            return;
        }
        if (isGameOver || isQuestCompleted) return;

        //gp.player.update();

        // XỬ LÝ KÉO THẢ CHUỘT (ANGRY BIRDS)
        if (gp.mouseH.leftMousePressed) {
            if (!isAiming) {
                isAiming = true;
                dragStartX = gp.mouseH.mouseX;
                dragStartY = gp.mouseH.mouseY;
            } else {
                double dx = dragStartX - gp.mouseH.mouseX;
                double dy = dragStartY - gp.mouseH.mouseY;
                launchAngle = Math.toDegrees(Math.atan2(dy, dx));
                launchPower = Math.sqrt(dx*dx + dy*dy) * 0.07;
                if (launchPower > MAX_POWER) launchPower = MAX_POWER;
            }
        }
        else if (isAiming) {
            isAiming = false;
            if (grenadesLeft > 0) {
                Grenade g = new Grenade(gp.player.worldX + 24, gp.player.worldY + 16, launchAngle, launchPower);
                grenades.add(g);
                grenadesLeft--;
            }
        }

        // CẬP NHẬT LOGIC KIỂM TRA LỰU ĐẠN RƠI TRÚNG
        Iterator<Grenade> it = grenades.iterator();
        while (it.hasNext()) {
            Grenade g = it.next();
            g.update(groundY);

            // Kiểm tra ngay khi lựu đạn vừa ghi nhận lần chạm đất đầu tiên
            if (g.bounceCount == 1 && !g.isFirstTouchHandled) {
                g.isFirstTouchHandled = true; // Đảm bảo chỉ check va chạm duy nhất một lần tại frame này

                Rectangle grenadeRect = new Rectangle((int)g.worldX, (int)g.worldY, g.size, g.size);

                // ĐIỀU KIỆN ĐÚNG LUẬT: Lần đầu chạm đất phải nằm trong Target Zone
                if (grenadeRect.intersects(targetZone)) {
                    System.out.println("Trúng");
                    targetsHit++;
                } else {
                    System.out.println("Trượt");
                    // Quả lựu này tuy vẫn bay/nảy tiếp nhưng đã bị đánh dấu hụt điểm do trượt mục tiêu đầu
                }
            }

            // Xóa quả lựu đạn khỏi màn hình sau khi nó nổ xong hiệu ứng (isDead = true)
            if (g.isDead) {
                it.remove();
            }
        }

        // Kiểm tra điều kiện Thắng cuộc
        if (targetsHit >= REQUIRED_HITS && !isQuestCompleted) {
            isQuestCompleted = true;
            grenades.clear();
        }

        // Kiểm tra điều kiện Thua cuộc (Không còn đủ lựu đạn để lật ngược tình thế)
        if (grenadesLeft + grenades.size() < (REQUIRED_HITS - targetsHit) && !isQuestCompleted) {
            isGameOver = true;
            dialogueBox.startDialogue(failScript);
        }

        if (isQuestCompleted && !isPhase2DialoguePlayed) {
            dialogueBox.startDialogue(afterQuestScript);
            isPhase2DialoguePlayed = true;
        }

        gp.checkCollisions();
    }

    @Override
    public void draw(Graphics2D g2) {
        int cameraX = (int) (gp.player.worldX - gp.screenWidth / 2.0);
        int cameraY = (int) (gp.player.worldY - gp.screenHeight / 2.0);
        int[] clamped = gp.clampCameraPosition(cameraX, cameraY);
        cameraX = clamped[0]; cameraY = clamped[1];

        // 1. Vẽ Map nền
        if (mapImage != null) {
            g2.drawImage(mapImage, 0, 0, gp.screenWidth, gp.screenHeight, cameraX, cameraY, cameraX + gp.screenWidth, cameraY + gp.screenHeight, null);
        }

        // 2. Vẽ ô mục tiêu cát
        if (targetZone != null) {
            g2.setColor(new Color(238, 214, 175, 160));
            g2.fillRect(targetZone.x - cameraX, targetZone.y - cameraY, targetZone.width, targetZone.height);
            g2.setColor(Color.ORANGE);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(targetZone.x - cameraX, targetZone.y - cameraY, targetZone.width, targetZone.height);
        }

        // 3. Vẽ Vũ
        //gp.player.draw(g2, cameraX, cameraY);
        g2.drawImage(player, 100, 300, 80, 120, null);

        // 4. Vẽ lựu đạn đang bay/nảy/nổ
        for (Grenade g : grenades) {
            g.draw(g2, cameraX, cameraY, gp);
        }

        // 5. Vẽ dây kéo lực Angry Birds
        if (isAiming && grenadesLeft >= 0) {
            int pScreenX = (int)(gp.player.worldX - cameraX) + 24;
            int pScreenY = (int)(gp.player.worldY - cameraY) + 16;

            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8}, 0));

            int endX = pScreenX + (int)(Math.cos(Math.toRadians(launchAngle)) * launchPower * 11);
            int endY = pScreenY + (int)(Math.sin(Math.toRadians(launchAngle)) * launchPower * 11);
            g2.drawLine(pScreenX, pScreenY, endX, endY);
        }

        // 6. Vẽ HUD thông số nâng cấp
        g2.setColor(Color.BLACK); g2.fillRect(15, 15, 270, 65);
        g2.setColor(Color.CYAN); g2.drawRect(15, 15, 270, 65);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Consolas", Font.BOLD, 14));
        g2.drawString("LỰU ĐẠN CÒN: " + grenadesLeft, 25, 38);
        g2.drawString("SỐ QUẢ TRÚNG: " + targetsHit + " / " + REQUIRED_HITS, 25, 60);

        dialogueBox.draw(g2, gp.screenWidth, gp.screenHeight);
    }

    @Override
    public void exit() { grenades.clear(); obstacles.clear(); }

    @Override
    public void handleMouseClick(MouseEvent e) {}
}