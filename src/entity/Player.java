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

    public BufferedImage playerImage;
    private int shootCooldown = 0;
    private final int shootInterval = 30;

    private static final double DIAGONAL_FACTOR = 1.0 / Math.sqrt(2);
    private static final BasicStroke AIM_STROKE = new BasicStroke(2);
    private static final Color BULLET_COLOR = new Color(0, 80, 200); // Xanh nước biển đậm
    private static final Font HEALTH_FONT = new Font("Arial", Font.BOLD, 11);

    // Constructor: Khởi tạo Player với GamePanel, KeyHandler và MouseHandler
    public Player(GamePanel gp, KeyHandler keyH, MouseHandler mouseH) {
        this.gp = gp;
        this.keyH = keyH;
        this.mouseH = mouseH;

        setDefaultValues();
        getPlayerImage();
    }

    // Thiết lập giá trị mặc định cho Player
    public void setDefaultValues() {
        worldX = 1000;
        worldY = 1000;
        speed = 4.5;
        aimAngle = 0;
        maxHealth = 300;
        health = maxHealth;
        direction = "down";
    }

    // Tải hình ảnh của Player
    public void getPlayerImage() {
        try {
            up1 = ImageIO.read(getClass().getResourceAsStream("/player/up1.png"));
            up1 = ImageIO.read(getClass().getResourceAsStream("/player/up1.png"));


            down1 = ImageIO.read(getClass().getResourceAsStream("/player/down1.png"));
            down2 = ImageIO.read(getClass().getResourceAsStream("/player/down2.png"));

            left1 = ImageIO.read(getClass().getResourceAsStream("/player/left1.png"));
            left2 = ImageIO.read(getClass().getResourceAsStream("/player/left2.png"));

            right1 = ImageIO.read(getClass().getResourceAsStream("/player/right1.png"));
            right2 = ImageIO.read(getClass().getResourceAsStream("/player/right2.png"));
            } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    // Cập nhật trạng thái của Player mỗi frame
    public void update() {
        vx = 0;
        vy = 0;

        if (keyH.upPressed) {
            vy -= speed;
            direction = "up";
        }
        if (keyH.downPressed) {
            vy += speed;
            direction = "down";
        }
        if (keyH.leftPressed) {
            vx -= speed;
            direction = "left";
        }
        if (keyH.rightPressed) {
            vx += speed;
            direction = "right";
        }
        spriteCounter++;
        if (spriteCounter > 100) {
            if (spriteNum == 1) {
                spriteNum = 2;
            }
            else if (spriteNum == 2) {
                spriteNum = 1;
            }
        }
        // Normalize diagonal: giữ tốc độ bằng nhau mọi hướng
        if (vx != 0 && vy != 0) {
            vx *= DIAGONAL_FACTOR;
            vy *= DIAGONAL_FACTOR;
        }

        // worldX/worldY là double → cộng trực tiếp vận tốc, di chuyển subpixel mượt mà
        worldX += vx;
        worldY += vy;

        // Giới hạn vị trí nhân vật trong map
        clampPlayerPosition();

        // Ngắm theo vị trí chuột: phải tính theo camera đã clamp, vì khi player ở rìa map
        // camera đứng yên còn player dịch sang rìa screen → tâm player KHÔNG còn ở giữa screen
        int rawCamX = (int) (worldX - gp.screenWidth / 2.0);
        int rawCamY = (int) (worldY - gp.screenHeight / 2.0);
        int[] cam = gp.clampCameraPosition(rawCamX, rawCamY);
        int playerCenterScreenX = (int) (worldX - cam[0]) + 40;
        int playerCenterScreenY = (int) (worldY - cam[1]) + 40;
        double dx = mouseH.mouseX - playerCenterScreenX;
        double dy = mouseH.mouseY - playerCenterScreenY;
        aimAngle = Math.toDegrees(Math.atan2(dy, dx));

        // Bắn liên tục theo hướng chuột
        shootCooldown++;
        if (shootCooldown >= shootInterval) {
            shoot();
            shootCooldown = 0;
        }

        // Update bullets
        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).update();
            if (bullets.get(i).isOutOfRange()) {
                bullets.remove(i);
                i--;
            }
        }
    }

    // Giới hạn vị trí nhân vật không cho phép vượt ra ngoài map
    private void clampPlayerPosition() {
        // Player size is 80x80
        int playerSize = 80;

        // Clamp X position
        if (worldX < 0) {
            worldX = 0;
        }
        if (worldX + playerSize > gp.worldWidth) {
            worldX = gp.worldWidth - playerSize;
        }

        // Clamp Y position
        if (worldY < 0) {
            worldY = 0;
        }
        if (worldY + playerSize > gp.worldHeight) {
            worldY = gp.worldHeight - playerSize;
        }
    }

    // Bắn đạn nếu cooldown cho phép
    public void shoot() {
        // Create bullet at player position
        Bullet bullet = new Bullet(worldX + 40, worldY + 40, aimAngle);
        bullet.color = BULLET_COLOR;
        bullets.add(bullet);
    }

    // Vẽ Player tại vị trí thực trên screen (theo camera đã clamp)
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int) (worldX - cameraX);
        int screenY = (int) (worldY - cameraY);

        BufferedImage img = null;
        switch (direction) {
            case "up":
                if (spriteNum == 1) {
                    img = up1;
                }
                if (spriteNum == 2) {
                    img = up2;
                }
                break;
            case "down":
                if (spriteNum == 1) {
                    img = down1;
                }
                if (spriteNum == 2) {
                    img = down2;
                }
                break;
            case "left":
                if (spriteNum == 1) {
                    img = left1;
                }
                if (spriteNum == 2) {
                    img = left2;
                }
                break;
            case "right":
                if (spriteNum == 1) {
                    img = right1;
                }
                if (spriteNum == 2) {
                    img = right2;
                }
                break;

        }
        g2.drawImage(img, screenX, screenY, 80, 80, null);

        drawHealthBar(g2, screenX, screenY - 16, 80, 14);
        drawAimingIndicator(g2, screenX + 40, screenY + 40);

        for (Bullet bullet : bullets) {
            bullet.draw(g2, cameraX, cameraY);
        }
    }

    // Vẽ thanh máu kèm số máu hiện tại / tối đa (vd "175/200")
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
        // Vẽ stroke đen mỏng để text luôn đọc được dù background đỏ hay xanh
        g2.setColor(Color.BLACK);
        g2.drawString(text, tx - 1, ty);
        g2.drawString(text, tx + 1, ty);
        g2.drawString(text, tx, ty - 1);
        g2.drawString(text, tx, ty + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(text, tx, ty);
    }

    // Vẽ chỉ báo hướng nhắm
    private void drawAimingIndicator(Graphics2D g2, int centerX, int centerY) {
        int indicatorLength = 30;
        double radians = Math.toRadians(aimAngle);
        int endX = (int)(centerX + indicatorLength * Math.cos(radians));
        int endY = (int)(centerY + indicatorLength * Math.sin(radians));

        g2.setColor(Color.RED);
        g2.setStroke(AIM_STROKE);
        g2.drawLine(centerX, centerY, endX, endY);
    }
}