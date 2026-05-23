package state;

import main.GamePanel;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class StateKTX extends GameState {
    private Image mapImage;
    private static final String MAP_PATH = "/maps/ktx.png";
    private static final Font HUD_FONT = new Font("Arial", Font.BOLD, 20);

    public StateKTX(GamePanel gp) {
        super(gp);
    }

    @Override
    public void enter() {
        try {
            BufferedImage src = ImageIO.read(getClass().getResourceAsStream(MAP_PATH));
            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            BufferedImage compat = gc.createCompatibleImage(gp.worldWidth, gp.worldHeight, Transparency.OPAQUE);
            Graphics2D mg = compat.createGraphics();
            mg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            mg.drawImage(src, 0, 0, gp.worldWidth, gp.worldHeight, null);
            mg.dispose();
            mapImage = compat;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exit() {
        // Không cần dọn dẹp quái vật nữa
    }

    @Override
    public void update() {
        // Chỉ cập nhật di chuyển của người chơi, không check va chạm sát thương hay Game Over
        gp.player.update();
    }

    @Override
    public void draw(Graphics2D g2) {
        int cameraX = (int) (gp.player.worldX - gp.screenWidth / 2.0);
        int cameraY = (int) (gp.player.worldY - gp.screenHeight / 2.0);
        int[] clamped = gp.clampCameraPosition(cameraX, cameraY);
        cameraX = clamped[0];
        cameraY = clamped[1];

        // Vẽ bản đồ
        g2.drawImage(mapImage,
                0, 0, gp.screenWidth, gp.screenHeight,
                cameraX, cameraY, cameraX + gp.screenWidth, cameraY + gp.screenHeight,
                null);

        // Chỉ vẽ duy nhất nhân vật người chơi
        gp.player.draw(g2, cameraX, cameraY);

        // Hiển thị UI text đơn giản, không hiển thị thanh máu hay số quái
//        g2.setColor(Color.WHITE);
//        g2.setFont(HUD_FONT);
//        g2.drawString("Che do: Kham pha khu vuc", 10, 30);
    }

    @Override
    public void handleMouseClick(MouseEvent e) {}


}