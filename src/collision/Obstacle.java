package collision;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.BasicStroke;

public class Obstacle {
    public int worldX, worldY, width, height;
    public Color color;
    public int type = 0;

    public Obstacle(int worldX, int worldY, int width, int height, Color color) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    public Obstacle(int worldX, int worldY, int width, int height, int type) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.width = width;
        this.height = height;
        this.type = type;
        this.color = new Color(0, 0, 0, 0);
    }

    public Rectangle getBounds() {
        return new Rectangle(worldX, worldY, width, height);
    }

    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = worldX - cameraX;
        int screenY = worldY - cameraY;

        if (screenX + width > 0 && screenX < 2000 &&
                screenY + height > 0 && screenY < 2000) {

            if (type == 0) {
                // Vật cản ẩn (Tường biên)
                if (color.getAlpha() > 0) {
                    g2.setColor(color);
                    g2.fillRect(screenX, screenY, width, height);
                }
            } else if (type == 1) {
                // Thùng gỗ (Wooden Box)
                g2.setColor(new Color(139, 69, 19)); // Màu nâu gỗ
                g2.fillRect(screenX, screenY, width, height);

                // Viền ngoài
                g2.setStroke(new BasicStroke(3));
                g2.setColor(new Color(101, 42, 14));
                g2.drawRect(screenX, screenY, width, height);

                // Đường chéo
                g2.drawLine(screenX, screenY, screenX + width, screenY + height);
                g2.drawLine(screenX + width, screenY, screenX, screenY + height);
            } else if (type == 2) {
                // Thùng phuy dầu (Oil Barrel) - Hình chữ nhật có bo tròn
                g2.setColor(new Color(220, 20, 60)); // Đỏ thẫm
                g2.fillRoundRect(screenX, screenY, width, height, 15, 15);

                // Viền ngoài kim loại
                g2.setStroke(new BasicStroke(3));
                g2.setColor(Color.DARK_GRAY);
                g2.drawRoundRect(screenX, screenY, width, height, 15, 15);

                // Các sọc ngang của thùng phuy
                g2.drawLine(screenX, screenY + height / 3, screenX + width, screenY + height / 3);
                g2.drawLine(screenX, screenY + 2 * height / 3, screenX + width, screenY + 2 * height / 3);
            }
        }
    }
}