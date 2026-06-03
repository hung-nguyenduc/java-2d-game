package state;

import Dialogue.DialogueLine;
import Dialogue.DialogueManager;
import entity.Enemy;
import main.GamePanel;
import collision.Obstacle;
import collision.ObstacleManager;
import entity.Item;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class CongQuanSu extends GameState {
    private boolean debugMode = false;
    private static final String MAP_IMAGE_PATH = "/maps/cong-quansu-closed.png";
    private static final String OBSTACLE_TXT_PATH = "/maps/cong-quansu.txt";
    private static final double MAP_SCALE = 1.0 / 2.5; // Tỷ lệ thu phóng map
    private DialogueManager dialogueBox;
    private Image mapImage;
    private List<Obstacle> obstacles = new ArrayList<>();
    BufferedImage vuFace;
    BufferedImage doMimiFace;

    private boolean isQuestCompleted = false;
    private boolean isPhase2DialoguePlayed = false;
    private DialogueLine[] introScript;
    private DialogueLine[] afterQuestScript;

    private List<Item> questItems = new ArrayList<>();
    private int itemsCollected = 0;
    private final int TOTAL_QUEST_ITEMS = 1;
    private Item nearbyItem = null; // Lưu vật phẩm đang đứng gần để hiển thị UI
    private Rectangle doorRect = new Rectangle(450, 300, 154, 70);
    private boolean isNearDoor = false;
    public CongQuanSu(GamePanel gp) {
        super(gp);
        dialogueBox = new DialogueManager() {
            @Override
            public void onDialogueComplete() {
                // ĐÂY LÀ NƠI XỬ LÝ KHI ĐỌC HẾT THOẠI:
                // Ví dụ: Cho phép Vũ bước ra khỏi cổng Parabol hoặc đổi sang State tiếp theo luôn!
                // gp.setState(new StateKTX(gp));
            }
        };
    }

    @Override
    public void enter() {
        try {
            BufferedImage src = ImageIO.read(getClass().getResourceAsStream(MAP_IMAGE_PATH));
            gp.worldWidth = (int) (src.getWidth() * MAP_SCALE);
            gp.worldHeight = (int) (src.getHeight() * MAP_SCALE);

            // Tạo ảnh tương thích phần cứng để render mượt, chống giật lag
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

        // Nạp danh sách vật cản từ file text thông qua Manager
        this.obstacles = ObstacleManager.loadObstacles(OBSTACLE_TXT_PATH, MAP_SCALE);

        // Reset các thông số hệ thống và dọn dẹp thực thể cũ
        gp.killCount = 0;
        gp.player.health = 100;
        gp.player.bullets.clear();
        gp.enemies.clear();

        // Đặt vị trí xuất phát cho Player trong map mới này
        gp.player.worldX = 400; // Tọa độ X mong muốn
        gp.player.worldY = 400; // Tọa độ Y mong muốn
        //gp.player.spawnAtCenter();
        // Sinh quái (Enemy) riêng cho map này
        //spawnEnemies();

        // Bật nhạc nền riêng của màn này
        // gp.sound.playMusic("level3_theme");
        try {
            vuFace = ImageIO.read(getClass().getResourceAsStream("/player/down1.png"));
            doMimiFace = ImageIO.read(getClass().getResourceAsStream("/NPC/DoMiMi/DoMiMi-xoaphong.png"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        introScript = new DialogueLine[] {
                new DialogueLine("Kì thi giải tích kết thúc, Vũ đến với kì quân sự ở B7.", null),
                new DialogueLine("Vũ: Let's gooo", vuFace),

        };

        afterQuestScript = new DialogueLine[] {
                new DialogueLine("Vũ: Phù, đủ đồ rồi, lượn ra Parabol thôi!", vuFace),

        };

        // Bắt đầu luôn thoại phần 1
        dialogueBox.startDialogue(introScript);
    }

    @Override
    public List<Obstacle> getObstacles() {
        // Trả về danh sách vật cản để CollisionChecker bốc đầu ra xử lý va chạm tường
        return this.obstacles;
    }

    @Override
    public void update() {
        // Khóa toàn bộ game (hoặc chỉ Player) khi hội thoại đang chạy
        if (dialogueBox.isActive()) {
            if (gp.keyH.spacePressed) {
                dialogueBox.advanceDialogue();
                gp.keyH.spacePressed = false; // Reset phím ngay lập tức để tránh bị trôi chữ quá nhanh
            }
            return; // Đang nói chuyện thì không làm gì khác
        }
        // Reset biến nearbyItem mỗi frame trước khi check lại
        nearbyItem = null;
        isNearDoor = false;
        Rectangle playerRect = new Rectangle((int)gp.player.worldX, (int)gp.player.worldY, 32, 32);
        // ---------------------------------------------------------
        // LOGIC NHIỆM VỤ Ở ĐÂY:
        // Nếu chưa làm xong nhiệm vụ thì check xem nhặt đủ đồ chưa
        if (!isQuestCompleted) {
            // Lặp qua danh sách đồ vật đang rớt trên map
            for (int i = 0; i < questItems.size(); i++) {
                Item item = questItems.get(i);

                // Tạo hộp va chạm ảo cho Player và Item để check xem có đụng nhau không
                Rectangle itemRect = new Rectangle(item.worldX, item.worldY, item.solidArea.width, item.solidArea.height);

                // Trùng Hitbox -> Đang đứng trên vật phẩm
                if (playerRect.intersects(itemRect)) {
                    nearbyItem = item; // Đánh dấu là đang đứng gần món này

                    // NẾU ĐỨNG GẦN VÀ BẤM PHÍM F
                    if (gp.keyH.fPressed) {
                        System.out.println("Vũ đã nhặt được: " + item.name);
                        questItems.remove(i);
                        itemsCollected++;
                        gp.keyH.fPressed = false; // Bấm xong phải reset phím ngay tránh lỗi nhặt đúp
                        nearbyItem = null; // Nhặt rồi thì không còn đứng gần nữa
                        break; // Nhặt xong 1 món thì thoát vòng lặp frame này luôn
                    }
                }
            }

        }

        // LOGIC KIỂM TRA CỬA RA VÀO:
        if (playerRect.intersects(doorRect)) {
            isNearDoor = true;
            if (gp.keyH.fPressed) {
                    System.out.println("Qua màn!");
                    // CHUYỂN SANG MAP TIẾP THEO Ở ĐÂY. Thay Level2State bằng state mày muốn.
                    gp.setState(new LoadingState(gp, new ZombieState(gp)));
                gp.keyH.fPressed = false; // Reset phím F
            }
        }

        // Kích hoạt hội thoại phần 2 ngay khi nhiệm vụ xong (chỉ gọi 1 lần)
        if (isQuestCompleted && !isPhase2DialoguePlayed) {
            dialogueBox.startDialogue(afterQuestScript);
            isPhase2DialoguePlayed = true;
            return;
        }
        // ---------------------------------------------------------

        // Cập nhật logic nhân vật
        gp.player.update();

        // Cập nhật logic quái vật
//        for (int i = 0; i < gp.enemies.size(); i++) {
//            gp.enemies.get(i).update();
//        }

        // Gọi bộ kiểm tra va chạm tập trung (Giữa các thực thể và vật cản)
        gp.checkCollisions();

        // 8. Kiểm tra điều kiện Thua / Thắng để chuyển State
//        if (gp.player.health <= 0) {
//            gp.setState(new GameOverState(gp));
//            return;
//        }

//        if (gp.killCount >= 10) { // Ví dụ diệt đủ 10 quái thì qua màn tiếp
//            // gp.setState(new LevelCompleteState(gp, 3, new Level4State(gp)));
//        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // Tự động tính toán vị trí Camera dựa theo Player
        int cameraX = (int) (gp.player.worldX - gp.screenWidth / 2.0);
        int cameraY = (int) (gp.player.worldY - gp.screenHeight / 2.0);

        // Giới hạn camera không bị lọt ra ngoài rìa bản đồ
        int[] clamped = gp.clampCameraPosition(cameraX, cameraY);
        cameraX = clamped[0];
        cameraY = clamped[1];

        // --- TIẾN HÀNH VẼ THEO THỨ TỰ TẦNG (LAYER) ---

        // Tầng 1: Vẽ ảnh nền Map
        if (mapImage != null) {
            g2.drawImage(mapImage, 0, 0, gp.screenWidth, gp.screenHeight,
                    cameraX, cameraY, cameraX + gp.screenWidth, cameraY + gp.screenHeight, null);
        }

        // Tầng 2: Vẽ các khối vật cản (Để debug, nếu map chạy mượt rồi có thể ẩn đi)


        // Tầng 3: Vẽ các thực thể (Quái vật, Đạn, Checkpoint...)
//        for (Enemy enemy : gp.enemies) {
//            enemy.draw(g2, cameraX, cameraY);
//        }
        // TẦNG MỚI: Vẽ Item trước khi vẽ Player để Player có thể đè lên item
        for (Item item : questItems) {
            item.draw(g2, cameraX, cameraY);
        }

        // Tầng 4: Vẽ Nhân vật chính

        gp.player.draw(g2, cameraX, cameraY);


        // Tầng 5: Vẽ giao diện hiển thị (HUD) cố định trên màn hình (Máu, Số mạng đã giết...)
//        g2.setColor(Color.WHITE);
//        g2.setFont(new Font("Arial", Font.BOLD, 20));
//        g2.drawString("HP: " + gp.player.health, 20, 30);
//        g2.drawString("KILLS: " + gp.killCount, 20, 60);

        // HIỂN THỊ HƯỚNG DẪN NHẶT ĐỒ NẾU ĐANG ĐỨNG GẦN:
        if (nearbyItem != null && !isQuestCompleted) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            String text = "Nhấn F để nhặt " + nearbyItem.name;

            // Căn tọa độ để chữ hiện ngay trên đầu Vũ
            int textX = (int)gp.player.worldX - cameraX - 20;
            int textY = (int)gp.player.worldY - cameraY - 10;

            // Vẽ thêm cái viền đen mỏng cho chữ dễ đọc trên nền sáng
            g2.setColor(Color.BLACK);
            g2.drawString(text, textX + 1, textY + 1);
            g2.setColor(Color.WHITE);
            g2.drawString(text, textX, textY);
        }

        // 2. Hiển thị chữ ở cửa (nếu đang đứng gần cửa)
        if (isNearDoor) {
            String text = "Nhấn F để ra vào";
            // Hiển thị màu xanh nếu đủ đồ, màu đỏ nếu thiếu đồ
            Color textColor = Color.GREEN ;

            int textX = (int)gp.player.worldX - cameraX - 30;
            int textY = (int)gp.player.worldY - cameraY - 10;

            g2.setColor(Color.BLACK);
            g2.drawString(text, textX + 1, textY + 1);
            g2.setColor(textColor);
            g2.drawString(text, textX, textY);
        }
        if (debugMode) {
            for (Obstacle obs : obstacles) {
                obs.draw(g2, cameraX, cameraY);
            }
            g2.drawRect(doorRect.x - cameraX, doorRect.y - cameraY, doorRect.width, doorRect.height);
        }

        // Tùy chọn: Vẽ khung chữ nhật tàng hình của cửa để debug xem tọa độ đúng chưa (Sau khi khớp rồi thì xóa hoặc comment dòng này đi)
        // g2.setColor(new Color(255, 0, 0, 100)); // Màu đỏ trong suốt
        // g2.fillRect(doorRect.x - cameraX, doorRect.y - cameraY, doorRect.width, doorRect.height);

        dialogueBox.draw(g2, gp.screenWidth, gp.screenHeight);
    }

    @Override
    public void exit() {
        // Dọn dẹp tài nguyên khi rời màn chơi để tránh tràn bộ nhớ (RAM)
        gp.enemies.clear();
        obstacles.clear();
        // gp.sound.stopMusic();
    }

    @Override
    public void handleMouseClick(MouseEvent e) {
        // Xử lý các nút bấm đặc biệt trên màn hình nếu có (ví dụ nút Pause)
    }
}