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
        try {
            checkpointImage = ImageIO.read(getClass().getResourceAsStream("/checkpoint/checkpoint.png"));
        } catch (IOException e) {
            System.out.println("LỖI: Không tìm thấy ảnh checkpoint!");
            e.printStackTrace();
        }
    }

    // Vẽ Checkpoint
    public void draw(Graphics2D g2, int playerWorldX, int playerWorldY) {
        int screenX = (int)(worldX - playerWorldX + gp.screenWidth / 2) + 500;
        int screenY = (int)(worldY - playerWorldY + gp.screenHeight / 2) + 500;

        // Only draw if on screen
        if(screenX > -80 && screenX < gp.screenWidth + 80 && screenY > -80 && screenY < gp.screenHeight + 80) {
            g2.drawImage(checkpointImage, screenX, screenY, sizeX, sizeY, null);
        }
    }
}

