package state;

import main.GamePanel;
import collision.Obstacle;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

/**
 * MyRoomState - Phòng ký túc xá của Vũ.
 * Sử dụng background_myroom.png làm nền, vẽ đồ nội thất bằng sprite PNG,
 * có vật phẩm nhặt được (điện thoại, laptop, mì tôm) và cửa thoát ra OpenWorld.
 */
public class MyRoomState extends GameState {

    // Ảnh nền phòng
    private Image roomBackground;
    private int roomW, roomH;

    // Sprites đồ nội thất
    private BufferedImage bedImg, pcImg, tableImg, fridgeImg, bookshelfImg,
            wardrobeImg, kitchenImg, phoneImg, laptopImg, noodleImg;

    // Obstacles (đồ nội thất cứng, không đi xuyên được)
    private List<Obstacle> obstacles = new ArrayList<>();

    // Vật phẩm nhặt được
    private static class PickupItem {
        String name;
        String description;
        BufferedImage image;
        int x, y, w, h;
        boolean collected = false;

        PickupItem(String name, String desc, BufferedImage img, int x, int y, int w, int h) {
            this.name = name;
            this.description = desc;
            this.image = img;
            this.x = x; this.y = y; this.w = w; this.h = h;
        }
    }
    private List<PickupItem> pickupItems = new ArrayList<>();

    // Cửa thoát
    private Rectangle doorRect;

    // HUD message
    private String hudMessage = "";
    private int hudMessageTimer = 0;

    // Interaction prompt
    private String interactPrompt = "";

    public MyRoomState(GamePanel gp) {
        super(gp);
    }

    @Override
    public void enter() {
        // Load ảnh nền phòng
        try {
            BufferedImage src = ImageIO.read(getClass().getResourceAsStream("/maps/ref/background_myroom.png"));
            // Scale cho vừa màn hình game (~750x420)
            roomW = gp.screenWidth;
            roomH = gp.screenHeight;

            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            BufferedImage compat = gc.createCompatibleImage(roomW, roomH, Transparency.OPAQUE);
            Graphics2D g = compat.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(src, 0, 0, roomW, roomH, null);
            g.dispose();
            roomBackground = compat;
        } catch (Exception e) {
            System.err.println("Lỗi load background_myroom.png");
            e.printStackTrace();
        }

        // World size = room size (camera cố định)
        gp.worldWidth = roomW;
        gp.worldHeight = roomH;

        // Load furniture sprites
        bedImg = loadSprite("/tile/my_bed.png");
        pcImg = loadSprite("/tile/dan_pc.png");
        tableImg = loadSprite("/tile/table_my_room.png");
        fridgeImg = loadSprite("/tile/tu_lanh.png");
        bookshelfImg = loadSprite("/tile/tu_do_my_room.png");
        wardrobeImg = loadSprite("/tile/tu_quan_ao.png");
        kitchenImg = loadSprite("/tile/cho_nau_an.png");
        phoneImg = loadSprite("/tile/phone.png");
        laptopImg = loadSprite("/tile/lap_top.png");
        noodleImg = loadSprite("/tile/mi_tom.png");

        // Reset player
        gp.player.bullets.clear();
        gp.enemies.clear();

        // Đặt player ở giữa phòng
        gp.player.worldX = roomW / 2.0;
        gp.player.worldY = roomH * 0.7;

        // === BỐ TRÍ ĐỒ NỘI THẤT ===
        obstacles.clear();
        pickupItems.clear();

        // Giường - góc trái dưới
        obstacles.add(new Obstacle(30, roomH - 200, 130, 80, new Color(0,0,0,0)));

        // Bàn PC - giữa trái
        obstacles.add(new Obstacle(200, roomH - 220, 140, 90, new Color(0,0,0,0)));

        // Bàn ăn + ghế - giữa phải
        obstacles.add(new Obstacle(420, roomH - 190, 100, 80, new Color(0,0,0,0)));

        // Tủ lạnh - góc phải trên
        obstacles.add(new Obstacle(roomW - 120, 100, 70, 110, new Color(0,0,0,0)));

        // Tủ sách - giữa trên
        obstacles.add(new Obstacle(140, 80, 120, 70, new Color(0,0,0,0)));

        // Tủ quần áo - góc trái trên
        obstacles.add(new Obstacle(10, 60, 80, 110, new Color(0,0,0,0)));

        // Bếp - góc phải dưới
        obstacles.add(new Obstacle(roomW - 110, roomH - 230, 70, 200, new Color(0,0,0,0)));

        // Cửa phòng (không phải obstacle, là portal)
        doorRect = new Rectangle(roomW / 2 - 35, 60, 70, 90);

        // === VẬT PHẨM NHẶT ĐƯỢC ===
        // Điện thoại trên bàn PC
        pickupItems.add(new PickupItem("iPhone 100 ProMax", "Vũ khí đánh bại Ma Vương",
                phoneImg, 240, roomH - 230, 30, 30));

        // Laptop trên giường
        pickupItems.add(new PickupItem("Acer Predator 21X", "230.000.000 VNĐ",
                laptopImg, 60, roomH - 210, 40, 30));

        // Mì tôm cạnh tủ lạnh
        pickupItems.add(new PickupItem("Mì tôm Omachi", "Thức ăn sinh viên huyền thoại",
                noodleImg, roomW - 130, 220, 30, 30));
    }

