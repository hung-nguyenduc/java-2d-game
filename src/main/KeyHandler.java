package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// Lớp xử lý sự kiện bàn phím
public class KeyHandler implements KeyListener {

    // volatile: đảm bảo game thread luôn thấy giá trị mới nhất từ EDT
    public volatile boolean upPressed, downPressed, leftPressed, rightPressed, spacePressed;

    // Xử lý khi gõ phím (không dùng cho game di chuyển)
    @Override
    public void keyTyped(KeyEvent e) {
        // Không dùng cái này cho game di chuyển
    }

    // Xử lý khi nhấn phím
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode(); // Lấy mã của phím vừa bấm

        if (code == KeyEvent.VK_UP)    { upPressed = true; }
        if (code == KeyEvent.VK_DOWN)  { downPressed = true; }
        if (code == KeyEvent.VK_LEFT)  { leftPressed = true; }
        if (code == KeyEvent.VK_RIGHT) { rightPressed = true; }
        if (code == KeyEvent.VK_SPACE) { spacePressed = true; }
    }

    // Xử lý khi thả phím
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode(); // Khi thả phím ra thì gán lại thành false

        if (code == KeyEvent.VK_UP)    { upPressed = false; }
        if (code == KeyEvent.VK_DOWN)  { downPressed = false; }
        if (code == KeyEvent.VK_LEFT)  { leftPressed = false; }
        if (code == KeyEvent.VK_RIGHT) { rightPressed = false; }
        if (code == KeyEvent.VK_SPACE) { spacePressed = false; }
    }
}