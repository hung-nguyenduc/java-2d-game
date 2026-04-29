package main;

import java.awt.*;
import java.awt.event.MouseEvent;

// GameState for game over screen
public class GameOverState extends GameState {
    private Rectangle menuButton = new Rectangle(300, 400, 200, 50);

    public GameOverState(GamePanel gp) {
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
        // No updates needed for game over screen
    }

    @Override
    public void draw(Graphics2D g2) {
        // Draw background
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Draw game over message
        g2.setColor(Color.RED);
        g2.setFont(new Font("Arial", Font.BOLD, 64));
        FontMetrics fm = g2.getFontMetrics();
        String gameOverText = "GAME OVER";
        int textX = (gp.screenWidth - fm.stringWidth(gameOverText)) / 2;
        g2.drawString(gameOverText, textX, 150);

        // Draw reason
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 32));
        fm = g2.getFontMetrics();
        String reasonText = "Bạn đã bị tiêu diệt!";
        int reasonX = (gp.screenWidth - fm.stringWidth(reasonText)) / 2;
        g2.drawString(reasonText, reasonX, 250);

        // Draw back to menu button
        drawButton(g2, menuButton, "Quay lại Menu");
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
        if (menuButton.contains(e.getPoint())) {
            gp.setState(new MenuState(gp));
        }
    }
}


