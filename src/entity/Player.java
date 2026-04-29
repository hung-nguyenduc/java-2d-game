package entity;

import main.GamePanel;
import main.KeyHandler;
import java.awt.*;
import java.awt.image.BufferedImage; // Import lớp để xử lý ảnh
import java.io.IOException;
import javax.imageio.ImageIO; // Import công cụ đọc ảnh

public class Player extends Entity {
    GamePanel gp;
    KeyHandler keyH;

    // Khai báo biến chứa hình ảnh
    public BufferedImage playerImage;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;

        setDefaultValues();
        getPlayerImage(); // Gọi hàm load ảnh ngay khi nhân vật được tạo ra
    }

    public void setDefaultValues() {
        worldX = 100;
        worldY = 100;
        speed = 4;
    }

    // Hàm chuyên dụng để tải ảnh từ thư mục
    public void getPlayerImage() {
        try {
            var is = getClass().getResourceAsStream("/player/player.png");
            if (is == null) {
                System.out.println("LỖI: Không tìm thấy ảnh nhân vật!");
                return;
            }
            playerImage = ImageIO.read(is);
        } catch (IOException e) {
            System.out.println("LỖI: Không đọc được ảnh nhân vật!");
            e.printStackTrace();
        }
    }

    public void update() {
        if (keyH.upPressed) {
            worldY -= speed;
        }
        else if (keyH.downPressed) {
            worldY += speed;
        }
        else if (keyH.leftPressed) {
            worldX -= speed;
        }
        else if (keyH.rightPressed) {
            worldX += speed;
        }
    }

    public void draw(Graphics2D g2) {
        // Tính toán vị trí của nhân vật TRÊN MÀN HÌNH
        // Luôn giữ nhân vật ở giữa màn hình
        int screenX = gp.screenWidth / 2 - (gp.tileSize / 2);
        int screenY = gp.screenHeight / 2 - (gp.tileSize / 2);

        // Vẽ nhân vật tại vị trí cố định trên màn hình
        g2.drawImage(playerImage, screenX, screenY, 80, 80, null);
    }
}