package state;

import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class InstructionsState extends GameState {
    private Rectangle backButton;
    private boolean isHovered = false;
    private BufferedImage bgImage;

    // Đường dẫn ảnh nền màn Hướng dẫn (tính từ gốc classpath = thư mục res, KHÔNG kèm tiền tố /res/)
    private final String BACKGROUND_IMAGE_PATH = "/maps/classroom.png";

    public InstructionsState(GamePanel gp) {
        super(gp);

        // Load ảnh nền
        try {
            bgImage = ImageIO.read(getClass().getResourceAsStream(BACKGROUND_IMAGE_PATH));
        } catch (Exception e) {
            System.out.println("Không tìm thấy ảnh nền Hướng dẫn, sử dụng nền Gradient mặc định.");
        }

        // Tự động căn giữa nút Quay lại ở dưới cùng màn hình
        int buttonWidth = 200;
        int buttonHeight = 50;
        int buttonX = (gp.screenWidth - buttonWidth) / 2;
        int buttonY = gp.screenHeight - 90;
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
        // 1. Vẽ Background
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, gp.screenWidth, gp.screenHeight, null);
            // Phủ 1 lớp đen mờ lên ảnh để làm nổi bật chữ
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        } else {
            // Nền dự phòng nếu chưa có ảnh
            GradientPaint bgGradient = new GradientPaint(0, 0, new Color(20, 20, 30), 0, gp.screenHeight, new Color(10, 30, 50));
            g2.setPaint(bgGradient);
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        }

        // 2. Vẽ Tiêu đề (Có đổ bóng)
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        String title = "HƯỚNG DẪN TÂN SINH VIÊN";
        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleX = (gp.screenWidth - titleMetrics.stringWidth(title)) / 2;

        g2.setColor(Color.BLACK);
        g2.drawString(title, titleX + 4, 84); // Bóng
        g2.setColor(new Color(255, 215, 0));
        g2.drawString(title, titleX, 80); // Chữ chính

        // 3. Vẽ danh sách các phím điều khiển
        int startX = 150;
        int startY = 180;
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 26));
        g2.drawString("CƠ CHẾ ĐIỀU KHIỂN:", startX - 20, startY);

        g2.setFont(new Font("Arial", Font.PLAIN, 22));

        // Nhóm phím di chuyển
        drawKeycap(g2, "↑", startX, startY + 40, 40);
        drawKeycap(g2, "↓", startX + 50, startY + 40, 40);
        drawKeycap(g2, "←", startX + 100, startY + 40, 40);
        drawKeycap(g2, "→", startX + 150, startY + 40, 40);
        g2.setColor(Color.WHITE);
        g2.drawString(": Di chuyển nhân vật", startX + 210, startY + 68);

        // Nút K (Đá)
        drawKeycap(g2, "K", startX, startY + 110, 40);
        g2.setColor(Color.WHITE);
        g2.drawString(": Đá lùi quái (Cận chiến cứu nguy)", startX + 60, startY + 138);

        // Nút G (Lựu đạn)
        drawKeycap(g2, "G", startX, startY + 180, 40);
        g2.setColor(Color.WHITE);
        g2.drawString(": Ném lựu đạn (Chú ý: Có thời gian hồi chiều)", startX + 60, startY + 208);

        // Chuột (Ngắm bắn)
        drawKeycap(g2, "Chuột", startX, startY + 250, 80); // Nút dài hơn cho chữ Chuột
        g2.setColor(Color.WHITE);
        g2.drawString(": Di chuột để ngắm. Đạn sẽ tự động xả liên tục!", startX + 100, startY + 278);

//        // 4. Mục tiêu màn chơi
//        g2.setColor(new Color(255, 100, 100)); // Màu đỏ nhạt cho nổi bật
//        g2.setFont(new Font("Arial", Font.BOLD, 24));
//        g2.drawString("MỤC TIÊU: Tiêu diệt sạch sẽ kẻ thù để mở khóa checkpoint!", startX - 20, startY + 340);

        // 5. Vẽ nút bấm Quay lại
        drawButton(g2, backButton, "Quay lại");
    }

    // Hàm tiện ích: Vẽ các phím bấm trông như phím cơ
    private void drawKeycap(Graphics2D g2, String key, int x, int y, int width) {
        int height = 40;

        // Bóng của phím
        g2.setColor(Color.BLACK);
        g2.fillRoundRect(x + 2, y + 4, width, height, 10, 10);

        // Mặt phím
        g2.setColor(new Color(60, 60, 60));
        g2.fillRoundRect(x, y, width, height, 10, 10);

        // Viền phím
        g2.setColor(Color.LIGHT_GRAY);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, width, height, 10, 10);

        // Chữ trên phím
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g2.getFontMetrics();
        int textX = x + (width - fm.stringWidth(key)) / 2;
        int textY = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(key, textX, textY);
    }

    private void drawButton(Graphics2D g2, Rectangle rect, String text) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(rect.x + 5, rect.y + 5, rect.width, rect.height, 20, 20);

        if (isHovered) {
            g2.setColor(new Color(100, 100, 100));
        } else {
            g2.setColor(new Color(60, 60, 60));
        }
        g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);

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

    // Nếu trong GamePanel m có bắt sự kiện di chuột thì gọi vào đây để nháy sáng nút
    public void handleMouseMoved(MouseEvent e) {
        isHovered = backButton.contains(e.getPoint());
    }
}