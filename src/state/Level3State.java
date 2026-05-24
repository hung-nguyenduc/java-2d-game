package state;

import entity.Door;
import entity.Enemy;
import main.GamePanel;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Level3State extends GameState {
    private Image mapImage;
    private static final String MAP_PATH = "/maps/classroom.png";
    private static final Font HUD_FONT_BIG = new Font("Arial", Font.BOLD, 20);
    private static final Font HUD_FONT_SMALL = new Font("Arial", Font.BOLD, 15);
    private static final Font CUTSCENE_FONT = new Font("Arial", Font.BOLD, 22);
    private static final Font DOOR_LABEL_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Color HUD_COLOR = new Color(255, 220, 80);
    private static final Color HUD_COLOR_FADED = new Color(255, 220, 80, 210);
    private static final Color FALLBACK_BG = new Color(55, 48, 38);

    // Vị trí cửa và các điểm Vũ đi qua trong cutscene (world coords)
    private static final int DOOR_X = 920, DOOR_Y = 1800, DOOR_W = 160, DOOR_H = 100;
    private static final double SPAWN_X = 960,  SPAWN_Y = 1920;            // ngoài cửa, sát rìa map
    private static final double DOOR_FRONT_X = 960, DOOR_FRONT_Y = 1900;   // ngay dưới cửa
    private static final double THROUGH_X = 960, THROUGH_Y = 1700;         // vừa qua cửa, vào trong
    private static final double SEAT_X = 960,  SEAT_Y = 540;               // dãy giữa, bàn đầu
    private static final double WALK_SPEED = 6.0;
    private static final int SIT_HOLD_FRAMES = 130; // ~2.2s ngồi yên trước khi quái spawn

    private Door door;
    private enum Phase { APPROACHING_DOOR, DOOR_OPENING, ENTERING, WALKING_TO_SEAT, SITTING, GAMEPLAY }
    private Phase phase;
    private int sittingFrames;

    public Level3State(GamePanel gp) {
        super(gp);
    }

    @Override
    public void enter() {
        try {
            InputStream is = getClass().getResourceAsStream(MAP_PATH);
            if (is != null) {
                BufferedImage src = ImageIO.read(is);
                GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice().getDefaultConfiguration();
                BufferedImage compat = gc.createCompatibleImage(gp.worldWidth, gp.worldHeight, Transparency.OPAQUE);
                Graphics2D mg = compat.createGraphics();
                mg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                mg.drawImage(src, 0, 0, gp.worldWidth, gp.worldHeight, null);
                mg.dispose();
                mapImage = compat;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        gp.enemies.clear();
        gp.killCount = 0;
        gp.player.health = gp.player.maxHealth;
        gp.player.bullets.clear();

        // Spawn Vũ ngoài cửa, hướng nhìn lên (270°)
        gp.player.worldX = SPAWN_X;
        gp.player.worldY = SPAWN_Y;
        gp.player.aimAngle = 270;

        door = new Door(DOOR_X, DOOR_Y, DOOR_W, DOOR_H);
        phase = Phase.APPROACHING_DOOR;
        sittingFrames = 0;
        // Quái chỉ spawn sau khi cutscene kết thúc
    }

    @Override
    public void exit() {
        gp.enemies.clear();
    }

    @Override
    public void update() {
        door.update();

        switch (phase) {
            case APPROACHING_DOOR:
                if (movePlayerToward(DOOR_FRONT_X, DOOR_FRONT_Y)) {
                    door.open();
                    phase = Phase.DOOR_OPENING;
                }
                break;
            case DOOR_OPENING:
                if (door.isOpen()) phase = Phase.ENTERING;
                break;
            case ENTERING:
                if (movePlayerToward(THROUGH_X, THROUGH_Y)) {
                    phase = Phase.WALKING_TO_SEAT;
                }
                break;
            case WALKING_TO_SEAT:
                if (movePlayerToward(SEAT_X, SEAT_Y)) {
                    phase = Phase.SITTING;
                    sittingFrames = 0;
                }
                break;
            case SITTING:
                sittingFrames++;
                if (sittingFrames >= SIT_HOLD_FRAMES) {
                    phase = Phase.GAMEPLAY;
                    spawnEnemies();
                }
                break;
            case GAMEPLAY:
                gp.player.update();
                for (int i = 0; i < gp.enemies.size(); i++) {
                    gp.enemies.get(i).update();
                }
                gp.checkCollisions();

                if (gp.player.health <= 0) {
                    gp.setState(new GameOverState(gp));
                    return;
                }
                if (gp.enemies.isEmpty()) {
                    gp.setState(new LevelCompleteState(gp, 3, new MenuState(gp)));
                }
                break;
        }
    }

    // Di chuyển player thẳng tới (tx, ty) với WALK_SPEED, trả true khi đã tới.
    private boolean movePlayerToward(double tx, double ty) {
        double dx = tx - gp.player.worldX;
        double dy = ty - gp.player.worldY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist <= WALK_SPEED) {
            gp.player.worldX = tx;
            gp.player.worldY = ty;
            return true;
        }
        gp.player.worldX += dx / dist * WALK_SPEED;
        gp.player.worldY += dy / dist * WALK_SPEED;
        return false;
    }

    @Override
    public void draw(Graphics2D g2) {
        int cameraX = (int) (gp.player.worldX - gp.screenWidth / 2.0);
        int cameraY = (int) (gp.player.worldY - gp.screenHeight / 2.0);
        int[] clamped = gp.clampCameraPosition(cameraX, cameraY);
        cameraX = clamped[0];
        cameraY = clamped[1];

        if (mapImage != null) {
            g2.drawImage(mapImage,
                0, 0, gp.screenWidth, gp.screenHeight,
                cameraX, cameraY, cameraX + gp.screenWidth, cameraY + gp.screenHeight,
                null);
        } else {
            g2.setColor(FALLBACK_BG);
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        }

        // Cửa vẽ trước player để player đè lên cửa khi đi qua
        door.draw(g2, cameraX, cameraY);

        // Bảng "GIẢNG ĐƯỜNG" treo trên cửa, chỉ trong giai đoạn dẫn dắt
        if (phase == Phase.APPROACHING_DOOR || phase == Phase.DOOR_OPENING) {
            drawDoorSign(g2, cameraX, cameraY);
        }

        // Bàn ghế tạm thời khi Vũ ngồi vào chỗ — vẽ trước player để player ngồi đè lên
        if (phase == Phase.SITTING) {
            drawDeskAndChair(g2, cameraX, cameraY);
        }

        gp.player.draw(g2, cameraX, cameraY);
        for (Enemy enemy : gp.enemies) {
            enemy.draw(g2, cameraX, cameraY);
        }

        // HUD chặng 3 — chỉ hiện khi đã vào gameplay
        if (phase == Phase.GAMEPLAY) {
            g2.setColor(HUD_COLOR);
            g2.setFont(HUD_FONT_BIG);
            g2.drawString("CHẶNG 3 - Giảng đường: " + gp.enemies.size() + " kẻ phá rối", 10, 30);

            g2.setFont(HUD_FONT_SMALL);
            g2.setColor(HUD_COLOR_FADED);
            String sub = "Tiêu diệt tất cả để tập trung học!";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(sub, (gp.screenWidth - fm.stringWidth(sub)) / 2, 30);
        } else {
            String hint = cutsceneHint();
            if (hint != null) {
                g2.setFont(CUTSCENE_FONT);
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(hint);
                int tx = (gp.screenWidth - tw) / 2;
                int ty = 44;
                g2.setColor(new Color(0, 0, 0, 190));
                g2.fillRoundRect(tx - 16, ty - 26, tw + 32, 36, 12, 12);
                g2.setColor(new Color(255, 235, 150));
                g2.drawString(hint, tx, ty);
            }
        }
    }

    // Bảng tên trên cửa (lúc cửa chưa mở để hướng sự chú ý)
    private void drawDoorSign(Graphics2D g2, int cameraX, int cameraY) {
        String label = "GIẢNG ĐƯỜNG";
        g2.setFont(DOOR_LABEL_FONT);
        FontMetrics fm = g2.getFontMetrics();
        int lw = fm.stringWidth(label) + 20;
        int lh = 22;
        int sx = (DOOR_X - cameraX) + DOOR_W / 2 - lw / 2;
        int sy = (DOOR_Y - cameraY) - lh - 14;

        g2.setColor(new Color(170, 120, 55));
        g2.fillRoundRect(sx, sy, lw, lh, 6, 6);
        g2.setColor(new Color(40, 22, 8));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(sx, sy, lw, lh, 6, 6);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(Color.WHITE);
        g2.drawString(label, sx + 10, sy + 16);
    }

    // Bàn + tựa ghế tạm thời quanh Vũ khi ngồi → tạo cảm giác Vũ đang ngồi vào chỗ
    private void drawDeskAndChair(Graphics2D g2, int cameraX, int cameraY) {
        int psX = (int) (gp.player.worldX - cameraX);
        int psY = (int) (gp.player.worldY - cameraY);
        // Tựa ghế phía sau Vũ (dưới màn hình theo top-down)
        g2.setColor(new Color(60, 95, 145));
        g2.fillRoundRect(psX + 12, psY + 68, 56, 16, 4, 4);
        g2.setColor(new Color(30, 50, 80));
        g2.drawRoundRect(psX + 12, psY + 68, 56, 16, 4, 4);
        // Mặt bàn phía trước Vũ (hướng bục giảng)
        g2.setColor(new Color(160, 115, 70));
        g2.fillRoundRect(psX - 10, psY - 20, 100, 18, 4, 4);
        g2.setColor(new Color(80, 50, 25));
        g2.drawRoundRect(psX - 10, psY - 20, 100, 18, 4, 4);
        // Cuốn vở mở trên bàn
        g2.setColor(new Color(245, 240, 220));
        g2.fillRect(psX + 22, psY - 16, 36, 12);
        g2.setColor(new Color(80, 80, 80));
        g2.drawLine(psX + 40, psY - 16, psX + 40, psY - 4);
    }

    private String cutsceneHint() {
        switch (phase) {
            case APPROACHING_DOOR: return "Vũ tới giảng đường...";
            case DOOR_OPENING:     return "Cửa mở...";
            case ENTERING:         return "Vũ bước vào lớp";
            case WALKING_TO_SEAT:  return "Đi lên dãy giữa, bàn đầu";
            case SITTING:          return "Ngồi xuống ghế...";
            default: return null;
        }
    }

    private void spawnEnemies() {
        gp.enemies.add(new Enemy(gp, gp.player, 300,  400, 0));
        gp.enemies.add(new Enemy(gp, gp.player, 700,  300, 1));
        gp.enemies.add(new Enemy(gp, gp.player, 1100, 600, 2));
        gp.enemies.add(new Enemy(gp, gp.player, 500,  1200, 1));
        gp.enemies.add(new Enemy(gp, gp.player, 1500, 900, 0));
        gp.enemies.add(new Enemy(gp, gp.player, 1800, 400, 2));
    }

    @Override
    public void handleMouseClick(MouseEvent e) {}
}
