package state;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import main.GamePanel;

public class MenuState extends GameState {
    // Tao chỉnh lại tọa độ và kích thước nút một chút cho cân đối hơn
    private Rectangle playButton = new Rectangle(300, 220, 200, 60);
    private Rectangle instructionsButton = new Rectangle(300, 310, 200, 60);
    private Rectangle infoButton = new Rectangle(300, 400, 200, 60);
    private BufferedImage background;

    public MenuState(GamePanel gp) {
        super(gp);
        getBackgroundImage();
    }

    @Override
    public void enter() {
        // No special initialization
    }

    @Override
    public void exit() {
        // No special cleanup
    }

    @Override
    public void update() {
        // No updates needed for menu
    }

    public void getBackgroundImage() {
        try {
            background = ImageIO.read(getClass().getResourceAsStream("/DialogueBackground/thu-vien.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // BẬT KHỬ RĂNG CƯA (Cực kỳ quan trọng để UI đẹp)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Vẽ background (giữ nguyên của mày)
        if (background != null) {
            g2.drawImage(background, 0, 0, gp.screenWidth, gp.screenHeight, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        }

        // Làm tối background một chút để làm nổi bật Title và Button
        g2.setColor(new Color(0, 0, 0, 100)); // Lớp phủ đen trong suốt 40%
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Vẽ Title (Không dùng hộp chữ nhật nữa, vẽ chữ nổi 3D đẹp hơn)
        drawTitle(g2, "Sinh Tồn Ở HUST", 130);

        // Vẽ Buttons
        drawButton(g2, playButton, "Chơi Ngay");
        drawButton(g2, instructionsButton, "Hướng Dẫn");
        drawButton(g2, infoButton, "Thông Tin");
    }

    private void drawTitle(Graphics2D g2, String text, int y) {
        g2.setFont(new Font("Arial", Font.BOLD, 64));
        FontMetrics fm = g2.getFontMetrics();
        int x = (gp.screenWidth - fm.stringWidth(text)) / 2;

        // 1. Đổ bóng (Shadow) cho Title
        g2.setColor(new Color(0, 0, 0, 180));
        g2.drawString(text, x + 5, y + 5);

        // 2. Chuyển màu (Gradient) cho chữ: từ Vàng cam xuống Cam đậm
        GradientPaint gradient = new GradientPaint(x, y - 50, new Color(241, 196, 15),
                x, y, new Color(230, 126, 34));
        g2.setPaint(gradient);
        g2.drawString(text, x, y);

        // 3. Viền chữ (Stroke) màu trắng mỏng để nổi bật (Tùy chọn, tao dùng drawString đè lên để giả viền)
        // Cách nhanh nhất trong Java2D để giả viền là không cần thiết nếu đổ bóng đã tốt,
        // nhưng tao giữ gọn nhẹ như vầy là đủ đẹp rồi.
    }

    private void drawButton(Graphics2D g2, Rectangle rect, String text) {
        // 1. Đổ bóng cho nút (Shadow)
        g2.setColor(new Color(0, 0, 0, 120)); // Đen trong suốt
        g2.fillRoundRect(rect.x + 5, rect.y + 5, rect.width, rect.height, 25, 25);

        // 2. Nền nút chuyển màu Gradient (Từ xanh nhạt xuống xanh đậm)
        Color colorTop = new Color(52, 152, 219);
        Color colorBottom = new Color(41, 128, 185);
        GradientPaint gp = new GradientPaint(rect.x, rect.y, colorTop,
                rect.x, rect.y + rect.height, colorBottom);
        g2.setPaint(gp);
        g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 25, 25);

        // 3. Viền nút (Sáng nhẹ để tạo cảm giác kính/nổi)
        g2.setColor(new Color(255, 255, 255, 100)); // Trắng mờ
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 25, 25);

        // 4. Căn giữa và vẽ text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        FontMetrics fm = g2.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();

        // Đổ bóng cho text trong nút
        g2.setColor(new Color(0, 0, 0, 150));
        g2.drawString(text, textX + 2, textY + 2);

        // Vẽ text chính
        g2.setColor(Color.WHITE);
        g2.drawString(text, textX, textY);
    }

    @Override
    public void handleMouseClick(MouseEvent e) {
        Point p = e.getPoint();
        if (playButton.contains(p)) {
            gp.setState(new KTXState(gp));
        } else if (instructionsButton.contains(p)) {
            gp.setState(new InstructionsState(gp));
        } else if (infoButton.contains(p)) {
            gp.setState(new InfoState(gp));
        }
    }
}