    private BufferedImage loadSprite(String path) {
        try {
            return ImageIO.read(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.err.println("Lỗi load sprite: " + path);
            return null;
        }
    }

    @Override
    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    @Override
    public void update() {
        gp.player.update();
        gp.checkCollisions();

        // Giảm timer HUD message
        if (hudMessageTimer > 0) hudMessageTimer--;

        // Kiểm tra player đến cửa → teleport về OpenWorld
        Rectangle playerRect = new Rectangle((int) gp.player.worldX, (int) gp.player.worldY,
                gp.tileSize, gp.tileSize);

        // Prompt khi gần cửa
        interactPrompt = "";
        if (playerRect.intersects(doorRect)) {
            interactPrompt = "Nhấn SPACE để ra ngoài";
            if (gp.keyH.spacePressed) {
                gp.keyH.spacePressed = false;
                gp.setState(new OpenWorldState(gp));
                return;
            }
        }

        // Kiểm tra nhặt vật phẩm (tự động khi đi gần)
        for (PickupItem item : pickupItems) {
            if (!item.collected) {
                Rectangle itemRect = new Rectangle(item.x, item.y, item.w, item.h);
                if (playerRect.intersects(itemRect)) {
                    item.collected = true;
                    // Thêm vào inventory
                    gp.player.addInventoryItem(item.name, item.description, item.image);
                    hudMessage = "Đã nhặt: " + item.name;
                    hudMessageTimer = 120; // 2 giây
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // Vẽ nền phòng
        if (roomBackground != null) {
            g2.drawImage(roomBackground, 0, 0, null);
        }

        // Vẽ đồ nội thất (sprites)
        drawFurniture(g2, bedImg, 30, roomH - 200, 130, 80);
        drawFurniture(g2, pcImg, 200, roomH - 220, 140, 90);
        drawFurniture(g2, tableImg, 420, roomH - 190, 100, 80);
        drawFurniture(g2, fridgeImg, roomW - 120, 100, 70, 110);
        drawFurniture(g2, bookshelfImg, 140, 80, 120, 70);
        drawFurniture(g2, wardrobeImg, 10, 60, 80, 110);
        drawFurniture(g2, kitchenImg, roomW - 110, roomH - 230, 70, 200);

        // Vẽ cửa (highlight)
        g2.setColor(new Color(139, 69, 19, 60)); // Nâu mờ
        g2.fillRect(doorRect.x, doorRect.y, doorRect.width, doorRect.height);

        // Vẽ vật phẩm chưa nhặt
        for (PickupItem item : pickupItems) {
            if (!item.collected && item.image != null) {
                g2.drawImage(item.image, item.x, item.y, item.w, item.h, null);
                // Hiệu ứng lấp lánh
                g2.setColor(new Color(255, 255, 100, (int)(80 + 40 * Math.sin(System.currentTimeMillis() * 0.005))));
                g2.drawRect(item.x - 1, item.y - 1, item.w + 2, item.h + 2);
            }
        }

        // Vẽ Player
        gp.player.draw(g2, 0, 0);

        // === HUD ===
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("PHÒNG KÝ TÚC CỦA VŨ", 10, 30);

        // Interaction prompt
        if (!interactPrompt.isEmpty()) {
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(interactPrompt);
            int tx = gp.screenWidth / 2 - tw / 2;
            int ty = gp.screenHeight - 50;
            // Background
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(tx - 10, ty - 20, tw + 20, 30, 10, 10);
            g2.setColor(Color.YELLOW);
            g2.drawString(interactPrompt, tx, ty);
        }

        // HUD message (nhặt đồ)
        if (hudMessageTimer > 0) {
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(hudMessage);
            int tx = gp.screenWidth / 2 - tw / 2;
            int ty = gp.screenHeight / 2 - 50;

            // Fade effect
            int alpha = Math.min(255, hudMessageTimer * 4);
            g2.setColor(new Color(0, 0, 0, Math.min(180, alpha)));
            g2.fillRoundRect(tx - 15, ty - 25, tw + 30, 40, 12, 12);
            g2.setColor(new Color(100, 255, 100, alpha));
            g2.drawString(hudMessage, tx, ty);
        }
    }

    private void drawFurniture(Graphics2D g2, BufferedImage img, int x, int y, int w, int h) {
        if (img != null) {
            g2.drawImage(img, x, y, w, h, null);
        } else {
            // Fallback: vẽ hình chữ nhật nếu không có ảnh
            g2.setColor(new Color(139, 90, 43));
            g2.fillRect(x, y, w, h);
            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, w, h);
        }
    }

    @Override
    public void exit() {
        gp.enemies.clear();
        obstacles.clear();
    }

    @Override
    public void handleMouseClick(MouseEvent e) {
        // Không cần xử lý click chuột trong phòng
    }
}
