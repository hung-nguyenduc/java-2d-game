package entity;

import java.awt.*;

// Cửa 2 cánh (sliding) — dùng làm cửa lớp học ở Level3
public class Door {
    public final int worldX, worldY;
    public final int width, height;

    public enum State { CLOSED, OPENING, OPEN, CLOSING }
    private State state = State.CLOSED;
    private double openProgress = 0.0;          // 0 = đóng, 1 = mở hoàn toàn
    private static final double OPEN_SPEED = 0.025; // ~40 frame để mở xong (~0.7s @60fps)

    private static final Color FRAME_COLOR = new Color(45, 25, 10);
    private static final Color PANEL_LIGHT = new Color(195, 140, 80);
    private static final Color PANEL_DARK  = new Color(125, 75, 35);
    private static final Color PANEL_BORDER = new Color(40, 22, 8);
    private static final Color HANDLE_COLOR = new Color(255, 215, 90);
    private static final Color HANDLE_SHADOW = new Color(140, 100, 30);

    public Door(int worldX, int worldY, int width, int height) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.width = width;
        this.height = height;
    }

    public void open()  { if (state == State.CLOSED) state = State.OPENING; }
    public void close() { if (state == State.OPEN)   state = State.CLOSING; }
    public boolean isOpen()   { return state == State.OPEN; }

    public void update() {
        if (state == State.OPENING) {
            openProgress += OPEN_SPEED;
            if (openProgress >= 1.0) { openProgress = 1.0; state = State.OPEN; }
        } else if (state == State.CLOSING) {
            openProgress -= OPEN_SPEED;
            if (openProgress <= 0.0) { openProgress = 0.0; state = State.CLOSED; }
        }
    }

    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int sx = worldX - cameraX;
        int sy = worldY - cameraY;
        int frameThick = 7;
        int innerW = width - 2 * frameThick;
        int innerH = height - 2 * frameThick;
        int panelW = innerW / 2;

        // Bóng đổ phía dưới khung → cửa nổi khỏi sàn
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(sx + 4, sy + height - 2, width, 6, 6, 6);

        // Lintel/dầm trên cửa: thanh ngang dày tạo ấn tượng "khung cửa"
        g2.setColor(new Color(85, 50, 22));
        g2.fillRect(sx - 4, sy - 8, width + 8, 8);
        g2.setColor(FRAME_COLOR);
        g2.drawRect(sx - 4, sy - 8, width + 7, 7);

        // Khung cửa (gỗ tối)
        g2.setColor(FRAME_COLOR);
        g2.fillRect(sx, sy, width, frameThick);
        g2.fillRect(sx, sy + height - frameThick, width, frameThick);
        g2.fillRect(sx, sy, frameThick, height);
        g2.fillRect(sx + width - frameThick, sy, frameThick, height);

        // "Lối đi tối" bên trong cửa
        g2.setColor(new Color(18, 14, 10));
        g2.fillRect(sx + frameThick, sy + frameThick, innerW, innerH);

        // 2 cánh cửa: trượt sang 2 bên khi mở (clip trong khung)
        Shape oldClip = g2.getClip();
        g2.clipRect(sx + frameThick, sy + frameThick, innerW, innerH);

        int slide = (int) (openProgress * panelW);
        int leftPanelX  = sx + frameThick - slide;
        int rightPanelX = sx + frameThick + panelW + slide;
        int panelY = sy + frameThick;

        drawPanel(g2, leftPanelX,  panelY, panelW, innerH);
        drawPanel(g2, rightPanelX, panelY, panelW, innerH);

        // Tay nắm tròn ở cạnh trong (chỉ thấy khi cửa gần đóng)
        if (openProgress < 0.4) {
            int hSize = 8;
            int hy = panelY + innerH / 2 - hSize / 2;
            g2.setColor(HANDLE_SHADOW);
            g2.fillOval(leftPanelX + panelW - 12 + 1, hy + 1, hSize, hSize);
            g2.fillOval(rightPanelX + 4 + 1, hy + 1, hSize, hSize);
            g2.setColor(HANDLE_COLOR);
            g2.fillOval(leftPanelX + panelW - 12, hy, hSize, hSize);
            g2.fillOval(rightPanelX + 4, hy, hSize, hSize);
        }

        g2.setClip(oldClip);
    }

    private void drawPanel(Graphics2D g2, int x, int y, int w, int h) {
        GradientPaint paint = new GradientPaint(x, y, PANEL_LIGHT, x + w, y, PANEL_DARK);
        g2.setPaint(paint);
        g2.fillRect(x, y, w, h);
        g2.setColor(PANEL_BORDER);
        g2.drawRect(x, y, w - 1, h - 1);
        // Inset rectangle để trông giống panel cửa thật
        if (w > 10 && h > 14) {
            g2.drawRect(x + 4, y + 6, w - 9, h - 13);
        }
    }
}
