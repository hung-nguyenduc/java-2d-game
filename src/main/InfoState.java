package main;

import java.awt.*;
import java.awt.event.MouseEvent;

// GameState for game info
public class InfoState extends GameState {
    private Rectangle backButton = new Rectangle(300, 500, 200, 50);

    public InfoState(GamePanel gp) {
        super(gp);
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
        // No updates needed for info screen
    }

    @Override
    public void draw(Graphics2D g2) {
        // Draw background
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Draw title
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 36));
        g2.drawString("Thông tin game", 250, 100);

        // Draw storyline
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        g2.drawString("Cốt truyện:", 150, 160);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.drawString("Trong một thế giới bị zombie xâm chiếm,", 150, 210);
        g2.drawString("bạn phải điều hướng qua các màn chơi,", 150, 240);
        g2.drawString("đánh bại kẻ thù, và đạt đến checkpoint", 150, 270);
        g2.drawString("để sinh tồn. Sử dụng kỹ năng bắn súng", 150, 300);
        g2.drawString("và di chuyển thông minh để chiến thắng", 150, 330);
        g2.drawString("tất cả các màn chơi và cứu thế giới!", 150, 360);

        // Draw back button
        drawButton(g2, backButton, "Quay lại");
    }

    private void drawButton(Graphics2D g2, Rectangle rect, String text) {
        g2.setColor(Color.GRAY);
        g2.fillRect(rect.x, rect.y, rect.width, rect.height);
        g2.setColor(Color.BLACK);
        g2.drawRect(rect.x, rect.y, rect.width, rect.height);
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + (rect.height + fm.getAscent()) / 2;
        g2.drawString(text, textX, textY);
    }

    @Override
    public void handleMouseClick(MouseEvent e) {
        if (backButton.contains(e.getPoint())) {
            gp.setState(new MenuState(gp));
        }
    }
}


