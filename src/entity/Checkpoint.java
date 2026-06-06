package entity;

import main.GamePanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

// Lớp đại diện cho checkpoint trong game
public class Checkpoint {
    GamePanel gp;
    public int worldX, worldY;
    public BufferedImage checkpointImage;
    private static BufferedImage cachedCheckpointImage = null;
    private static boolean isCheckpointLoaded = false;
    public int sizeX = 30;
    public int sizeY = 80;

    // Constructor: Khởi tạo Checkpoint tại vị trí chỉ định
    public Checkpoint(GamePanel gp, int startX, int startY) {
        this.gp = gp;
        this.worldX = startX;
        this.worldY = startY;

        getCheckpointImage();
    }

    // Tải hình ảnh của Checkpoint
    public void getCheckpointImage() {
        if (isCheckpointLoaded) {
            checkpointImage = cachedCheckpointImage;
            return;
        }
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/checkpoint/checkpoint.png");
            if (is != null) {
                cachedCheckpointImage = ImageIO.read(is);
            } else {
                System.err.println("Không tìm thấy ảnh checkpoint /checkpoint/checkpoint.png");
            }
        } catch (Exception e) {
            System.out.println("LỖI: Không thể nạp ảnh checkpoint!");
            e.printStackTrace();
        }
        isCheckpointLoaded = true;
        checkpointImage = cachedCheckpointImage;
    }

    // Vẽ Checkpoint
    public void draw(Graphics2D g2, double playerWorldX, double playerWorldY) {
        int screenX = (int)(worldX - playerWorldX + gp.screenWidth / 2);
        int screenY = (int)(worldY - playerWorldY + gp.screenHeight / 2);

        // Only draw if on screen
        if(screenX > -80 && screenX < gp.screenWidth + 80 && screenY > -80 && screenY < gp.screenHeight + 80) {
            g2.drawImage(checkpointImage, screenX, screenY, sizeX, sizeY, null);
        }
    }
}

