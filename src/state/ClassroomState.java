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
    private boolean debugMode = false;
    private static final String MAP_IMAGE_PATH = "/maps/classroom4.png";
    private static final String OBSTACLE_TXT_PATH = "/maps/classroom_obstacles.txt";
    private static final double MAP_SCALE = 1.0 / 2.7; // Tỷ lệ thu phóng map
    private DialogueManager dialogueBox;
    private Image mapImage;
    private List<Obstacle> obstacles = new ArrayList<>();
    BufferedImage vuFace;
    BufferedImage thayGiaoFace;

    private Item mathBook;
    private boolean isNearBook = false;
    private boolean isQuizActive = false;
    private boolean isQuizFinished = false;
    private int currentQuestionIndex = 0;
    private int score = 0;

    // THÊM CÁC BIẾN NÀY DƯỚI CHỖ KHAI BÁO CỦA TRẮC NGHIỆM:
    private Image[] resultImages = new Image[6];
    private boolean showResultImage = false;

    // --- BIẾN ĐƯỢC THÊM MỚI ĐỂ XỬ LÝ TÁCH DIALOGUE ---
    private boolean isFinalDialogueTriggered = false;

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
            mathBook = new Item("Sách bài tập", "/items/calculus.png", 300, 380);
            for (int i = 0; i <= 5; i++) {
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
        gp.player.health = 100;
        gp.player.bullets.clear();
        gp.enemies.clear();

        gp.player.worldX = 130;
        gp.player.worldY = 250;

        // Reset cờ kích hoạt hội thoại cuối khi vào lại map
        isFinalDialogueTriggered = false;

        try {
            vuFace = ImageIO.read(getClass().getResourceAsStream("/player/down1.png"));
            thayGiaoFace = ImageIO.read(getClass().getResourceAsStream("/NPC/thay-giao-gt.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ĐÃ SỬA: Chỉ nạp và chạy 3 câu đầu tiên ở đây
        DialogueLine[] script1 = {
                new DialogueLine("Vũ: Em xin lỗi thầy em đến muộn ạ, xin phép thầy cho em vào lớp\n", vuFace),
                new DialogueLine("Thầy: Mới hôm đầu đi học mà đã muộn, lần sau muộn nữa\n tôi cho cậu trượt môn", thayGiaoFace),
                new DialogueLine("Vũ với quyết tâm A+ giải tích nên đã lên thẳng bàn đầu ngồi", null)
        };
        dialogueBox.startDialogue(script1);
    }

    private void submitAnswer(int selectedOptionIndex) {
        if (selectedOptionIndex == correctAnswers[currentQuestionIndex]) {
            score++;
        } else {
            gp.sound.playSE("nhac_tra_loi_sai"); // Tiếng báo trả lời sai
        }

        currentQuestionIndex++;

        gp.keyH.key1Pressed = false;
        gp.keyH.key2Pressed = false;
        gp.keyH.key3Pressed = false;
        gp.keyH.key4Pressed = false;

        if (currentQuestionIndex >= 5) {
            isQuizActive = false;
            showResultImage = true;
        }
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
            gp.setState(new LoadingState(gp, new CongQuanSu(gp)));
            return;
        }

        // Nếu đang hiện hội thoại (bất kể script 1 hay script 2) thì đóng băng logic di chuyển
        if (dialogueBox.isActive()) {
            dialogueBox.update();
            if (gp.keyH.spacePressed) {
                dialogueBox.advanceDialogue();
                gp.keyH.spacePressed = false;
            }
            return;
        }

        // ĐÃ THÊM: Check vị trí của Vũ xem đã đến gần bàn số 1 (100, 100) chưa
        // Sử dụng khoảng cách (Ví dụ trong bán kính 40px quanh điểm 100,100) để dễ kích hoạt
        if (!isFinalDialogueTriggered) {
            double distanceToDesk = Math.hypot(gp.player.worldX - 310, gp.player.worldY - 390);
            if (distanceToDesk <= 40) {
                isFinalDialogueTriggered = true; // Chặn không cho trigger lại lần 2

                // Gọi câu thoại cuối cùng
                DialogueLine[] script2 = {
                        new DialogueLine("Vừa ngồi vào bàn, Vũ đã phải chạm trán thử thách đầu tiên: \nlàm 5 câu fami sohoa", null)
                };
                dialogueBox.startDialogue(script2);
                return; // Ngắt update để ưu tiên hiển thị hộp thoại vừa bật
            }
        }

        // LOGIC KHI ĐANG HIỆN ẢNH KẾT QUẢ:
        if (showResultImage) {
            if (gp.keyH.spacePressed) {
                if (score >= 3) {
                    System.out.println("Qua môn! Chuyển map...");
                    gp.setState(new LoadingState(gp, new CongQuanSu(gp)));
                } else {
                    System.out.println("Trượt rồi, chơi lại!");
                    showResultImage = false;
                    score = 0;
                    currentQuestionIndex = 0;
                    gp.setState(new TachMonState(gp));
                }
                gp.keyH.spacePressed = false;
            }
            return;
        }

        // Nếu bảng câu hỏi đang mở -> Khóa di chuyển, chỉ check phím bấm trả lời
        if (isQuizActive) {
            if (gp.keyH.key1Pressed) submitAnswer(0);
            else if (gp.keyH.key2Pressed) submitAnswer(1);
            else if (gp.keyH.key3Pressed) submitAnswer(2);
            else if (gp.keyH.key4Pressed) submitAnswer(3);
            return;
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
    }

    @Override
    public void draw(Graphics2D g2) {
        int cameraX = (int) (gp.player.worldX - gp.screenWidth / 2.0);
        int cameraY = (int) (gp.player.worldY - gp.screenHeight / 2.0);

        int[] clamped = gp.clampCameraPosition(cameraX, cameraY);
        cameraX = clamped[0];
        cameraY = clamped[1];

        // Tầng 1: Vẽ ảnh nền Map
        if (mapImage != null) {
            g2.drawImage(mapImage, 0, 0, gp.screenWidth, gp.screenHeight,
                    cameraX, cameraY, cameraX + gp.screenWidth, cameraY + gp.screenHeight, null);
        }

        if (debugMode) {
            // Tầng 2: Vẽ các khối vật cản
            for (Obstacle obs : obstacles) {
                obs.draw(g2, cameraX, cameraY);
            }
        }


        if (!isQuizFinished && mathBook != null) {
            mathBook.draw(g2, cameraX, cameraY);
        }

        // Tầng 4: Vẽ Nhân vật chính
        gp.player.draw(g2, cameraX, cameraY);

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

        // Vẽ Dialogue Box lên trên cùng
        dialogueBox.draw(g2, gp.screenWidth, gp.screenHeight);

        // VẼ ẢNH KẾT QUẢ ĐÈ LÊN MÀN HÌNH CHÍNH
        if (showResultImage) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

            Image resultImg = resultImages[score];
            if (resultImg != null) {
                int imgWidth = 400;
                int imgHeight = 300;
                int imgX = (gp.screenWidth - imgWidth) / 2;
                int imgY = (gp.screenHeight - imgHeight) / 2;

                g2.drawImage(resultImg, imgX, imgY, imgWidth, imgHeight, null);

                g2.setFont(new Font("Arial", Font.BOLD, 18));
                g2.setColor(Color.WHITE);
                String guideText = (score >= 3) ? "Nhấn SPACE để qua map" : "Nhấn SPACE để thi lại";

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

        g2.setColor(new Color(0, 0, 0, 220));
        g2.fillRoundRect(x, y, windowWidth, windowHeight, 20, 20);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x, y, windowWidth, windowHeight, 20, 20);

        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.drawString(questions[currentQuestionIndex], x + 30, y + 50);

        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        for (int i = 0; i < 4; i++) {
            int optY = y + 120 + (i * 50);
            g2.setColor(new Color(255, 255, 255, 50));
            g2.fillRoundRect(x + 30, optY - 25, windowWidth - 60, 40, 10, 10);

            g2.setColor(Color.WHITE);
            g2.drawString(options[currentQuestionIndex][i], x + 40, optY);
        }

        g2.setFont(new Font("Arial", Font.ITALIC, 14));
        g2.setColor(Color.GRAY);
        g2.drawString("Sử dụng chuột click hoặc ấn phím 1, 2, 3, 4 để chọn", 30, 40);
    }

    @Override
    public void exit() {
        gp.enemies.clear();
        obstacles.clear();
    }

    @Override
    public void handleMouseClick(MouseEvent e) {
        if (isQuizActive) {
            int mx = e.getX();
            int my = e.getY();

            int windowWidth = 600;
            int windowHeight = 350;
            int x = (gp.screenWidth - windowWidth) / 2;
            int y = (gp.screenHeight - windowHeight) / 2;

            for (int i = 0; i < 4; i++) {
                int optY = y + 120 + (i * 50);
                Rectangle optionBox = new Rectangle(x + 30, optY - 25, windowWidth - 60, 40);

                if (optionBox.contains(mx, my)) {
                    submitAnswer(i);
                    break;
                }
            }
        }
    }
}