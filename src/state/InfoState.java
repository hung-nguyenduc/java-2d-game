package state;

import main.GamePanel;
import java.awt.*;
import java.awt.event.MouseEvent;

public class InfoState extends GameState {
    private Rectangle backButton;
    private boolean isHovered = false; // Biến chờ để làm hiệu ứng hover

    private String storyText = "Nhập vai Vũ, một sinh viên Bách Khoa K36. Hành trình đi từ kiếp nạn Giải tích, kỳ quân sự bão táp cho đến khi gác bút nghiên cầm súng bảo vệ Tổ quốc. Hãy giúp Vũ vượt mọi ải từ giảng đường đến chiến trường để ghi danh tại đài tưởng niệm!";

    public InfoState(GamePanel gp) {
        super(gp);
        // Tự động căn giữa nút Quay lại ở dưới cùng màn hình
        int buttonWidth = 200;
        int buttonHeight = 50;
        int buttonX = (gp.screenWidth - buttonWidth) / 2;
        int buttonY = gp.screenHeight - 150;
        backButton = new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight);
    }

    @Override
    public void enter() {}

    @Override
    public void exit() {}

    @Override
    public void update() {}

    @Override
    public void draw(Graphics2D g2) {
        // 1. Vẽ Background Gradient (Đen xám -> Đỏ tối)
        GradientPaint bgGradient = new GradientPaint(0, 0, new Color(30, 30, 30), 0, gp.screenHeight, new Color(40, 10, 10));
        g2.setPaint(bgGradient);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // 2. Vẽ Tiêu đề với hiệu ứng Đổ bóng
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        String title = "THÔNG TIN GAME";
        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleX = (gp.screenWidth - titleMetrics.stringWidth(title)) / 2;

        // Vẽ bóng chữ trước (lệch đi vài pixel)
        g2.setColor(Color.BLACK);
        g2.drawString(title, titleX + 4, 104);
        // Vẽ chữ chính đè lên
        g2.setColor(new Color(255, 215, 0));
        g2.drawString(title, titleX, 100);

        // 3. Vẽ Tiêu đề Cốt truyện
        g2.setFont(new Font("Arial", Font.BOLD, 28));
        g2.setColor(Color.WHITE);
        g2.drawString("Cốt truyện:", 100, 180);

        // 4. Vẽ nội dung Cốt truyện (Tự động xuống dòng)
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        g2.setColor(new Color(220, 220, 220)); // Trắng xám cho dịu mắt

        // Chiều rộng tối đa của dòng chữ = Chiều rộng màn hình trừ đi lề 2 bên (mỗi bên 100px)
        int textMaxWidth = gp.screenWidth - 200;
        drawWrappedText(g2, storyText, 100, 230, textMaxWidth);

        // 5. Vẽ nút bấm
        drawButton(g2, backButton, "Quay lại");
    }

    // Hàm tiện ích: Tự động cắt dòng khi chữ dài vượt quá maxWidth
    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder(words[0]);

        for (int i = 1; i < words.length; i++) {
            if (fm.stringWidth(currentLine + " " + words[i]) < maxWidth) {
                currentLine.append(" ").append(words[i]);
            } else {
                g2.drawString(currentLine.toString(), x, y);
                y += fm.getHeight() + 10; // Cộng thêm 10px khoảng cách giữa các dòng
                currentLine = new StringBuilder(words[i]);
            }
        }
        // Vẽ nốt đoạn cuối cùng
        g2.drawString(currentLine.toString(), x, y);
    }

    private void drawButton(Graphics2D g2, Rectangle rect, String text) {
        // Bóng của nút
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(rect.x + 5, rect.y + 5, rect.width, rect.height, 20, 20);

        // Nền của nút (Đổi màu nếu chuột đang hover vào)
        if (isHovered) {
            g2.setColor(new Color(100, 100, 100)); // Sáng lên khi di chuột qua
        } else {
            g2.setColor(new Color(60, 60, 60)); // Màu mặc định
        }
        g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);

        // Viền nút
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);

        // Text bên trong nút
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        FontMetrics fm = g2.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, textX, textY);
    }

    @Override
    public void handleMouseClick(MouseEvent e) {
        if (backButton.contains(e.getPoint())) {
            gp.setState(new MenuState(gp));
        }
    }

    // Nếu sau này mỳ bắt thêm sự kiện chuột di chuyển thì gọi hàm này
    public void handleMouseMoved(MouseEvent e) {
        isHovered = backButton.contains(e.getPoint());
    }
}