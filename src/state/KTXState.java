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

public class KTXState extends GameState {
    private static final String MAP_IMAGE_PATH = "/maps/ktx.png";
    private static final String OBSTACLE_TXT_PATH = "/maps/ktx_obstacles.txt";
    private static final double MAP_SCALE = 1.0 / 2.5;

    private DialogueManager dialogueBox;
    private Image mapImage;
    private List<Obstacle> obstacles = new ArrayList<>();
    private BufferedImage vuFace;
    private BufferedImage doMimiFace;

    private boolean isQuestCompleted = false;
    private boolean isPhase2DialoguePlayed = false;
    private DialogueLine[] introScript;
    private DialogueLine[] afterQuestScript;

    private List<Item> questItems = new ArrayList<>();
    private int itemsCollected = 0;
    private final int TOTAL_QUEST_ITEMS = 3;
    private Item nearbyItem = null;
    private Rectangle doorRect = new Rectangle(337, 655, 64, 64);
    private boolean isNearDoor = false;

    // Kích thước và bán kính
    private final int PLAYER_SIZE = 48;
    private final double PLAYER_RADIUS = 24;
    private final double DOOR_RADIUS = 50;

    public KTXState(GamePanel gp) {
        super(gp);
        dialogueBox = new DialogueManager() {
            @Override
            public void onDialogueComplete() {
                // Xử lý khi hết thoại
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

        gp.killCount = 0;
        gp.player.health = 100;
        gp.player.bullets.clear();
        gp.enemies.clear();

        gp.player.spawnAtCenter();

        try {
            vuFace = ImageIO.read(getClass().getResourceAsStream("/player/down1.png"));
            doMimiFace = ImageIO.read(getClass().getResourceAsStream("/NPC/DoMiMi/DoMiMi-xoaphong.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        introScript = new DialogueLine[] {
                new DialogueLine("Giới thiệu nhân vật:\nĐây là Vũ, tân sinh viên Bách Khoa K36.", vuFace),
                new DialogueLine("Vũ tự tin bước vào trường với ước mơ ra trường đúng hạn\nvà trở thành một kỹ sư tài ba.", vuFace),
                new DialogueLine("Vũ đang ngủ ở kí túc xá, ngáy khò khò", vuFace),
                new DialogueLine("Độ Mimi: Alo Vũ à Vũ?", doMimiFace),
                new DialogueLine("Độ Mimi: Ôi em ơi, số điện thoại, địa chỉ nhà\nanh đều có ở đây hết rồi, em đừng có chối!", doMimiFace),
                new DialogueLine("Vũ: Ơ anh nhầm người rồi...", vuFace),
                new DialogueLine("Độ Mimi: Thế em có định đi học giải tích ko?", doMimiFace),
                new DialogueLine("Vũ: Ôi thôi chết quên mẹ giờ học rồi, phải đi ngay thôi!", vuFace),
                new DialogueLine("Nhiệm vụ: thu thập cặp sách, sách giải tích, hộp bút để đi học", null)
        };

        afterQuestScript = new DialogueLine[] {
                new DialogueLine("Vũ: Phù, đủ đồ rồi, lượn ra Parabol thôi!", vuFace),
        };

        // Khởi tạo vật phẩm
        questItems.clear();
        questItems.add(new Item("Cặp sách", "/items/backpack.png", 540, 640, 85));
        questItems.add(new Item("Sách giải tích", "/items/calculus.png", 460, 347, 80));
        questItems.add(new Item("Hộp bút", "/items/pencilcase.png", 150, 242, 40));

        itemsCollected = 0;

        dialogueBox.startDialogue(introScript);
    }

    @Override
    public List<Obstacle> getObstacles() {
        return this.obstacles;
    }

    // ============= PHẦN TÍNH TOÁN NHẶT ĐỒ HÌNH TRÒN =============

    // Tính khoảng cách giữa player và item
    private double getDistanceToItem(Item item, double playerCenterX, double playerCenterY) {
        double itemCenterX = item.worldX + PLAYER_SIZE / 2.0;
        double itemCenterY = item.worldY + PLAYER_SIZE / 2.0;
        double dx = itemCenterX - playerCenterX;
        double dy = itemCenterY - playerCenterY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Kiểm tra xem player có đứng gần item không
    private boolean isNearItem(Item item, double playerCenterX, double playerCenterY) {
        double distance = getDistanceToItem(item, playerCenterX, playerCenterY);
        return distance <= (item.radius + PLAYER_RADIUS);
    }

    // Kiểm tra xem player có đứng gần cửa không
    private boolean isNearDoor(double playerCenterX, double playerCenterY) {
        double doorCenterX = doorRect.x + doorRect.width / 2.0;
        double doorCenterY = doorRect.y + doorRect.height / 2.0;
        double dx = doorCenterX - playerCenterX;
        double dy = doorCenterY - playerCenterY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= (DOOR_RADIUS + PLAYER_RADIUS);
    }

    // Xử lý nhặt đồ
    private void handlePickup() {
        for (int i = 0; i < questItems.size(); i++) {
            Item item = questItems.get(i);

            // Tính tâm player
            double playerCenterX = gp.player.worldX + PLAYER_SIZE / 2.0;
            double playerCenterY = gp.player.worldY + PLAYER_SIZE / 2.0;

            if (isNearItem(item, playerCenterX, playerCenterY)) {
                nearbyItem = item;

                if (gp.keyH.fPressed) {
                    System.out.println("Đã nhặt: " + item.name);
                    questItems.remove(i);
                    itemsCollected++;
                    gp.keyH.fPressed = false;
                    nearbyItem = null;
                    break;
                }
            }
        }
    }

    // ============= KẾT THÚC PHẦN TÍNH TOÁN =============

    @Override
    public void update() {
        // Khóa game khi đang hội thoại
        if (dialogueBox.isActive()) {
            if (gp.keyH.spacePressed) {
                dialogueBox.advanceDialogue();
                gp.keyH.spacePressed = false;
            }
            return;
        }

        nearbyItem = null;
        isNearDoor = false;

        // Tính tâm player
        double playerCenterX = gp.player.worldX + PLAYER_SIZE / 2.0;
        double playerCenterY = gp.player.worldY + PLAYER_SIZE / 2.0;

        // LOGIC NHẶT ĐỒ
        if (!isQuestCompleted) {
            handlePickup();  // Gọi hàm xử lý nhặt đồ

            if (itemsCollected >= TOTAL_QUEST_ITEMS) {
                isQuestCompleted = true;
            }
        }

        // LOGIC CỬA RA
        if (isNearDoor(playerCenterX, playerCenterY)) {
            isNearDoor = true;

            if (gp.keyH.fPressed) {
                if (isQuestCompleted) {
                    System.out.println("Qua màn!");
                    gp.setState(new LoadingState(gp, new ClassroomState(gp)));
                } else {
                    System.out.println("Chưa thu thập đủ đồ!");
                }
                gp.keyH.fPressed = false;
            }
        }

        // Kích hoạt hội thoại phần 2
        if (isQuestCompleted && !isPhase2DialoguePlayed) {
            dialogueBox.startDialogue(afterQuestScript);
            isPhase2DialoguePlayed = true;
            return;
        }

        // Cập nhật player
        gp.player.update();

        // Gọi bộ kiểm tra va chạm
        gp.checkCollisions();
    }

    @Override
    public void draw(Graphics2D g2) {
        // Tính toán camera
        int cameraX = (int) (gp.player.worldX - gp.screenWidth / 2.0);
        int cameraY = (int) (gp.player.worldY - gp.screenHeight / 2.0);

        int[] clamped = gp.clampCameraPosition(cameraX, cameraY);
        cameraX = clamped[0];
        cameraY = clamped[1];

        // Tầng 1: Vẽ map
        if (mapImage != null) {
            g2.drawImage(mapImage, 0, 0, gp.screenWidth, gp.screenHeight,
                    cameraX, cameraY, cameraX + gp.screenWidth, cameraY + gp.screenHeight, null);
        }

        // Tầng 2: Vẽ vật cản (debug)
        for (Obstacle obs : obstacles) {
            obs.draw(g2, cameraX, cameraY);
        }

        // Tầng 3: Vẽ Item
        for (Item item : questItems) {
            item.draw(g2, cameraX, cameraY);
        }

        // Tầng 4: Vẽ Player
        gp.player.draw(g2, cameraX, cameraY);

        // HIỂN THỊ HUD NHẶT ĐỒ
        if (nearbyItem != null && !isQuestCompleted) {
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            String text = "Nhấn F để nhặt " + nearbyItem.name;

            int textX = (int) gp.player.worldX - cameraX - 20;
            int textY = (int) gp.player.worldY - cameraY - 10;

            g2.setColor(Color.BLACK);
            g2.drawString(text, textX + 1, textY + 1);
            g2.setColor(Color.WHITE);
            g2.drawString(text, textX, textY);
        }

        // HIỂN THỊ HUD CỬA
        if (isNearDoor) {
            String text = isQuestCompleted ? "Nhấn F để ra ngoài" : "Cần thu thập đủ đồ trước!";
            Color textColor = isQuestCompleted ? Color.GREEN : Color.RED;

            int textX = (int) gp.player.worldX - cameraX - 30;
            int textY = (int) gp.player.worldY - cameraY - 10;

            g2.setColor(Color.BLACK);
            g2.drawString(text, textX + 1, textY + 1);
            g2.setColor(textColor);
            g2.drawString(text, textX, textY);
        }

        // Vẽ dialogue box
        dialogueBox.draw(g2, gp.screenWidth, gp.screenHeight);
    }

    @Override
    public void exit() {
        gp.enemies.clear();
        obstacles.clear();
    }

    @Override
    public void handleMouseClick(MouseEvent e) {
        // Xử lý click chuột nếu cần
    }
}