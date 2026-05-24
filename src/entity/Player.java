package entity;

import main.GamePanel;
import main.KeyHandler;
import main.MouseHandler;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Player extends Entity {
    GamePanel gp;
    KeyHandler keyH;
    MouseHandler mouseH;

    // Vũ khí có thể tháo lắp tùy màn chơi
    public Weapon currentWeapon;

    private static final double DIAGONAL_FACTOR = 1.0 / Math.sqrt(2);
    private static final Font HEALTH_FONT = new Font("Arial", Font.BOLD, 11);

    public Player(GamePanel gp, KeyHandler keyH, MouseHandler mouseH) {
        this.gp = gp;
        this.keyH = keyH;
        this.mouseH = mouseH;

        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues() {
        speed = 5;
        maxHealth = 30000;
        health = maxHealth;
        direction = "down";
        currentWeapon = null; // Mặc định tay không bắt giặc, chưa có súng
    }

    public void equipWeapon(Weapon weapon) {
        this.currentWeapon = weapon;
    }

    public void spawnAtCenter() {
        this.worldX = gp.worldWidth / 2.0 - 40;
        this.worldY = gp.worldHeight / 2.0 - 40 +100;
    }

    public void getPlayerImage() {
        try {
            up1 = ImageIO.read(getClass().getResourceAsStream("/player/up1.png"));
            up2 = ImageIO.read(getClass().getResourceAsStream("/player/up2.png"));
            down1 = ImageIO.read(getClass().getResourceAsStream("/player/down1.png"));
            down2 = ImageIO.read(getClass().getResourceAsStream("/player/down2.png"));
            left1 = ImageIO.read(getClass().getResourceAsStream("/player/left1.png"));
            left2 = ImageIO.read(getClass().getResourceAsStream("/player/left2.png"));
            right1 = ImageIO.read(getClass().getResourceAsStream("/player/right1.png"));
            right2 = ImageIO.read(getClass().getResourceAsStream("/player/right2.png"));
        } catch (IOException ex) {
            throw new RuntimeException("Lỗi tải ảnh nhân vật Vũ!", ex);
        }
    }

    public void update() {
        vx = 0;
        vy = 0;

        // Xử lý nút bấm di chuyển
        if (keyH.upPressed) { vy -= speed; direction = "up"; }
        if (keyH.downPressed) { vy += speed; direction = "down"; }
        if (keyH.leftPressed) { vx -= speed; direction = "left"; }
        if (keyH.rightPressed) { vx += speed; direction = "right"; }

        // Đổi frame chân bước đi lạch bạch
        if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {
            spriteCounter++;
            if (spriteCounter > 10) {
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }
        }

        // Đồng bộ tốc độ đi chéo
        if (vx != 0 && vy != 0) {
            vx *= DIAGONAL_FACTOR;
            vy *= DIAGONAL_FACTOR;
        }

        worldX += vx;
        worldY += vy;

        clampPlayerPosition();

        // NẾU CÓ SÚNG THÌ MỚI UPDATE LOGIC NGẮM BẮN
        if (currentWeapon != null) {
            currentWeapon.update();
        }
    }

    public void clampPlayerPosition() {
        int playerSize = 80;
        if (worldX < 0) worldX = 0;
        if (worldX + playerSize > gp.worldWidth) worldX = gp.worldWidth - playerSize;
        if (worldY < 0) worldY = 0;
        if (worldY + playerSize > gp.worldHeight) worldY = gp.worldHeight - playerSize;
    }

    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int) (worldX - cameraX);
        int screenY = (int) (worldY - cameraY);

        BufferedImage img = down1;
        switch (direction) {
            case "up": img = (spriteNum == 1) ? up1 : up2; break;
            case "down": img = (spriteNum == 1) ? down1 : down2; break;
            case "left": img = (spriteNum == 1) ? left1 : left2; break;
            case "right": img = (spriteNum == 1) ? right1 : right2; break;
        }

        g2.drawImage(img, screenX, screenY, 90, 90, null);


        // 3. NẾU CÓ SÚNG THÌ MỚI VẼ SÚNG VÀ VẼ ĐẠN
        if (currentWeapon != null) {
            currentWeapon.draw(g2, screenX, screenY, cameraX, cameraY);
            drawHealthBar(g2, screenX, screenY - 16, 80, 14);
        }
    }

    private void drawHealthBar(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(Color.RED);
        g2.fillRect(x, y, width, height);
        g2.setColor(Color.GREEN);
        int healthWidth = (int) ((double) health / maxHealth * width);
        g2.fillRect(x, y, healthWidth, height);
        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, width, height);

        String text = health + "/" + maxHealth;
        g2.setFont(HEALTH_FONT);
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (width - fm.stringWidth(text)) / 2;
        int ty = y + (height + fm.getAscent()) / 2 - 2;

        g2.setColor(Color.BLACK);
        g2.drawString(text, tx - 1, ty); g2.drawString(text, tx + 1, ty);
        g2.drawString(text, tx, ty - 1); g2.drawString(text, tx, ty + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(text, tx, ty);
    }
}