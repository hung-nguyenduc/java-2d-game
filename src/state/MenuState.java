package state;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import main.GamePanel;

public class MenuState extends GameState {
    private Rectangle playButton = new Rectangle(300, 220, 200, 60);
    private Rectangle instructionsButton = new Rectangle(300, 310, 200, 60);
    private Rectangle infoButton = new Rectangle(300, 400, 200, 60);
    private BufferedImage background;

    // Thêm các biến trạng thái hover
    private boolean isPlayHover = false;
    private boolean isInsHover = false;
    private boolean isInfoHover = false;

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
        // Cập nhật vị trí chuột liên tục mỗi frame
        Point mousePos = gp.getMousePosition();
        if (mousePos != null) {
            isPlayHover = playButton.contains(mousePos);
            isInsHover = instructionsButton.contains(mousePos);
            isInfoHover = infoButton.contains(mousePos);
        } else {
            // Khi chuột kéo ra ngoài cửa sổ game
            isPlayHover = false;
            isInsHover = false;
            isInfoHover = false;
        }
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
        // BẬT KHỬ RĂNG CƯA
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Vẽ background
        if (background != null) {
            g2.drawImage(background, 0, 0, gp.screenWidth, gp.screenHeight, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        }

        // Làm tối background
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Vẽ Title
        drawTitle(g2, "Sinh Tồn Ở HUST", 130);

        // Vẽ Buttons (Truyền thêm trạng thái hover vào)
        drawButton(g2, playButton, "Chơi Ngay", isPlayHover);
        drawButton(g2, instructionsButton, "Hướng Dẫn", isInsHover);
        drawButton(g2, infoButton, "Thông Tin", isInfoHover);
    }

    private void drawTitle(Graphics2D g2, String text, int y) {
        g2.setFont(new Font("Arial", Font.BOLD, 70));
        FontMetrics fm = g2.getFontMetrics();
        int x = (gp.screenWidth - fm.stringWidth(text)) / 2;

        g2.setColor(new Color(0, 0, 0, 180));
        g2.drawString(text, x + 5, y + 5);

        GradientPaint gradient = new GradientPaint(x, y - 50, new Color(241, 196, 15),
                x, y, new Color(230, 126, 34));
        g2.setPaint(gradient);
        g2.drawString(text, x, y);
    }

    // Cập nhật hàm drawButton để nhận thêm tham số isHover
    private void drawButton(Graphics2D g2, Rectangle rect, String text, boolean isHover) {
        // 1. Đổ bóng cho nút
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(rect.x + 5, rect.y + 5, rect.width, rect.height, 25, 25);

        // 2. Chỉnh màu Gradient tùy theo trạng thái Hover
        Color colorTop;
        Color colorBottom;

        if (isHover) {
            // Khi di chuột vào: Nút sáng rực lên
            colorTop = new Color(255, 100, 30);   // Đỏ cam sáng chói
            colorBottom = new Color(200, 30, 0);  // Đỏ tươi
        } else {
            // Trạng thái bình thường của mày
            colorTop = new Color(205, 69, 0);     // Đỏ cam
            colorBottom = new Color(139, 0, 0);   // Đỏ sẫm
        }

        GradientPaint gp = new GradientPaint(rect.x, rect.y, colorTop,
                rect.x, rect.y + rect.height, colorBottom);
        g2.setPaint(gp);
        g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 25, 25);

        // 3. Viền nút
        g2.setColor(new Color(255, 255, 255, isHover ? 180 : 100)); // Trắng sáng hơn nếu hover
        g2.setStroke(new BasicStroke(isHover ? 3f : 2f)); // Viền dày hơn tí khi hover
        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 25, 25);

        // 4. Text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        FontMetrics fm = g2.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();

        g2.setColor(new Color(0, 0, 0, 150));
        g2.drawString(text, textX + 2, textY + 2);

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