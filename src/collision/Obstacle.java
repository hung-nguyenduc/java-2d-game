package collision;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Obstacle {
    public int worldX, worldY, width, height;
    public Color color; // Để bạn dễ phân biệt các khối

    public Obstacle(int worldX, int worldY, int width, int height, Color color) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    // Hàm trả về Rectangle để sau này bạn làm hệ thống xử lý va chạm (Collision)
    public Rectangle getBounds() {
        return new Rectangle(worldX, worldY, width, height);
    }

    // Vẽ vật cản dựa theo vị trí Camera tương tự như Player và Enemy
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        // Tính toán vị trí hiển thị trên màn hình (Screen X, Screen Y)
        int screenX = worldX - cameraX;
        int screenY = worldY - cameraY;

        // Chỉ vẽ nếu vật cản nằm trong vùng nhìn thấy của màn hình (tối ưu hiệu năng)
        if (screenX + width > 0 && screenX < 800 && // Thay 800/600 bằng gp.screenWidth/screenHeight của bạn
                screenY + height > 0 && screenY < 600) {

            Color transparentRed = new Color(255, 0, 0, 125);

            g2.setColor(transparentRed);
            g2.fillRect(screenX, screenY, width, height);

            // Vẽ thêm viền đen cho đẹp (tùy chọn)
//            g2.setColor(Color.BLACK);
//            g2.drawRect(screenX, screenY, width, height);
        }
    }
}