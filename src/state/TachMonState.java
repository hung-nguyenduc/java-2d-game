package state;

import main.GamePanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.awt.event.MouseEvent;

public class TachMonState extends GameState {
    private Image endingGif;

    public TachMonState(GamePanel gp) {
        super(gp);
    }

    @Override
    public void enter() {
        try {
            // Đổi đường dẫn này cho đúng tên file GIF ending của mày
            endingGif = new ImageIcon(getClass().getResource("/video/kick.gif")).getImage();
        } catch (Exception e) {
            System.err.println("Không tìm thấy file GIF Ending!");
            e.printStackTrace();
        }

        // Có thể bật bài nhạc buồn buồn lúc trượt môn ở đây
        // gp.sound.playMusic("sad_ending_theme");
    }

    @Override
    public void update() {
        // Ending thì thường chỉ đứng im chiếu GIF, đợi người chơi bấm phím gì đó để về Menu
        if (gp.keyH.spacePressed || gp.keyH.enterPressed) {
            gp.keyH.spacePressed = false;
            gp.keyH.enterPressed = false;

            // Ví dụ ấn Space thì văng về lại màn hình Menu chính
            // gp.setState(new MenuState(gp));
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        if (endingGif != null) {
            // Để full màn hình hoặc tùy chỉnh kích thước tùy mày
            g2.drawImage(endingGif, 0, 0, gp.screenWidth, gp.screenHeight, gp);
        }

        // Vẽ thêm dòng chữ hướng dẫn
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
        g2.setColor(Color.WHITE);
        String text = "Nhấn SPACE để về Menu chính";
        java.awt.FontMetrics fm = g2.getFontMetrics();
        int textX = (gp.screenWidth - fm.stringWidth(text)) / 2;

        g2.drawString(text, textX, gp.screenHeight - 50);
    }

    @Override
    public void exit() {
        endingGif = null;
        // gp.sound.stopMusic();
    }

    @Override
    public void handleMouseClick(MouseEvent e) {
    }
}