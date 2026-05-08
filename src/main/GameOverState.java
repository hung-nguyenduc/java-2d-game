package main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GameOverState extends GameState {
    private Rectangle retryButton = new Rectangle(300, 320, 200, 50);
    private Rectangle menuButton  = new Rectangle(300, 400, 200, 50);
    private BufferedImage img;
    public GameOverState(GamePanel gp) {

        super(gp);
        getImg();
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
        // Nền tối mờ
//        g2.setColor(new Color(0, 0, 0, 200));
//        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        g2.drawImage(img, 0, 0, gp.screenWidth, gp.screenHeight, null);
        // Chữ GAME OVER
        g2.setColor(Color.RED);
        g2.setFont(new Font("Arial", Font.BOLD, 80));
        FontMetrics fm = g2.getFontMetrics();
        String title = "GAME OVER";
        g2.drawString(title, (gp.screenWidth - fm.stringWidth(title)) / 2, gp.screenHeight / 2 - 40);

        // Nút
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        drawButton(g2, retryButton, "Choi lai");
        drawButton(g2, menuButton,  "Menu chinh");
    }

    private void drawButton(Graphics2D g2, Rectangle rect, String text) {
        g2.setColor(new Color(80, 0, 0));
        g2.fillRect(rect.x, rect.y, rect.width, rect.height);
        g2.setColor(Color.RED);
        g2.drawRect(rect.x, rect.y, rect.width, rect.height);
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        int tx = rect.x + (rect.width  - fm.stringWidth(text)) / 2;
        int ty = rect.y + (rect.height + fm.getAscent()) / 2 - 4;
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