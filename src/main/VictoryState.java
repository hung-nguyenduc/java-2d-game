package main;

import java.awt.*;
import java.awt.event.MouseEvent;

// GameState for victory screen
public class VictoryState extends GameState {
    private Rectangle menuButton = new Rectangle(300, 400, 200, 50);

    public VictoryState(GamePanel gp) {
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
        // No updates needed for victory screen
    }

    @Override
    public void draw(Graphics2D g2) {
        // Draw background
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Draw victory message
        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial", Font.BOLD, 64));
        FontMetrics fm = g2.getFontMetrics();
        String victoryText = "CHIẾN THẮNG!";
        int textX = (gp.screenWidth - fm.stringWidth(victoryText)) / 2;
        g2.drawString(victoryText, textX, 150);

        // Draw congratulations message
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 32));
        fm = g2.getFontMetrics();
        String congrats = "Bạn đã hoàn thành tất cả các màn chơi!";
        int congratsX = (gp.screenWidth - fm.stringWidth(congrats)) / 2;
        g2.drawString(congrats, congratsX, 250);

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
            gp.currentMap = 0;
            gp.loadMap();
            gp.setState(new MenuState(gp));
        }
    }
}


