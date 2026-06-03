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
    private static final String MAP_IMAGE_PATH = "/maps/b7.png"; // Map bãi tập ném lựu
    private static final String OBSTACLE_TXT_PATH = "/maps/b7.txt";
    private static final double MAP_SCALE = 1.0 / 1.5;

    private DialogueManager dialogueBox;
    private Image mapImage;
    private List<Obstacle> obstacles = new ArrayList<>();
    BufferedImage vuFace;

    // Cơ chế Angry Birds (Kéo thả chuột)
    private boolean isAiming = false;
    private int dragStartX, dragStartY; // Điểm bắt đầu click chuột
    private double launchAngle = 0;
    private double launchPower = 0;
    private final double MAX_POWER = 15.0; // Giới hạn lực ném max

    // Quản lý quả lựu đạn trên map
    private List<Grenade> grenades = new ArrayList<>();
    private int grenadesLeft = 3; // Chỉ cho 3 quả lựu đạn thử thách

    // Ô mục tiêu cần ném trúng (Tọa độ thế giới)
    private Rectangle targetZone;
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
                if (isGameOver) gp.setState(new NemLuuDanQuanSu(gp)); // Thua thì cho thi lại
            }
        };
    }

    @Override
    public void enter() {
        // Load map tương tự các màn trước
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
        } catch (Exception e) {
            System.err.println("Lỗi nạp ảnh bản đồ, dùng nền đen dự phòng.");
        }

        this.obstacles = ObstacleManager.loadObstacles(OBSTACLE_TXT_PATH, MAP_SCALE);

        // Reset thông số màn chơi
        grenades.clear();
        grenadesLeft = 3;
        isQuestCompleted = false;
        isGameOver = false;
        isPhase2DialoguePlayed = false;

        // Đặt Vũ đứng ở góc dưới bên trái khu vực ném
        gp.player.worldX = 150;
        gp.player.worldY = gp.worldHeight - 200;

        // ĐỊNH NGHĨA Ô MỤC TIÊU BÊN PHẢI (Ví dụ: rộng 100x60 đặt cách rìa phải 250px)
        int zoneX = 500;
        int zoneY = 400; // Nằm sát mặt đất phía bên phải
        targetZone = new Rectangle(zoneX, zoneY, 120, 50);

        try {
            vuFace = ImageIO.read(getClass().getResourceAsStream("/player/down1.png"));
        } catch (Exception e) {}

        introScript = new DialogueLine[] {
                new DialogueLine("Chào mừng Vũ đến với bãi tập ném lựu đạn chuẩn chỉ!", null),
                new DialogueLine("Vũ: Click giữ chuột vào mình, kéo về sau để căn LỰC và GÓC, thả ra để ném lựu đạn trúng vào ô cát bên phải!", vuFace)
        };
        afterQuestScript = new DialogueLine[] { new DialogueLine("Vũ: Uỳnh!! Trúng hồng tâm ô cát rồi! Quá chuẩn!", vuFace) };
        failScript = new DialogueLine[] { new DialogueLine("Giảng viên: Hết lựu đạn rồi Vũ ơi, ném toàn trượt ra ngoài, cấm thi!", null) };

        dialogueBox.startDialogue(introScript);
    }

    @Override
    public List<Obstacle> getObstacles() { return this.obstacles; }

    @Override
    public void update() {
        if (dialogueBox.isActive()) {
            if (gp.keyH.spacePressed) { dialogueBox.advanceDialogue(); gp.keyH.spacePressed = false; }
            return;
        }
        if (isGameOver || isQuestCompleted) return;

        gp.player.update();

        // XỬ LÝ ĐIỀU KHIỂN CHUỘT KIỂU ANGRY BIRDS
        // Khi đè chuột trái -> Bắt đầu tính góc ném, lực ném
        if (gp.mouseH.leftMousePressed) {
            if (!isAiming) {
                isAiming = true;
                dragStartX = gp.mouseH.mouseX;
                dragStartY = gp.mouseH.mouseY;
            } else {
                // Tính khoảng cách kéo kéo chuột
                double dx = dragStartX - gp.mouseH.mouseX;
                double dy = dragStartY - gp.mouseH.mouseY;

                // Góc bắn (độ) ngược hướng kéo
                launchAngle = Math.toDegrees(Math.atan2(dy, dx));

                // Lực bắn tỷ lệ thuận với độ dài kéo chuột
                launchPower = Math.sqrt(dx*dx + dy*dy) * 0.08;
                if (launchPower > MAX_POWER) launchPower = MAX_POWER; // Giới hạn lực
            }
        }
        // Khi thả chuột ra -> KÍCH NỔ / PHÓNG QUẢ LỰU ĐẠN
        else if (isAiming) {
            isAiming = false;
            if (grenadesLeft > 0) {
                // Sinh lựu đạn ngay tại vị trí của Vũ
                Grenade g = new Grenade(gp.player.worldX + 20, gp.player.worldY + 20, launchAngle, launchPower);
                grenades.add(g);
                grenadesLeft--;
            }
        }

        // CẬP NHẬT CHUYỂN ĐỘNG QUẢ LỰU ĐẠN
        Iterator<Grenade> it = grenades.iterator();
        while (it.hasNext()) {
            Grenade g = it.next();
            g.update();

            // Tạo hitbox kiểm tra va chạm
            Rectangle grenadeRect = new Rectangle((int)g.worldX, (int)g.worldY, g.size, g.size);

            // Kiểm tra nếu lựu đạn rơi trúng ô mục tiêu cho sẵn
            if (grenadeRect.intersects(targetZone)) {
                isQuestCompleted = true;
                it.remove();
                break;
            }

            // Nếu lựu đạn nổ tung (hết thời gian bay) mà không trúng ô mục tiêu
            if (g.isExploded) {
                System.out.println("Lựu đạn đã nổ ngoài vùng mục tiêu!");
                it.remove();
            }
        }

        // Check điều kiện Thua (Hết lựu đạn bay trên trời và hết lựu đạn dự trữ mà chưa trúng mục tiêu)
        if (grenadesLeft <= 0 && grenades.isEmpty() && !isQuestCompleted) {
            isGameOver = true;
            dialogueBox.startDialogue(failScript);
        }

        // Kích hoạt kết màn thắng cuộc
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

        // 1. Vẽ Map
        if (mapImage != null) {
            g2.drawImage(mapImage, 0, 0, gp.screenWidth, gp.screenHeight, cameraX, cameraY, cameraX + gp.screenWidth, cameraY + gp.screenHeight, null);
        }

        // 2. VẼ Ô MỤC TIÊU CHO SẴN TRÊN MẶT ĐẤT
        g2.setColor(new Color(255, 69, 0, 100)); // Màu cam đỏ trong suốt dễ nhận diện
        g2.fillRect(targetZone.x - cameraX, targetZone.y - cameraY, targetZone.width, targetZone.height);
        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(targetZone.x - cameraX, targetZone.y - cameraY, targetZone.width, targetZone.height);

        // 3. Vẽ Vũ
        gp.player.draw(g2, cameraX, cameraY);

        // 4. Vẽ Lựu đạn đang bay
        for (Grenade g : grenades) {
            g.draw(g2, cameraX, cameraY);
        }

        // 5. VẼ ĐƯỜNG DẪN KÉO LỰC (ANGRY BIRDS UI TRỰC QUAN)
        if (isAiming && grenadesLeft >= 0) {
            int pScreenX = (int)(gp.player.worldX - cameraX) + 20;
            int pScreenY = (int)(gp.player.worldY - cameraY) + 20;

            // Vẽ một đường line hiển thị lực kéo từ tay Vũ ngược ra hướng bắn
            g2.setColor(Color.YELLOW);
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{9}, 0)); // Nét đứt sinh động

            // Tính toán điểm mút của mũi tên lực ngắm bắn
            int endX = pScreenX + (int)(Math.cos(Math.toRadians(launchAngle)) * launchPower * 10);
            int endY = pScreenY + (int)(Math.sin(Math.toRadians(launchAngle)) * launchPower * 10);
            g2.drawLine(pScreenX, pScreenY, endX, endY);
        }

        // 6. Vẽ bảng thông số HUD
        g2.setColor(Color.BLACK); g2.fillRect(15, 15, 250, 65);
        g2.setColor(Color.GREEN); g2.drawRect(15, 15, 250, 65);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Consolas", Font.BOLD, 14));
        g2.drawString("LỰU ĐẠN CÒN: " + grenadesLeft, 25, 38);
        g2.drawString("MỤC TIÊU: " + (isQuestCompleted ? "ĐÃ TRÚNG" : "CHƯA TRÚNG"), 25, 60);

        dialogueBox.draw(g2, gp.screenWidth, gp.screenHeight);
    }

    @Override
    public void exit() { grenades.clear(); obstacles.clear(); }

    @Override
    public void handleMouseClick(MouseEvent e) {}
}