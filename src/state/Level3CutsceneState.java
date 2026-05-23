package state;

import main.GamePanel;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class Level3CutsceneState extends GameState {

    // ── Phases ────────────────────────────────────────────────────────────────
    private static final int PHASE_FADE_IN   = 0;
    private static final int PHASE_SHOW_ROOM = 1;
    private static final int PHASE_VU_WALK   = 2;
    private static final int PHASE_VU_SIT    = 3;
    private static final int PHASE_DIALOGUE  = 4;
    private static final int PHASE_FADE_OUT  = 5;

    private int phase = PHASE_FADE_IN;
    private int frameCount = 0;
    private int phaseFrame = 0;

    // ── Sprites ───────────────────────────────────────────────────────────────
    private final BufferedImage[] walkLeft = new BufferedImage[4];
    private final BufferedImage[] walkUp   = new BufferedImage[4];
    private BufferedImage sitSprite;
    private BufferedImage roomImage;

    // ── Vũ position ───────────────────────────────────────────────────────────
    private float vuX, vuY;
    private int   spriteFrame = 0;
    private boolean walkingLeft = true;

    // Đường đi: cửa phải (giữa hành lang) → giữa phòng → bàn đầu (khu trái, hàng trước)
    // Toạ độ tham chiếu khớp với giang-duong.png khi scale về screen 768x576
    private static final float DOOR_X = 720f, DOOR_Y = 360f;
    private static final float MID_X  = 420f, MID_Y  = 300f;
    private static final float DESK_X = 230f, DESK_Y = 215f;
    private boolean reachedMid = false;

    // ── Fade ──────────────────────────────────────────────────────────────────
    private float fadeAlpha = 1.0f;
    private boolean dialogueShown = false;

    private final GameState nextState;

    public Level3CutsceneState(GamePanel gp, GameState nextState) {
        super(gp);
        this.nextState = nextState;
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Override public void enter() {
        loadSprites();
        loadRoomImage();
        vuX = DOOR_X; vuY = DOOR_Y;
        phase = PHASE_FADE_IN; phaseFrame = 0; frameCount = 0;
        fadeAlpha = 1f; spriteFrame = 0;
        dialogueShown = false; reachedMid = false; walkingLeft = true;
    }

    private void loadSprites() {
        try {
            for (int i = 1; i <= 4; i++) {
                InputStream l = getClass().getResourceAsStream("/player/character_move_left (" + i + ").png");
                if (l != null) walkLeft[i-1] = ImageIO.read(l);
                InputStream u = getClass().getResourceAsStream("/player/character_move_up (" + i + ").png");
                if (u != null) walkUp[i-1]   = ImageIO.read(u);
            }
            InputStream s = getClass().getResourceAsStream("/player/character_stand_front (1).png");
            if (s != null) sitSprite = ImageIO.read(s);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadRoomImage() {
        try {
            InputStream is = getClass().getResourceAsStream("/maps/giang-duong.png");
            if (is != null) {
                BufferedImage src = ImageIO.read(is);
                roomImage = new BufferedImage(gp.screenWidth, gp.screenHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = roomImage.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(src, 0, 0, gp.screenWidth, gp.screenHeight, null);
                g.dispose();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override public void exit() {}

    // ─────────────────────────────────────────────────────────────────────────
    @Override public void update() {
        frameCount++; phaseFrame++;

        switch (phase) {
            case PHASE_FADE_IN:
                fadeAlpha = Math.max(0f, 1f - phaseFrame / 60f);
                if (phaseFrame >= 60) nextPhase();
                break;
            case PHASE_SHOW_ROOM:
                if (phaseFrame >= 80) nextPhase();
                break;
            case PHASE_VU_WALK:
                moveVu();
                break;
            case PHASE_VU_SIT:
                if (phaseFrame >= 50) { dialogueShown = true; nextPhase(); }
                break;
            case PHASE_DIALOGUE:
                if (gp.keyH.spacePressed) nextPhase();
                break;
            case PHASE_FADE_OUT:
                fadeAlpha = Math.min(1f, phaseFrame / 60f);
                if (phaseFrame >= 60) gp.setState(nextState);
                break;
        }
    }

    private void moveVu() {
        float tx = reachedMid ? DESK_X : MID_X;
        float ty = reachedMid ? DESK_Y : MID_Y;
        float dx = tx - vuX, dy = ty - vuY;
        float dist = (float) Math.sqrt(dx*dx + dy*dy);
        float speed = 2.0f;
        if (dist > speed) {
            vuX += dx/dist * speed; vuY += dy/dist * speed;
            if (frameCount % 10 == 0) spriteFrame = (spriteFrame + 1) % 4;
            walkingLeft = Math.abs(dx) >= Math.abs(dy);
        } else {
            vuX = tx; vuY = ty;
            if (!reachedMid) reachedMid = true;
            else nextPhase();
        }
    }

    private void nextPhase() { phase++; phaseFrame = 0; }

    // ─────────────────────────────────────────────────────────────────────────
    @Override public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);

        // Vẽ thẳng ảnh giang-duong.png làm background → cutscene giống y hệt file
        if (roomImage != null) {
            g2.drawImage(roomImage, 0, 0, null);
        } else {
            // Fallback nếu load ảnh thất bại
            g2.setColor(new Color(205, 205, 190));
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        }

        if (phase >= PHASE_SHOW_ROOM) drawVu(g2);
        if (dialogueShown)            drawDialogue(g2);
        if (fadeAlpha > 0f) {
            g2.setColor(new Color(0, 0, 0, Math.min(255, (int)(fadeAlpha * 255))));
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        }
    }

    // =========================================================================
    // VŨ CHARACTER
    // =========================================================================
    private void drawVu(Graphics2D g2) {
        boolean sitting = (phase >= PHASE_VU_SIT);
        BufferedImage sprite;
        if      (sitting)     sprite = sitSprite;
        else if (walkingLeft) sprite = (walkLeft[0] != null) ? walkLeft[spriteFrame % 4] : null;
        else                  sprite = (walkUp[0]   != null) ? walkUp  [spriteFrame % 4] : null;

        int sx = (int) vuX - 24, sy = (int) vuY - 24;
        if (sprite != null) {
            g2.drawImage(sprite, sx, sy, 48, 48, null);
        } else {
            g2.setColor(new Color(65, 95, 160));
            g2.fillRoundRect(sx + 8, sy + 17, 28, 31, 8, 8);
            g2.setColor(new Color(218, 175, 138));
            g2.fillOval(sx + 12, sy + 1, 24, 24);
        }

        if (phase >= PHASE_VU_WALK) {
            g2.setFont(new Font("Arial", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            String name = "Vũ";
            int nw = fm.stringWidth(name);
            g2.setColor(new Color(0, 0, 0, 165));
            g2.drawString(name, (int) vuX - nw/2 + 1, (int) vuY - 27);
            g2.setColor(new Color(255, 228, 80));
            g2.drawString(name, (int) vuX - nw/2,     (int) vuY - 28);
        }
    }

    // =========================================================================
    // DIALOGUE BOX
    // =========================================================================
    private void drawDialogue(Graphics2D g2) {
        int W = gp.screenWidth, H = gp.screenHeight;
        int bx = 28, by = H - 118, bw = W - 56, bh = 98;
        g2.setColor(new Color(0, 0, 0, 72));
        g2.fillRoundRect(bx + 3, by + 4, bw, bh, 16, 16);
        g2.setColor(new Color(8, 6, 4, 218));
        g2.fillRoundRect(bx, by, bw, bh, 16, 16);
        g2.setColor(new Color(190, 158, 78));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(bx, by, bw, bh, 16, 16);
        g2.setColor(new Color(190, 158, 78, 32));
        g2.drawRoundRect(bx + 5, by + 5, bw - 10, bh - 10, 12, 12);
        g2.setStroke(new BasicStroke(1f));
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.setColor(Color.WHITE);
        g2.drawString("Đến giảng đường, Vũ quyết tâm lấy A+ giải tích nên đã", bx + 16, by + 35);
        g2.drawString("lên thẳng bàn đầu ngồi.", bx + 16, by + 58);
        if ((frameCount / 28) % 2 == 0) {
            g2.setFont(new Font("Arial", Font.ITALIC, 12));
            g2.setColor(new Color(190, 158, 78, 208));
            String hint = "Nhấn SPACE để tiếp tục ▶";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(hint, bx + bw - fm.stringWidth(hint) - 14, by + bh - 10);
        }
    }

    @Override public void handleMouseClick(MouseEvent e) {
        if (phase == PHASE_DIALOGUE) nextPhase();
    }
}