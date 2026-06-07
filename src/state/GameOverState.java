package state;

import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class GameOverState extends GameState {

    // Khai báo kích thước nút
    private int btnWidth = 220;
    private int btnHeight = 55;

    // Khởi tạo các nút
    private Rectangle retryButton;
    private Rectangle menuButton;
    private BufferedImage img;

    public GameOverState(GamePanel gp) {
        super(gp);
        getImg();

        // Tự động tính toán để căn giữa màn hình
        int centerX = (gp.screenWidth - btnWidth) / 2;
        int startY = gp.screenHeight / 2 + 30; // Nút bắt đầu ngay dưới chữ Game Over

        retryButton = new Rectangle(centerX, startY, btnWidth, btnHeight);
        menuButton  = new Rectangle(centerX, startY + 80, btnWidth, btnHeight);
    }

    public void getImg() {
        try {
            img = ImageIO.read(getClass().getResourceAsStream("/endgame/endgame2.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void enter() {}

    @Override
    public void exit() {}

    @Override
    public void update() {}

    @Override
    public void draw(Graphics2D g2) {
        // Bật tính năng khử răng cưa (Antialiasing) để nét vẽ mượt mà
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Vẽ hình nền
        g2.drawImage(img, 0, 0, gp.screenWidth, gp.screenHeight, null);

        // 2. Lớp nền tối mờ (Overlay) giúp nổi bật UI
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // 3. Cài đặt chữ GAME OVER
        String title = "GAME OVER";
        g2.setFont(new Font("Arial", Font.BOLD, 85));
        FontMetrics fm = g2.getFontMetrics();
        int titleX = (gp.screenWidth - fm.stringWidth(title)) / 2;
        int titleY = gp.screenHeight / 2 - 50;

        // Đổ bóng chữ (Vẽ màu đen lùi xuống dưới và sang phải một chút)
        g2.setColor(new Color(0, 0, 0, 200));
        g2.drawString(title, titleX + 6, titleY + 6);

        // Vẽ chữ chính với màu Đỏ thẫm (Crimson)
        g2.setColor(new Color(220, 20, 60));
        g2.drawString(title, titleX, titleY);

        // 4. Vẽ các nút
        drawButton(g2, retryButton, "Chơi lại");
        drawButton(g2, menuButton,  "Menu chính");
    }

    private void drawButton(Graphics2D g2, Rectangle rect, String text) {
        // Đổ bóng cho nút
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(rect.x + 5, rect.y + 5, rect.width, rect.height, 25, 25);

        // Dải màu Gradient cho nút (sáng ở trên, tối dần xuống dưới)
        GradientPaint gpnt = new GradientPaint(
                rect.x, rect.y, new Color(180, 30, 30),
                rect.x, rect.y + rect.height, new Color(90, 0, 0)
        );
        g2.setPaint(gpnt);
        g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 25, 25);

        // Viền nút
        g2.setColor(new Color(255, 120, 120));
        g2.setStroke(new BasicStroke(2)); // Độ dày của viền
        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 25, 25);

        // Cài đặt chữ trong nút
        g2.setFont(new Font("Arial", Font.BOLD, 22)); // Dùng BOLD trông sẽ khỏe khoắn hơn
        FontMetrics fm = g2.getFontMetrics();
        int tx = rect.x + (rect.width  - fm.stringWidth(text)) / 2;
        int ty = rect.y + (rect.height + fm.getAscent()) / 2 - 4;

        // Đổ bóng nhẹ cho chữ bên trong nút
        g2.setColor(new Color(0, 0, 0, 180));
        g2.drawString(text, tx + 2, ty + 2);

        // Màu chữ chính
        g2.setColor(Color.WHITE);
        g2.drawString(text, tx, ty);
    }

    @Override
    public void handleMouseClick(MouseEvent e) {
        Point p = e.getPoint();
        if (retryButton.contains(p)) {
            // Reset player rồi quay về màn 1
            gp.player.worldX = 1000;
            gp.player.worldY = 1000;
            gp.setState(new ZombieState(gp));
        } else if (menuButton.contains(p)) {
            gp.player.worldX = 1000;
            gp.player.worldY = 1000;
            gp.player.health = gp.player.maxHealth;
            gp.player.bullets.clear();
            gp.setState(new MenuState(gp));
        }
    }
}