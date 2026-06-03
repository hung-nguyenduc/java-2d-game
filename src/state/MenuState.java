package state;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import main.GamePanel;

// GameState for the main menu
public class MenuState extends GameState {
    private Rectangle playButton = new Rectangle(300, 200, 200, 50);
    private Rectangle instructionsButton = new Rectangle(300, 300, 200, 50);
    private Rectangle infoButton = new Rectangle(300, 400, 200, 50);
    private Rectangle titleButton = new Rectangle(175, 100, 500, 50);
    private BufferedImage background;

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
        // No updates needed for menu
    }

    public void getBackgroundImage() {
        try {
            background = ImageIO.read(getClass().getResourceAsStream("/DialogueBackground/lop-hoc.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void draw(Graphics2D g2) {
        // Draw background
//        g2.setColor(Color.BLACK);
//        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.drawImage(background, 0, 0, gp.screenWidth, gp.screenHeight, null);
        // Draw title
        g2.setColor(Color.RED);
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g2.getFontMetrics();
        String title = "Sinh tồn ở HUST";
        int titleX = (gp.screenWidth - fm.stringWidth(title)) / 2 + 30;
        //g2.drawString(title, titleX, 100);


        // Draw buttons
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        drawButton(g2, playButton, "Chơi");
        drawButton(g2, instructionsButton, "Xem hướng dẫn");
        drawButton(g2, infoButton, "Thông tin game");

        g2.setFont(new Font("Arial", Font.BOLD, 36));
        drawTitle(g2, titleButton, "Sinh tồn ở HUST");
    }
    private void drawTitle(Graphics2D g2, Rectangle rect, String text) {
        g2.setColor(Color.ORANGE);
        g2.fillRect(rect.x, rect.y, rect.width, rect.height);
        g2.setColor(Color.BLACK);
        g2.drawRect(rect.x, rect.y, rect.width, rect.height);
        g2.setColor(Color.RED);
        FontMetrics fm = g2.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + (rect.height + fm.getAscent()) / 2;
        g2.drawString(text, textX, textY);
    }
    private void drawButton(Graphics2D g2, Rectangle rect, String text) {
        g2.setColor(Color.DARK_GRAY);
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
            gp.setState(new KTXState(gp));
        } else if (instructionsButton.contains(p)) {
            gp.setState(new InstructionsState(gp));
        } else if (infoButton.contains(p)) {
            gp.setState(new InfoState(gp));
        }
    }
}


