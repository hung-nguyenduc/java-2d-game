package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Item {
    public int worldX, worldY;
    public String name;
    public BufferedImage image;
    // Khung va chạm mặc định (ví dụ 32x32 pixel).
    // Nếu sprite của mày to/nhỏ hơn thì chỉnh lại cho khớp.
    public Rectangle solidArea = new Rectangle(0, 0, 32, 32);

    public Item(String name, String imagePath, int worldX, int worldY) {
        this.name = name;
        this.worldX = worldX;
        this.worldY = worldY;
        try {
            this.image = ImageIO.read(getClass().getResourceAsStream(imagePath));
        } catch (Exception e) {
            System.err.println("Không tìm thấy ảnh item: " + imagePath);
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        // Tính toán tọa độ vẽ trên màn hình dựa vào camera
        int screenX = worldX - cameraX;
        int screenY = worldY - cameraY;

        if (image != null) {
            g2.drawImage(image, screenX, screenY, solidArea.width, solidArea.height, null);
        }
    }
}