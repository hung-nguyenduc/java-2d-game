package main;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

// Lớp xử lý sự kiện chuột (chỉ theo dõi vị trí để ngắm bắn)
public class MouseHandler implements MouseMotionListener {

    // volatile: đảm bảo game thread luôn thấy giá trị mới nhất từ EDT
    public volatile int mouseX, mouseY;

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
}
