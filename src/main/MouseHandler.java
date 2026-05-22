package main;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;

// Lớp xử lý sự kiện chuột (theo dõi vị trí và click chuột trái)
public class MouseHandler implements MouseMotionListener, MouseListener {

    // volatile: đảm bảo game thread luôn thấy giá trị mới nhất từ EDT
    public volatile int mouseX, mouseY;
    public volatile boolean leftMousePressed = false;

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
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
