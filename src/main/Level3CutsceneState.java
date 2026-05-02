package main;

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

    // ── Vũ position ───────────────────────────────────────────────────────────
    private float vuX, vuY;
    private int   spriteFrame = 0;
    private boolean walkingLeft = true;

    // Cửa phải → điểm trung gian → bàn đầu (row1, mid-left, seat1)
    private static final float DOOR_X = 730f, DOOR_Y = 425f;
    private static final float MID_X  = 370f, MID_Y  = 215f;
    private static final float DESK_X = 296f, DESK_Y = 161f;
    private boolean reachedMid = false;

    // ── Room layout constants ─────────────────────────────────────────────────
    // 4 sections × 3 seats each
    // [sec][seat] = left-x of seat
    private static final int[][] SEAT_X = {
        {56,  104, 152},   // left section
        {252, 296, 340},   // mid-left
        {402, 446, 490},   // mid-right
        {580, 624, 668},   // right section
    };
    private static final int[] SEAT_W = {44, 40, 40, 42};

    // deskTop Y for 6 rows (rows 4-5 are on elevated floors)
    private static final int[] ROW_Y = {152, 208, 264, 320, 372, 426};
    private static final int DESK_H  = 18;
    private static final int CHAIR_H = 14;

    // 6 students: [row, section, seat]
    private static final int[][] STUDENTS = {
        {1, 0, 1}, {2, 1, 2}, {3, 2, 0},
        {0, 3, 1}, {4, 0, 0}, {5, 2, 1},
    };
    private static final Color[] SHIRT_COLORS = {
        new Color(42,  72, 158), new Color(158, 52,  42),  new Color(48, 118,  52),
        new Color(115, 52, 148), new Color(148, 118,  38), new Color(52,  98, 138),
    };

    // ── Fan & fade ────────────────────────────────────────────────────────────
    private float fanAngle  = 0f;
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

    @Override public void exit() {}

    // ─────────────────────────────────────────────────────────────────────────
    @Override public void update() {
        frameCount++; phaseFrame++;
        fanAngle = (fanAngle + 3.2f) % 360f;

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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);

        drawRoom(g2);
        if (phase >= PHASE_SHOW_ROOM) drawVu(g2);
        if (dialogueShown)            drawDialogue(g2);
        if (fadeAlpha > 0f) {
            g2.setColor(new Color(0, 0, 0, Math.min(255, (int)(fadeAlpha * 255))));
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        }
    }

    // =========================================================================
    // ROOM DRAWING
    // =========================================================================
    private void drawRoom(Graphics2D g2) {
        int W = gp.screenWidth, H = gp.screenHeight;

        // 1. Floor (light gray)
        g2.setColor(new Color(205, 205, 190));
        g2.fillRect(0, 0, W, H);

        // 2. Ceiling strip (dark gray, top 48px)
        g2.setColor(new Color(75, 72, 68));
        g2.fillRect(0, 0, W, 48);

        // 3. Walls (light yellow)
        Color wall = new Color(242, 228, 168);
        g2.setColor(wall);
        g2.fillRect(0, 48, 52, H - 48);   // left wall
        g2.fillRect(716, 48, W - 716, H - 48); // right wall
        g2.fillRect(0, H - 28, W, 28);    // bottom wall

        // 4. Podium platform
        g2.setColor(new Color(178, 198, 172));
        g2.fillRect(52, 48, 664, 100);
        // Podium step edge
        g2.setColor(new Color(138, 158, 132));
        g2.fillRect(52, 145, 664, 7);

        // 5. Blackboard
        drawBlackboard(g2);

        // 6. Teacher desk
        drawTeacherDesk(g2);

        // 7. Lectern
        drawLectern(g2);

        // 8. Lecturer
        drawLecturer(g2);

        // 9. Windows (left wall)
        drawWindows(g2, H);

        // 10. Door (right wall)
        drawDoor(g2, H);

        // 11. Ceiling lights (animated)
        drawCeilingLights(g2, W);

        // 12. Ceiling fans (animated, drawn last on ceiling)
        drawFans(g2);

        // 13. Elevated floor steps (before desks so desks draw on top)
        drawElevatedFloor(g2, W);

        // 14. All desks & chairs
        drawAllDesks(g2);

        // 15. Students
        drawAllStudents(g2);
    }

    // ── Blackboard ────────────────────────────────────────────────────────────
    private void drawBlackboard(Graphics2D g2) {
        int bx = 165, by = 54, bw = 445, bh = 66;
        // Board surface (dark green)
        g2.setColor(new Color(28, 82, 42));
        g2.fillRect(bx, by, bw, bh);
        // Board frame (dark brown)
        g2.setColor(new Color(88, 58, 30));
        g2.setStroke(new BasicStroke(3.5f));
        g2.drawRect(bx, by, bw, bh);
        g2.setStroke(new BasicStroke(1f));
        // Subtle highlight top edge
        g2.setColor(new Color(45, 112, 58));
        g2.fillRect(bx+1, by+1, bw-2, 2);
        // Title text
        g2.setColor(new Color(215, 252, 220));
        g2.setFont(new Font("Arial", Font.BOLD, 23));
        FontMetrics fm = g2.getFontMetrics();
        String title = "Giải tích I";
        g2.drawString(title, bx + (bw - fm.stringWidth(title)) / 2, by + 40);
        // Formula (chalk style)
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.setColor(new Color(188, 228, 195, 175));
        g2.drawString("∫ f(x) dx = F(b) − F(a)", bx + 20, by + 58);
        // Chalk tray
        g2.setColor(new Color(145, 115, 75));
        g2.fillRect(bx, by + bh, bw, 5);
    }

    // ── Teacher's desk ────────────────────────────────────────────────────────
    private void drawTeacherDesk(Graphics2D g2) {
        int dx = 108, dy = 102, dw = 118, dh = 40;
        // Front face (dark)
        g2.setColor(new Color(108, 68, 32));
        g2.fillRect(dx, dy + dh - 7, dw, 7);
        // Surface
        g2.setColor(new Color(152, 98, 55));
        g2.fillRect(dx, dy, dw, dh - 7);
        // Surface highlight
        g2.setColor(new Color(168, 112, 68));
        g2.fillRect(dx, dy, dw, 4);
        // Papers
        g2.setColor(new Color(248, 245, 238));
        g2.fillRect(dx + 6, dy + 5, 38, 24);
        g2.setColor(new Color(222, 220, 212));
        g2.fillRect(dx + 10, dy + 9, 28, 16);
        // Pen line on paper
        g2.setColor(new Color(30, 60, 140));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawLine(dx+13, dy+13, dx+32, dy+13);
        g2.drawLine(dx+13, dy+17, dx+28, dy+17);
        g2.setStroke(new BasicStroke(1f));
        // Mug
        g2.setColor(new Color(185, 82, 52));
        g2.fillRoundRect(dx + 62, dy + 6, 14, 14, 4, 4);
        g2.setColor(new Color(215, 112, 82));
        g2.fillRoundRect(dx + 63, dy + 7, 12, 9, 3, 3);
    }

    // ── Lectern ───────────────────────────────────────────────────────────────
    private void drawLectern(Graphics2D g2) {
        g2.setColor(new Color(105, 78, 48));
        g2.fillRect(352, 112, 52, 33);
        g2.setColor(new Color(125, 94, 60));
        g2.fillRect(352, 112, 52, 26);
        g2.setColor(new Color(148, 110, 72));
        g2.fillRect(346, 110, 64, 10);
    }

    // ── Lecturer with glasses ─────────────────────────────────────────────────
    private void drawLecturer(Graphics2D g2) {
        int cx = 155, cy = 128;
        // Body (suit)
        g2.setColor(new Color(48, 68, 138));
        g2.fillRoundRect(cx - 11, cy - 2, 22, 22, 5, 5);
        // Collar / shirt
        g2.setColor(new Color(245, 245, 245));
        g2.fillRect(cx - 4, cy - 2, 8, 7);
        // Head
        g2.setColor(new Color(215, 175, 138));
        g2.fillOval(cx - 8, cy - 19, 16, 16);
        // Hair (dark)
        g2.setColor(new Color(28, 22, 18));
        g2.fillOval(cx - 8, cy - 19, 16, 8);
        // Glasses frames
        g2.setColor(new Color(42, 38, 32));
        g2.setStroke(new BasicStroke(1.4f));
        g2.drawOval(cx - 8, cy - 12, 6, 5);   // left lens
        g2.drawOval(cx + 2, cy - 12, 6, 5);   // right lens
        g2.drawLine(cx - 2, cy - 10, cx + 2, cy - 10); // bridge
        g2.drawLine(cx - 14, cy - 10, cx - 8, cy - 10); // left arm
        g2.drawLine(cx + 8, cy - 10, cx + 14, cy - 10); // right arm
        g2.setStroke(new BasicStroke(1f));
    }

    // ── Windows (left wall, 3 windows with purple curtains) ───────────────────
    private void drawWindows(Graphics2D g2, int H) {
        int[] wyArr = {155, 285, 415};
        for (int wy : wyArr) {
            int wx = 6, ww = 42, wh = 92;
            // Outer frame
            g2.setColor(new Color(198, 185, 145));
            g2.fillRect(wx, wy, ww, wh);
            // Glass
            g2.setColor(new Color(165, 218, 248));
            g2.fillRect(wx + 4, wy + 4, ww - 8, wh - 8);
            // Window cross-bar
            g2.setColor(new Color(160, 148, 118));
            g2.fillRect(wx + 4, wy + 4 + (wh-8)/2, ww - 8, 2);
            g2.fillRect(wx + 4 + (ww-8)/2, wy + 4, 2, wh - 8);
            // Curtain rail
            g2.setColor(new Color(135, 105, 148));
            g2.fillRect(wx + 2, wy + 2, ww - 4, 4);
            // Left curtain panel (light purple)
            g2.setColor(new Color(198, 162, 222, 210));
            g2.fillRect(wx + 4, wy + 6, (ww - 10) / 2, wh - 10);
            // Right curtain panel
            g2.fillRect(wx + 4 + (ww - 10) / 2 + 2, wy + 6, (ww - 10) / 2, wh - 10);
            // Curtain fold lines
            g2.setColor(new Color(168, 128, 192, 130));
            g2.setStroke(new BasicStroke(1f));
            for (int fold = 0; fold < 2; fold++) {
                int foldX = wx + 4 + fold * 6 + 3;
                g2.drawLine(foldX, wy + 6, foldX, wy + 6 + wh - 10);
                int foldX2 = wx + 4 + (ww-10)/2 + 2 + fold * 6 + 3;
                g2.drawLine(foldX2, wy + 6, foldX2, wy + 6 + wh - 10);
            }
            g2.setStroke(new BasicStroke(1f));
        }
    }

    // ── Door (right wall, dark blue-green) ────────────────────────────────────
    private void drawDoor(Graphics2D g2, int H) {
        int dx = 718, dy = 365, dw = 46, dh = 125;
        // Frame
        g2.setColor(new Color(45, 35, 22));
        g2.fillRect(dx - 4, dy - 5, dw + 4, dh + 5);
        // Door open when Vũ enters
        boolean open = (phase >= PHASE_SHOW_ROOM);
        if (open) {
            // Ajar sliver
            g2.setColor(new Color(18, 52, 118));
            g2.fillRect(dx, dy, 12, dh);
            // Hallway light
            g2.setPaint(new GradientPaint(dx + 12, dy + dh/2,
                new Color(255, 238, 195, 65), dx + 90, dy + dh/2,
                new Color(255, 238, 195, 0)));
            g2.fillRect(dx + 12, dy, 88, dh);
        } else {
            g2.setColor(new Color(18, 52, 118));
            g2.fillRect(dx, dy, dw, dh);
            g2.setColor(new Color(14, 42, 98));
            g2.fillRoundRect(dx + 4, dy + 8,  dw - 8, dh/2 - 14, 3, 3);
            g2.fillRoundRect(dx + 4, dy + dh/2 + 6, dw - 8, dh/2 - 14, 3, 3);
        }
        // Knob
        g2.setColor(new Color(188, 158, 52));
        g2.fillOval(dx + 2, dy + dh/2 - 6, 10, 10);
        g2.setColor(new Color(215, 188, 80));
        g2.fillOval(dx + 4, dy + dh/2 - 4, 6, 6);
    }

    // ── Ceiling lights (3 rows, animated flicker) ─────────────────────────────
    private void drawCeilingLights(Graphics2D g2, int W) {
        float flicker = (float)(0.92 + 0.08 * Math.sin(frameCount * 0.072));
        int[] lightXs = {128, 384, 638};
        int[] rowYs   = {6, 18, 30};
        for (int ly : rowYs) {
            for (int lx : lightXs) {
                // Tube
                g2.setColor(new Color(255, 252, 225, (int)(248 * flicker)));
                g2.fillRoundRect(lx - 46, ly, 92, 8, 4, 4);
                // Glow halo
                g2.setColor(new Color(255, 250, 205, (int)(28 * flicker)));
                g2.fillOval(lx - 58, ly - 4, 116, 20);
            }
        }
    }

    // ── Ceiling fans (2 fans, rotating) ───────────────────────────────────────
    private void drawFans(Graphics2D g2) {
        int[] fxArr = {212, 558};
        int   fy    = 24;
        for (int fx : fxArr) {
            // Shadow on ceiling
            g2.setColor(new Color(55, 52, 48, 60));
            g2.fillOval(fx - 26, fy - 26, 52, 52);

            Graphics2D g = (Graphics2D) g2.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.translate(fx, fy);
            g.rotate(Math.toRadians(fanAngle));
            // 3 blades
            g.setColor(new Color(85, 150, 88));
            for (int b = 0; b < 3; b++) {
                g.rotate(Math.toRadians(120));
                g.fillRoundRect(-4, -24, 9, 24, 5, 5);
            }
            g.dispose();
            // Hub
            g2.setColor(new Color(62, 62, 68));
            g2.fillOval(fx - 5, fy - 5, 10, 10);
            g2.setColor(new Color(88, 88, 95));
            g2.fillOval(fx - 3, fy - 3, 6, 6);
        }
    }

    // ── Elevated floor sections (rows 5-6 are higher) ────────────────────────
    private void drawElevatedFloor(Graphics2D g2, int W) {
        // Step before row 5
        g2.setColor(new Color(155, 155, 142));
        g2.fillRect(52, 354, 664, 10);
        // Level 1 elevated floor
        g2.setColor(new Color(192, 192, 178));
        g2.fillRect(52, 364, 664, 68);
        // Step before row 6
        g2.setColor(new Color(142, 142, 130));
        g2.fillRect(52, 412, 664, 10);
        // Level 2 elevated floor
        g2.setColor(new Color(180, 180, 167));
        g2.fillRect(52, 422, 664, 126);
    }

    // ── All 6 rows × 4 sections × 3 seats ────────────────────────────────────
    private void drawAllDesks(Graphics2D g2) {
        for (int row = 0; row < 6; row++) {
            int dy   = ROW_Y[row];
            int cy   = dy + DESK_H + 3;
            for (int sec = 0; sec < 4; sec++) {
                for (int seat = 0; seat < 3; seat++) {
                    int x = SEAT_X[sec][seat];
                    int w = SEAT_W[sec];
                    drawDesk(g2, x, dy, w);
                    drawChair(g2, x, cy, w);
                }
            }
        }
    }

    private void drawDesk(Graphics2D g2, int x, int y, int w) {
        // Front face (shadow)
        g2.setColor(new Color(172, 130, 75));
        g2.fillRect(x, y + DESK_H - 5, w, 5);
        // Surface (light brown)
        g2.setColor(new Color(212, 168, 108));
        g2.fillRect(x, y, w, DESK_H - 5);
        // Highlight top edge
        g2.setColor(new Color(230, 190, 132));
        g2.fillRect(x, y, w, 3);
        // Right side edge
        g2.setColor(new Color(188, 145, 85));
        g2.drawLine(x + w - 1, y, x + w - 1, y + DESK_H);
    }

    private void drawChair(Graphics2D g2, int x, int y, int w) {
        int cw = w - 6, cx2 = x + 3;
        // Chair seat (dark navy blue)
        g2.setColor(new Color(22, 52, 138));
        g2.fillRoundRect(cx2, y, cw, CHAIR_H, 4, 4);
        // Chair highlight (top strip)
        g2.setColor(new Color(40, 72, 168));
        g2.fillRoundRect(cx2, y, cw, 4, 4, 4);
        // Chair legs (small)
        g2.setColor(new Color(55, 55, 60));
        g2.fillRect(cx2 + 2, y + CHAIR_H, 3, 3);
        g2.fillRect(cx2 + cw - 5, y + CHAIR_H, 3, 3);
    }

    // ── 6 students at fixed random seats ─────────────────────────────────────
    private void drawAllStudents(Graphics2D g2) {
        for (int i = 0; i < STUDENTS.length; i++) {
            int row = STUDENTS[i][0], sec = STUDENTS[i][1], seat = STUDENTS[i][2];
            int sx  = SEAT_X[sec][seat];
            int sw  = SEAT_W[sec];
            int dy  = ROW_Y[row];
            drawStudent(g2, sx, sw, dy, SHIRT_COLORS[i]);
        }
    }

    private void drawStudent(Graphics2D g2, int sx, int sw, int deskY, Color shirtColor) {
        int chairY = deskY + DESK_H + 3;
        int cx     = sx + sw / 2;
        int cy     = chairY + CHAIR_H / 2;
        // Body
        g2.setColor(shirtColor);
        g2.fillRoundRect(cx - 8, cy - 3, 16, 15, 4, 4);
        // Head
        g2.setColor(new Color(220, 180, 145));
        g2.fillOval(cx - 7, cy - 15, 14, 13);
        // Hair
        g2.setColor(new Color(35, 25, 18));
        g2.fillOval(cx - 7, cy - 15, 14, 7);
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
            g2.setColor(new Color(0, 0, 0, 145));
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
        // Shadow
        g2.setColor(new Color(0, 0, 0, 72));
        g2.fillRoundRect(bx + 3, by + 4, bw, bh, 16, 16);
        // Background
        g2.setColor(new Color(8, 6, 4, 218));
        g2.fillRoundRect(bx, by, bw, bh, 16, 16);
        // Border
        g2.setColor(new Color(190, 158, 78));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(bx, by, bw, bh, 16, 16);
        g2.setColor(new Color(190, 158, 78, 32));
        g2.drawRoundRect(bx + 5, by + 5, bw - 10, bh - 10, 12, 12);
        g2.setStroke(new BasicStroke(1f));
        // Text
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.setColor(Color.WHITE);
        g2.drawString("Đến giảng đường, Vũ quyết tâm lấy A+ giải tích nên đã", bx + 16, by + 35);
        g2.drawString("lên thẳng bàn đầu ngồi.", bx + 16, by + 58);
        // Blinking hint
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
