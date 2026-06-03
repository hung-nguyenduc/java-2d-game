package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Color;

public class Item {
    public int worldX, worldY;
    public String name;
    public BufferedImage image;
    public Rectangle solidArea;
    public double radius; // Bán kính vùng nhặt

    private static final int ITEM_SIZE = 48;
    private static final double DEFAULT_PICKUP_RADIUS = 60;

    public Item(String name, String imagePath, int worldX, int worldY) {
        this.name = name;
        this.worldX = worldX;
        this.worldY = worldY;

        this.solidArea = new Rectangle(worldX, worldY, ITEM_SIZE, ITEM_SIZE);
        this.radius = DEFAULT_PICKUP_RADIUS;

        // Load ảnh
        try {
            BufferedImage original = ImageIO.read(getClass().getResourceAsStream(imagePath));
            if (original != null) {
                this.image = new BufferedImage(ITEM_SIZE, ITEM_SIZE, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = this.image.createGraphics();
                g2d.drawImage(original, 0, 0, ITEM_SIZE, ITEM_SIZE, null);
                g2d.dispose();
            }
        } catch (Exception e) {
            System.err.println("Lỗi load ảnh: " + imagePath);
            e.printStackTrace();
        }
    }

    // Constructor tùy chỉnh bán kính
    public Item(String name, String imagePath, int worldX, int worldY, double radius) {
        this(name, imagePath, worldX, worldY);
        this.radius = radius;
    }

    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        if (image != null) {
            int screenX = worldX - cameraX;
            int screenY = worldY - cameraY;
            g2.drawImage(image, screenX, screenY, ITEM_SIZE, ITEM_SIZE, null);
        }
    }
}