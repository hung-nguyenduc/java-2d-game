package main;

import state.TachMonState;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// Lớp xử lý sự kiện bàn phím
public class KeyHandler implements KeyListener {

    // volatile: đảm bảo game thread luôn thấy giá trị mới nhất từ EDT
    public volatile boolean upPressed, downPressed, leftPressed, rightPressed, spacePressed, fPressed, kPressed, gPressed;
    public boolean key1Pressed, key2Pressed, key3Pressed, key4Pressed;
    public boolean enterPressed, exitPressed;
    public TachMonState tachMonState;
    // Xử lý khi gõ phím (không dùng cho game di chuyển)
    @Override
    public void keyTyped(KeyEvent e) {
        // Không dùng cái này cho game di chuyển
    }

    // Xử lý khi nhấn phím
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode(); // Lấy mã của phím vừa bấm

        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W)    { upPressed = true; }
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S)  { downPressed = true; }
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A)  { leftPressed = true; }
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) { rightPressed = true; }
        if (code == KeyEvent.VK_SPACE) { spacePressed = true; }
        if (code == KeyEvent.VK_F) { fPressed = true; }
        if (code == KeyEvent.VK_K) { kPressed = true; }
        if (code == KeyEvent.VK_G) { gPressed = true; }
        if (code == KeyEvent.VK_1 || code == KeyEvent.VK_NUMPAD1) { key1Pressed = true; }
        if (code == KeyEvent.VK_2 || code == KeyEvent.VK_NUMPAD2) { key2Pressed = true; }
        if (code == KeyEvent.VK_3 || code == KeyEvent.VK_NUMPAD3) { key3Pressed = true; }
        if (code == KeyEvent.VK_4 || code == KeyEvent.VK_NUMPAD4) { key4Pressed = true; }
        if (code == KeyEvent.VK_ENTER) { enterPressed = true; }
    }

    // Xử lý khi thả phím
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode(); // Khi thả phím ra thì gán lại thành false

        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W)    { upPressed = false; }
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S)  { downPressed = false; }
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A)  { leftPressed = false; }
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) { rightPressed = false; }
        if (code == KeyEvent.VK_SPACE) { spacePressed = false; }
        if (code == KeyEvent.VK_F) { fPressed = false; }
        if (code == KeyEvent.VK_K) { kPressed = false; }
        if (code == KeyEvent.VK_G) { gPressed = false; }
        if (code == KeyEvent.VK_1 || code == KeyEvent.VK_NUMPAD1) { key1Pressed = false; }
        if (code == KeyEvent.VK_2 || code == KeyEvent.VK_NUMPAD2) { key2Pressed = false; }
        if (code == KeyEvent.VK_3 || code == KeyEvent.VK_NUMPAD3) { key3Pressed = false; }
        if (code == KeyEvent.VK_4 || code == KeyEvent.VK_NUMPAD4) { key4Pressed = false; }
        if (code == KeyEvent.VK_ENTER) { enterPressed = false; }
    }
}