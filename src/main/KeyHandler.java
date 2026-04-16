package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// Lớp xử lý sự kiện bàn phím
public class KeyHandler implements KeyListener {

    public boolean upPressed, downPressed, leftPressed, rightPressed;

    // Xử lý khi gõ phím (không dùng cho game di chuyển)
    @Override
    public void keyTyped(KeyEvent e) {
        // Không dùng cái này cho game di chuyển
    }

    // Xử lý khi nhấn phím
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode(); // Lấy mã của phím vừa bấm

        if (code == KeyEvent.VK_W) { upPressed = true; }
        if (code == KeyEvent.VK_S) { downPressed = true; }
        if (code == KeyEvent.VK_A) { leftPressed = true; }
        if (code == KeyEvent.VK_D) { rightPressed = true; }
    }

    // Xử lý khi thả phím
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode(); // Khi thả phím ra thì gán lại thành false

        if (code == KeyEvent.VK_W) { upPressed = false; }
        if (code == KeyEvent.VK_S) { downPressed = false; }
        if (code == KeyEvent.VK_A) { leftPressed = false; }
        if (code == KeyEvent.VK_D) { rightPressed = false; }
    }
}