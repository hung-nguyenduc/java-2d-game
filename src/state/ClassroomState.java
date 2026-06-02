package state;

import Dialogue.DialogueLine;
import Dialogue.DialogueManager;
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

public class ClassroomState extends GameState {
    private static final String MAP_IMAGE_PATH = "/maps/classroom.png";
    private static final String OBSTACLE_TXT_PATH = "/maps/classroom_obstacles.txt";
    private static final double MAP_SCALE = 1.0 / 2.5; // Tỷ lệ thu phóng map
    private DialogueManager dialogueBox;
    private Image mapImage;
    private List<Obstacle> obstacles = new ArrayList<>();
    BufferedImage vuFace;
    BufferedImage doMimiFace;

    private Item mathBook;
    private boolean isNearBook = false;
    private boolean isQuizActive = false;
    private boolean isQuizFinished = false;
    private int currentQuestionIndex = 0;
    private int score = 0;

    // THÊM CÁC BIẾN NÀY DƯỚI CHỖ KHAI BÁO CỦA TRẮC NGHIỆM:
    private Image[] resultImages = new Image[6];
    private boolean showResultImage = false;

    private String[] questions = {
            "Câu 1: Đạo hàm của sin(x) là gì?",
            "Câu 2: Tích phân của 2x dx là gì?",
            "Câu 3: Giới hạn lim(x->0) (sin x)/x bằng mấy?",
            "Câu 4: Đạo hàm của e^x là gì?",
            "Câu 5: Chuỗi số sum(1/n) hội tụ hay phân kỳ?"
    };

    private String[][] options = {
            {"1. cos(x)", "2. -cos(x)", "3. sin(x)", "4. -sin(x)"},
            {"1. x^2", "2. x^2 + C", "3. 2", "4. x"},
            {"1. 0", "2. 1", "3. Vô cực", "4. Không tồn tại"},
            {"1. e^x", "2. x*e^(x-1)", "3. ln(x)", "4. 1"},
            {"1. Hội tụ", "2. Phân kỳ", "3. Hội tụ tuyệt đối", "4. Dao động"}
    };

    // Vị trí đáp án đúng (từ 0 đến 3)
    private int[] correctAnswers = {0, 1, 1, 0, 1};

