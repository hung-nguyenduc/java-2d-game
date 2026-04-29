package main;

import java.awt.*;
import java.awt.event.MouseEvent;

// GameState for instructions
public class InstructionsState extends GameState {
    private Rectangle backButton = new Rectangle(300, 500, 200, 50);

    public InstructionsState(GamePanel gp) {
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
        // No updates needed for instructions screen
    }

    @Override
    public void draw(Graphics2D g2) {
        // Draw background
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Draw title
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 36));
        g2.drawString("Hướng dẫn", 300, 100);

        // Draw instructions
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        g2.drawString("Sử dụng phím AWSD để di chuyển:", 150, 200);
        g2.drawString("W: Lên", 200, 250);
        g2.drawString("S: Xuống", 200, 280);
        g2.drawString("A: Trái", 200, 310);
        g2.drawString("D: Phải", 200, 340);
        g2.drawString("Nhắm và bắn tự động vào kẻ thù gần nhất.", 150, 400);
        g2.drawString("Tiêu diệt tất cả kẻ thù để đạt checkpoint!", 150, 430);

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


