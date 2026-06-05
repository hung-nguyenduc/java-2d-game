package entity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

// Lớp đại diện cho đạn trong game - Đã hợp thể và tối ưu cấu trúc mới
public class Bullet {
    public double worldX, worldY; // Vị trí trong thế giới
    public double vx, vy;         // Vận tốc di chuyển (tính sẵn theo góc)
    public double bulletSpeed = 12.0; // Tăng tốc độ lên tí cho đạn shotgun bay mượt hơn
    public int bulletSize = 12;   // Kích thước đạn
    public int maxRange = 1000;   // Phạm vi tối đa (px) viên đạn có thể bay
    public double travelDistance = 0; // Khoảng cách đã đi để check out of range
    public Color color = Color.RED;   // Màu sắc dự phòng
    public BufferedImage bulletImg;
    public double angle;          // Góc bắn của đạn (độ)

    // Constructor: Khởi tạo đạn với vị trí tâm súng và góc bắn từ Weapon truyền sang
    public Bullet(double startX, double startY, double angle) {
        this.worldX = startX;
        this.worldY = startY;
        this.angle = angle;

        // Tính toán sẵn vận tốc vector một lần duy nhất tại đây để tối ưu CPU
        this.vx = Math.cos(Math.toRadians(angle)) * bulletSpeed;
        this.vy = Math.sin(Math.toRadians(angle)) * bulletSpeed;

        getBulletImg();
    }

    // Nạp ảnh viên đạn từ thư mục resources
    public void getBulletImg() {
        try {
            BufferedImage bulletTemp = ImageIO.read(getClass().getResourceAsStream("/weapon/flybullet.jpg"));
            bulletImg = makeColorTransparent(bulletTemp, Color.BLACK, 40); // Loại bỏ viền đen với dung sai 40
        } catch (IOException e) {
            System.err.println("Không thể nạp ảnh viên đạn /weapon/flybullet.jpg");
            e.printStackTrace();
        }
    }

    private BufferedImage makeColorTransparent(BufferedImage im, Color color, int tolerance) {
        BufferedImage dimg = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dimg.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(im, null, 0, 0);
        g.dispose();
        for(int i = 0; i < dimg.getHeight(); i++) {
            for(int j = 0; j < dimg.getWidth(); j++) {
                int px = dimg.getRGB(j, i);
                int r = (px >> 16) & 0xFF;
                int g_ = (px >> 8) & 0xFF;
                int b = px & 0xFF;
                if (Math.abs(r - color.getRed()) <= tolerance &&
                    Math.abs(g_ - color.getGreen()) <= tolerance &&
                    Math.abs(b - color.getBlue()) <= tolerance) {
                    dimg.setRGB(j, i, 0x00FFFFFF); // Giữ nguyên RGB nhưng set Alpha = 0
                }
            }
        }
        return dimg;
    }

    // Cập nhật vị trí đạn mỗi frame
    public void update() {
        worldX += vx;
        worldY += vy;

        // Cộng dồn khoảng cách viên đạn đã di chuyển được
        travelDistance += bulletSpeed;
    }

    // Kiểm tra xem đạn có vượt quá phạm vi cho phép không để Weapon tự xóa khỏi danh sách
    public boolean isOutOfRange() {
        return travelDistance > maxRange;
    }

    // Vẽ viên đạn xoay theo góc ngắm dựa trên tọa độ camera đã clamp
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int) (worldX - cameraX);
        int screenY = (int) (worldY - cameraY);

        // Nếu ảnh bị lỗi không nạp được, vẽ hình tròn dự phòng tránh crash game
        if (bulletImg == null) {
            g2.setColor(color);
            g2.fillOval(screenX - bulletSize / 2, screenY - bulletSize / 2, bulletSize, bulletSize);
            return;
        }

        // Lưu trạng thái gốc của nét vẽ
        AffineTransform original = g2.getTransform();

        // 1. Dịch chuyển trục tọa độ đến đúng tâm viên đạn trên màn hình
        g2.translate(screenX + bulletSize / 2.0, screenY + bulletSize / 2.0);

        // 2. Xoay hệ tọa độ theo góc bay của viên đạn
        g2.rotate(Math.toRadians(angle));

        // 3. Vẽ ảnh đạn (đặt gốc tọa độ về góc âm để căn giữa chuẩn chỉ)
        g2.drawImage(bulletImg, -bulletSize / 2, -bulletSize / 2, bulletSize, bulletSize, null);

        // Khôi phục lại trạng thái hệ tọa độ gốc cho các thực thể khác vẽ tiếp
        g2.setTransform(original);
    }
}