    public ClassroomState(GamePanel gp) {
        super(gp);
        dialogueBox = new DialogueManager() {
            @Override
            public void onDialogueComplete() {
                // Xử lý khi hội thoại báo kết quả kết thúc
                if (isQuizFinished && score >= 3) {
                    System.out.println("Chuyển map tiếp theo...");
                    // gp.setState(new NextState(gp));
                } else if (isQuizFinished && score < 3) {
                    System.out.println("Trượt rồi, chơi lại!");
                    // Reset lại để cho thi lại hoặc chuyển sang GameOverState
                    isQuizFinished = false;
                    score = 0;
                    currentQuestionIndex = 0;
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

            // Tạo ảnh tương thích phần cứng để render mượt, chống giật lag
            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            BufferedImage compatibleMap = gc.createCompatibleImage(gp.worldWidth, gp.worldHeight, Transparency.OPAQUE);

            Graphics2D g2d = compatibleMap.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(src, 0, 0, gp.worldWidth, gp.worldHeight, null);
            g2d.dispose();

            mapImage = compatibleMap;
            mathBook = new Item("Sách bài tập", "/items/calculus.png", gp.worldWidth / 2 - 100, gp.worldHeight / 2 - 50);
            for (int i = 0; i <= 5; i++) {
                // Thay đổi đường dẫn "/ui/" cho đúng với thư mục chứa ảnh của mày
                resultImages[i] = ImageIO.read(getClass().getResourceAsStream("/congratulations/" + i + ".png"));
            }
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

        //gp.player.spawnAtCenter();
        gp.player.worldX = gp.worldWidth / 2.0 - 40 -220;
        gp.player.worldY = gp.worldHeight / 2.0 - 40 +100;

        // Bật nhạc nền riêng của màn này
        // gp.sound.playMusic("level3_theme");
        try {
            vuFace = ImageIO.read(getClass().getResourceAsStream("/player/down1.png"));
            doMimiFace = ImageIO.read(getClass().getResourceAsStream("/NPC/DoMiMi/DoMiMi-xoaphong.png"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        DialogueLine[] script = {
                new DialogueLine("Đến giảng đường, Vũ với quyết tâm A+ giải tích nên đã \nlên thẳng bàn đầu ngồi", null),
                new DialogueLine("Vừa ngồi vào bản, Vũ đã phải chạm trán thử thách đầu tiên: \nlàm 5 câu fami sohoa", null)
        };
        dialogueBox.startDialogue(script);
    }

    private void submitAnswer(int selectedOptionIndex) {
        if (selectedOptionIndex == correctAnswers[currentQuestionIndex]) {
            score++;
        }

        currentQuestionIndex++;

        gp.keyH.key1Pressed = false;
        gp.keyH.key2Pressed = false;
        gp.keyH.key3Pressed = false;
        gp.keyH.key4Pressed = false;

        // Nếu đã làm hết 5 câu
        if (currentQuestionIndex >= 5) {
            isQuizActive = false;
            showResultImage = true; // BẬT CỜ HIỂN THỊ ẢNH KẾT QUẢ
        }
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
        // LOGIC KHI ĐANG HIỆN ẢNH KẾT QUẢ:
        if (showResultImage) {
            if (gp.keyH.spacePressed) {
                if (score >= 3) {
                    System.out.println("Qua môn! Chuyển map...");
                    // CHUYỂN MAP Ở ĐÂY:
                    gp.setState(new ZombieState(gp));
                } else {
                    System.out.println("Trượt rồi, chơi lại!");
                    // Reset lại điểm và câu hỏi để thi lại
                    showResultImage = false;
                    score = 0;
                    currentQuestionIndex = 0;
                    gp.setState(new TachMonState(gp));
                }
                gp.keyH.spacePressed = false; // Xóa phím để tránh dính đúp
            }
            return; // Khóa game, không cho chạy vòng vòng khi đang xem điểm
        }
        // Nếu bảng câu hỏi đang mở -> Khóa di chuyển, chỉ check phím bấm trả lời
        if (isQuizActive) {
            if (gp.keyH.key1Pressed) submitAnswer(0);
            else if (gp.keyH.key2Pressed) submitAnswer(1);
            else if (gp.keyH.key3Pressed) submitAnswer(2);
            else if (gp.keyH.key4Pressed) submitAnswer(3);
            return; // Khóa di chuyển
        }
        isNearBook = false;
        Rectangle playerRect = new Rectangle((int)gp.player.worldX, (int)gp.player.worldY, 32, 32);

        // Kiểm tra đứng gần sách chưa thi xong
        if (!showResultImage && mathBook != null) {
            Rectangle bookRect = new Rectangle(mathBook.worldX, mathBook.worldY, mathBook.solidArea.width, mathBook.solidArea.height);
            if (playerRect.intersects(bookRect)) {
                isNearBook = true;

                // Ấn F để bắt đầu làm bài
                if (gp.keyH.fPressed) {
                    isQuizActive = true;
                    gp.keyH.fPressed = false;
                }
            }
        }

        // Cập nhật logic nhân vật
        gp.player.update();

        // Check va chạm
        gp.checkCollisions();

        // Kiểm tra điều kiện chuyển state

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
        if (!isQuizFinished && mathBook != null) {
            mathBook.draw(g2, cameraX, cameraY);
        }
        // Tầng 4: Vẽ Nhân vật chính
        if (!dialogueBox.isActive()) {
            gp.player.draw(g2, cameraX, cameraY);
        }
        // HIỂN THỊ CHỮ NHẤN F KHI ĐỨNG GẦN SÁCH
        if (isNearBook && !isQuizActive && !isQuizFinished) {
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            String text = "Nhấn F để làm Fami Sohoa";
            int textX = (int)gp.player.worldX - cameraX - 30;
            int textY = (int)gp.player.worldY - cameraY - 10;
            g2.setColor(Color.BLACK);
            g2.drawString(text, textX + 1, textY + 1);
            g2.setColor(Color.YELLOW);
            g2.drawString(text, textX, textY);
        }
        // VẼ GIAO DIỆN TRẮC NGHIỆM
        if (isQuizActive) {
            drawQuizUI(g2);
        }


        // Tầng 5: Vẽ giao diện hiển thị (HUD) cố định trên màn hình (Máu, Số mạng đã giết...)
//        g2.setColor(Color.WHITE);
//        g2.setFont(new Font("Arial", Font.BOLD, 20));
//        g2.drawString("HP: " + gp.player.health, 20, 30);
//        g2.drawString("KILLS: " + gp.killCount, 20, 60);

        dialogueBox.draw(g2, gp.screenWidth, gp.screenHeight);
        // VẼ ẢNH KẾT QUẢ ĐÈ LÊN MÀN HÌNH CHÍNH
        if (showResultImage) {
            // Làm mờ nền đi một chút cho nó giống popup
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

            Image resultImg = resultImages[score]; // Lấy đúng ảnh theo số điểm
            if (resultImg != null) {
                // Kích thước cái bảng kết quả (tùy chỉnh cho vừa mắt)
                int imgWidth = 400;
                int imgHeight = 300;
                int imgX = (gp.screenWidth - imgWidth) / 2;
                int imgY = (gp.screenHeight - imgHeight) / 2;

                g2.drawImage(resultImg, imgX, imgY, imgWidth, imgHeight, null);

                // Dòng chữ nhấp nháy hướng dẫn ấn Space
                g2.setFont(new Font("Arial", Font.BOLD, 18));
                g2.setColor(Color.WHITE);
                String guideText = (score >= 3) ? "Nhấn SPACE để qua map" : "Nhấn SPACE để thi lại";

                // Căn giữa dòng chữ
                FontMetrics fm = g2.getFontMetrics();
                int textX = (gp.screenWidth - fm.stringWidth(guideText)) / 2;
                int textY = imgY + imgHeight + 40;

                g2.drawString(guideText, textX, textY);
            }
        }
    }
    private void drawQuizUI(Graphics2D g2) {
        int windowWidth = 600;
        int windowHeight = 350;
        int x = (gp.screenWidth - windowWidth) / 2;
        int y = (gp.screenHeight - windowHeight) / 2;

        // Vẽ khung đen mờ
        g2.setColor(new Color(0, 0, 0, 220));
        g2.fillRoundRect(x, y, windowWidth, windowHeight, 20, 20);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x, y, windowWidth, windowHeight, 20, 20);

        // Vẽ câu hỏi
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.drawString(questions[currentQuestionIndex], x + 30, y + 50);

        // Vẽ các đáp án
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        for (int i = 0; i < 4; i++) {
            // Tọa độ y của từng đáp án giãn cách nhau 50px
            int optY = y + 120 + (i * 50);

            // Vẽ hộp bao quanh đáp án để có cảm giác click được
            g2.setColor(new Color(255, 255, 255, 50));
            g2.fillRoundRect(x + 30, optY - 25, windowWidth - 60, 40, 10, 10);

            g2.setColor(Color.WHITE);
            g2.drawString(options[currentQuestionIndex][i], x + 40, optY);
        }

        // Hướng dẫn nhỏ
        g2.setFont(new Font("Arial", Font.ITALIC, 14));
        g2.setColor(Color.GRAY);
        g2.drawString("Sử dụng chuột click hoặc ấn phím 1, 2, 3, 4 để chọn", 30, 40);
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
        // Chỉ xử lý click chuột khi Quiz đang bật
        if (isQuizActive) {
            int mx = e.getX();
            int my = e.getY();

            int windowWidth = 600;
            int windowHeight = 350;
            int x = (gp.screenWidth - windowWidth) / 2;
            int y = (gp.screenHeight - windowHeight) / 2;

            for (int i = 0; i < 4; i++) {
                int optY = y + 120 + (i * 50);
                // Tạo một cái khung chữ nhật ảo khớp với giao diện đáp án tao vẽ ở trên
                Rectangle optionBox = new Rectangle(x + 30, optY - 25, windowWidth - 60, 40);

                // Nếu chuột click trúng vào khung chữ nhật đó
                if (optionBox.contains(mx, my)) {
                    submitAnswer(i);
                    break;
                }
            }
        }
    }
}