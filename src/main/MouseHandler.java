package main;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;

// Lớp xử lý sự kiện chuột (theo dõi vị trí và click chuột trái)
public class MouseHandler implements MouseMotionListener, MouseListener {

    public volatile int mouseX, mouseY;
    public volatile boolean leftMousePressed = false;
    private GamePanel gp;

    public void setGamePanel(GamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateMousePos(e.getX(), e.getY());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        updateMousePos(e.getX(), e.getY());
    }

    private void updateMousePos(int rawX, int rawY) {
        if (gp != null) {
            double scaleX = (double) gp.getWidth() / gp.screenWidth;
            double scaleY = (double) gp.getHeight() / gp.screenHeight;
            double scale = Math.min(scaleX, scaleY);

            int scaledWidth = (int) (gp.screenWidth * scale);
            int scaledHeight = (int) (gp.screenHeight * scale);
            int xOffset = (gp.getWidth() - scaledWidth) / 2;
            int yOffset = (gp.getHeight() - scaledHeight) / 2;

            mouseX = (int) ((rawX - xOffset) / scale);
            mouseY = (int) ((rawY - yOffset) / scale);
        } else {
            mouseX = rawX;
            mouseY = rawY;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftMousePressed = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftMousePressed = false;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Không cần xử lý
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Không cần xử lý
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Không cần xử lý
    }
}
