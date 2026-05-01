package main;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class LevelCompleteState extends GameState {

    private final int level;
    private final GameState nextState;
    private int frameCount = 0;

    // Không nhận phím trong 90 frame đầu (1.5s) để tránh trigger ngay lúc màn game kết thúc
    private static final int INPUT_GRACE = 90;

    // Color scheme cho từng màn
    private final Color bgColor;
    private final Color panelColor;
    private final Color accentColor;
    private final Color titleColor;
    private final Color glowColor;
    private final Color starColor;

    public LevelCompleteState(GamePanel gp, int level, GameState nextState) {
        super(gp);
        this.level = level;
        this.nextState = nextState;

        if (level == 1) {
            // Màn 1: campus HUST ngoài trời → xanh lá rừng + vàng đồng
            bgColor    = new Color(8, 22, 8);
            panelColor = new Color(15, 40, 15, 235);
            accentColor = new Color(195, 155, 40);
            titleColor  = new Color(255, 220, 50);
            glowColor   = new Color(60, 140, 30);
            starColor   = new Color(180, 220, 100, 120);
        } else {
            // Màn 2: overlay navy đậm → xanh biển thẫm + cyan
            bgColor    = new Color(4, 4, 22);
            panelColor = new Color(6, 6, 48, 235);
            accentColor = new Color(0, 210, 230);
            titleColor  = new Color(80, 255, 255);
            glowColor   = new Color(30, 70, 190);
            starColor   = new Color(100, 180, 255, 120);
        }
    }

    @Override
    public void enter() {
        frameCount = 0;
    }

    @Override
    public void exit() {}

    @Override
    public void update() {
        frameCount++;
        if (frameCount > INPUT_GRACE && gp.keyH.spacePressed) {
            gp.setState(nextState);
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        int W = gp.screenWidth;
        int H = gp.screenHeight;

        // Nền toàn màn
        g2.setColor(bgColor);
        g2.fillRect(0, 0, W, H);

        // Các chấm sao nền
        drawStars(g2, W, H);

        // Panel trung tâm
        int pW = 610, pH = 330;
        int pX = (W - pW) / 2;
        int pY = (H - pH) / 2;

        // Hào quang phía sau panel
        drawGlow(g2, pX + pW / 2, pY + pH / 2, pW, pH);

        // Nền panel
        g2.setColor(panelColor);
        g2.fill(new RoundRectangle2D.Double(pX, pY, pW, pH, 28, 28));

        // Viền ngoài panel
        g2.setColor(accentColor);
        g2.setStroke(new BasicStroke(3f));
        g2.draw(new RoundRectangle2D.Double(pX, pY, pW, pH, 28, 28));

        // Viền trong mờ
        g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 55));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Double(pX + 9, pY + 9, pW - 18, pH - 18, 20, 20));
        g2.setStroke(new BasicStroke(1f));

        // 4 góc kim cương
        drawDiamond(g2, pX + 22, pY + 22);
        drawDiamond(g2, pX + pW - 22, pY + 22);
        drawDiamond(g2, pX + 22, pY + pH - 22);
        drawDiamond(g2, pX + pW - 22, pY + pH - 22);

        // Tiêu đề "CHÚC MỪNG!"
        g2.setFont(new Font("Arial", Font.BOLD, 50));
        FontMetrics fm = g2.getFontMetrics();
        String title = "CHÚC MỪNG!";
        int tX = pX + (pW - fm.stringWidth(title)) / 2;
        int tY = pY + 85;
        // Bóng chữ
        g2.setColor(new Color(0, 0, 0, 160));
        g2.drawString(title, tX + 3, tY + 3);
        // Chữ chính
        g2.setColor(titleColor);
        g2.drawString(title, tX, tY);

        // Kẻ ngang phân cách
        g2.setColor(accentColor);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(pX + 45, pY + 105, pX + pW - 45, pY + 105);
        g2.setStroke(new BasicStroke(1f));

        // Dòng chính
        g2.setFont(new Font("Arial", Font.BOLD, 21));
        fm = g2.getFontMetrics();
        String msg = "Bạn đã hoàn thành Chặng " + level + "!";
        g2.setColor(Color.WHITE);
        g2.drawString(msg, pX + (pW - fm.stringWidth(msg)) / 2, pY + 155);

        // Dòng phụ
        g2.setFont(new Font("Arial", Font.PLAIN, 15));
        fm = g2.getFontMetrics();
        String sub = (level == 1)
            ? "Tiếp tục chiến đấu - Chặng 2 đang chờ!"
            : "Xuất sắc! Hãy chuẩn bị cho những gì tiếp theo!";
        g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 210));
        g2.drawString(sub, pX + (pW - fm.stringWidth(sub)) / 2, pY + 188);

        // Kẻ ngang thứ hai
        g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 70));
        g2.drawLine(pX + 70, pY + 215, pX + pW - 70, pY + 215);

        // Dòng nhắc nhở (nhấp nháy sau grace period)
        String hint;
        Color hintColor;
        if (frameCount <= INPUT_GRACE) {
            hint = "Chuẩn bị...";
            hintColor = Color.GRAY;
        } else if ((frameCount / 28) % 2 == 0) {
            hint = (level == 1) ? "Nhấn SPACE để vào Chặng 2" : "Nhấn SPACE để quay về Menu";
            hintColor = Color.WHITE;
        } else {
            hint = null;
            hintColor = null;
        }

        if (hint != null) {
            g2.setFont(new Font("Arial", Font.ITALIC, 14));
            fm = g2.getFontMetrics();
            g2.setColor(hintColor);
            g2.drawString(hint, pX + (pW - fm.stringWidth(hint)) / 2, pY + 295);
        }

        // Thanh tiến trình đếm ngược grace period
        if (frameCount <= INPUT_GRACE) {
            int barW = pW - 120;
            int barX = pX + 60;
            int barY = pY + 280;
            g2.setColor(new Color(60, 60, 60, 180));
            g2.fillRoundRect(barX, barY, barW, 8, 8, 8);
            int filled = (int)((double) frameCount / INPUT_GRACE * barW);
            g2.setColor(accentColor);
            g2.fillRoundRect(barX, barY, filled, 8, 8, 8);
        }
    }

    private void drawGlow(Graphics2D g2, int cx, int cy, int w, int h) {
        for (int i = 10; i >= 1; i--) {
            int alpha = i * 3;
            int expand = i * 9;
            g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), alpha));
            g2.fillOval(cx - w / 2 - expand, cy - h / 2 - expand, w + expand * 2, h + expand * 2);
        }
    }

    private void drawStars(Graphics2D g2, int W, int H) {
        int[][] pts = {
            {45,35},{130,75},{220,25},{370,55},{500,18},{640,65},{720,38},{760,85},
            {90,490},{270,470},{420,520},{590,505},{680,535},{740,480},
            {25,200},{25,380},{770,160},{770,400},{380,15},{380,545},{190,300},{570,295}
        };
        for (int[] p : pts) {
            int pulse = (p[0] * 3 + p[1] + frameCount / 25) % 4;
            int sz = (pulse < 2) ? 2 : 3;
            int alpha = 80 + pulse * 20;
            g2.setColor(new Color(starColor.getRed(), starColor.getGreen(), starColor.getBlue(), alpha));
            g2.fillOval(p[0] - sz / 2, p[1] - sz / 2, sz, sz);
        }
    }

    private void drawDiamond(Graphics2D g2, int cx, int cy) {
        g2.setColor(accentColor);
        int r = 7;
        int[] px = {cx, cx + r, cx, cx - r};
        int[] py = {cy - r, cy, cy + r, cy};
        g2.fillPolygon(px, py, 4);
        g2.setColor(new Color(255, 255, 255, 90));
        g2.fillPolygon(new int[]{cx, cx + 3, cx}, new int[]{cy - r, cy, cy}, 3);
    }

    @Override
    public void handleMouseClick(MouseEvent e) {
        if (frameCount > INPUT_GRACE) {
            gp.setState(nextState);
        }
    }
}