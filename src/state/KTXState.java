package state;

import Dialogue.DialogueLine;
import Dialogue.DialogueManager;
import main.GamePanel;
import collision.Obstacle;
import collision.ObstacleManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class KTXState extends GameState {
    private static final String MAP_IMAGE_PATH = "/maps/ktx1.png";
    private static final String OBSTACLE_TXT_PATH = "/maps/ktx_obstacles.txt";
    private static final double MAP_SCALE = 1.0 / 2.5; // Tỷ lệ thu phóng map
    private DialogueManager dialogueBox;
    private Image mapImage;
    private List<Obstacle> obstacles = new ArrayList<>();
    BufferedImage vuFace;
    BufferedImage doMimiFace;
    public KTXState(GamePanel gp) {
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
        gp.player.health = 100; // Reset máu player (hoặc giữ nguyên tùy logic game)
        gp.player.bullets.clear();
        gp.enemies.clear();

        // Đặt vị trí xuất phát cho Player trong map mới này
//        gp.player.worldX = 100; // Tọa độ X mong muốn
//        gp.player.worldY = 100; // Tọa độ Y mong muốn
        gp.player.spawnAtCenter();
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

        DialogueLine[] script = {
                new DialogueLine("Giới thiệu nhân vật:\nĐây là Vũ, tân sinh viên Bách Khoa K36.", vuFace),
                new DialogueLine("Vũ tự tin bước vào trường với ước mơ ra trường đúng hạn\nvà trở thành một kỹ sư tài ba.", vuFace),
                new DialogueLine("Vũ đang ngủ ở kí túc xá, ngáy khò khò", vuFace),
                new DialogueLine("Độ Mimi: Alo Vũ à Vũ?", doMimiFace),
                new DialogueLine("Độ Mimi: Ôi em ơi, số điện thoại, địa chỉ nhà\nanh đều có ở đây hết rồi, em đừng có chối!", doMimiFace),
                new DialogueLine("Vũ: Ơ anh nhầm người rồi...", vuFace),
                new DialogueLine("Độ Mimi: Thế em có định đi học giải tích ko?", doMimiFace),
                new DialogueLine("Vũ: Ôi thôi chết quên mẹ giờ học rồi, phải đi ngay thôi!", vuFace)
        };
        dialogueBox.startDialogue(script);
//        String[] script = {
//                "Giới thiệu nhân vật:\nĐây là Vũ, tân sinh viên Bách Khoa K36.",
//                "Vũ tự tin bước vào trường với ước mơ ra trường đúng hạn\nvà trở thành một kỹ sư tài ba.",
//                "(Chuyển cảnh sang năm thứ nhất)\nBối cảnh: Kí túc xá, Vũ đang ngủ ngáy khò khò...",
//                "Độ Mimi: Alo Vũ à Vũ? Ôi em ơi, số điện thoại, địa chỉ nhà\nanh đều có ở đây hết rồi, em đừng có chối!",
//                "Vũ: Ơ anh nhầm người rồi...",
//                "Độ Mimi: Thế em có định đi học giải tích ko?",
//                "Vũ: Ôi thôi chết quên mẹ giờ học rồi, phải đi ngay thôi!"
//        };
//        dialogueBox.startDialogue(script);
    }

    private void spawnEnemies() {
        // Thêm quái vào danh sách, thay đổi tọa độ tùy ý bạn
        // gp.enemies.add(new Enemy(gp, 500, 400));
        // gp.enemies.add(new Enemy(gp, 1200, 800));
    }

    @Override
    public List<Obstacle> getObstacles() {
        // Trả về danh sách vật cản để CollisionChecker bốc đầu ra xử lý va chạm tường
        return this.obstacles;
    }

    @Override
    public void update() {
        // Nếu đang hiện hội thoại thì đóng băng quái vật hoặc đóng băng di chuyển của Player lại
        if (dialogueBox.isActive()) {
            // Chỉ cập nhật hiệu ứng chữ, không cho Player chạy đi đâu hết
            // Nếu bạn dùng KeyHandler chung, hãy check điều kiện này để chặn di chuyển của Vũ nhé!

            // Xử lý lắng nghe phím Enter chuyển dòng từ KeyHandler của bạn
            if (gp.keyH.spacePressed) {
                dialogueBox.advanceDialogue();
                gp.keyH.spacePressed = false; // Reset phím ngay lập tức để tránh bị trôi chữ quá nhanh
            }
            return;
        }
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
        for (Obstacle obs : obstacles) {
            obs.draw(g2, cameraX, cameraY);
        }

        // Tầng 3: Vẽ các thực thể (Quái vật, Đạn, Checkpoint...)
//        for (Enemy enemy : gp.enemies) {
//            enemy.draw(g2, cameraX, cameraY);
//        }

        // Tầng 4: Vẽ Nhân vật chính
        if (!dialogueBox.isActive()) {
            gp.player.draw(g2, cameraX, cameraY);
        }

        // Tầng 5: Vẽ giao diện hiển thị (HUD) cố định trên màn hình (Máu, Số mạng đã giết...)
//        g2.setColor(Color.WHITE);
//        g2.setFont(new Font("Arial", Font.BOLD, 20));
//        g2.drawString("HP: " + gp.player.health, 20, 30);
//        g2.drawString("KILLS: " + gp.killCount, 20, 60);

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