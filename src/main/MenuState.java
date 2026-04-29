package main;

import java.awt.*;
import java.awt.event.MouseEvent;

// GameState for the main menu
public class MenuState extends GameState {
    private Rectangle playButton = new Rectangle(300, 200, 200, 50);
    private Rectangle instructionsButton = new Rectangle(300, 300, 200, 50);
    private Rectangle infoButton = new Rectangle(300, 400, 200, 50);

    public MenuState(GamePanel gp) {
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
        // No updates needed for menu
    }

    @Override
    public void draw(Graphics2D g2) {
        // Draw background
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Draw title
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g2.getFontMetrics();
        String title = "Zombie Survival Game";
        int titleX = (gp.screenWidth - fm.stringWidth(title)) / 2;
        g2.drawString(title, titleX, 100);

        // Draw buttons
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        drawButton(g2, playButton, "Chơi");
        drawButton(g2, instructionsButton, "Xem hướng dẫn");
        drawButton(g2, infoButton, "Thông tin game");
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
        Point p = e.getPoint();
        if (playButton.contains(p)) {
            gp.setState(new ZombieState(gp));
        } else if (instructionsButton.contains(p)) {
            gp.setState(new InstructionsState(gp));
        } else if (infoButton.contains(p)) {
            gp.setState(new InfoState(gp));
        }
    }
}


