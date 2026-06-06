package entity;

import main.GamePanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

// Lớp đại diện cho enemy trong game (Đã rút gọn còn 1 loại)
public class Enemy extends Entity {
    GamePanel gp;
    Player player;

    // --- 8 BIẾN HÌNH ẢNH CHO 4 HƯỚNG ---
    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    public BufferedImage currentImage;

    // --- BIẾN ANIMATION CHUYỂN FRAME ---
    public int spriteCounter = 0;
    public int spriteNum = 1;

    private int shootCooldown = 0;
    private final int shootInterval = 60; // Bắn mỗi 60 frames
    public double minDistance = 150; // Khoảng cách tối thiểu dừng lại giữ khoảng cách với Player
    public boolean canDodge = true;
    public int damage = 1;
    public int strafeDir = (Math.random() < 0.5) ? 1 : -1; // Hướng đi ngang lượn lờ

    public String enemyDirection;

    // Constructor: Khởi tạo Enemy với vị trí ban đầu (Đã bỏ tham số enemyType)
    public Enemy(GamePanel gp, Player player, int startX, int startY) {
        this.gp = gp;
        this.player = player;
        worldX = startX;
        worldY = startY;
        speed = 5.0; // Tốc độ di chuyển
        aimAngle = 0;
        health = maxHealth; // Đặt máu ban đầu
        enemyDirection = "down"; // Hướng mặc định ban đầu

        getEnemyImage();
    }

    // Nạp ảnh cho 1 loại quái duy nhất
    public void getEnemyImage() {
        try {
            // MÀY ĐIỀN TÊN FILE GỐC CỦA MÀY VÀO ĐÂY (Ví dụ: "/enemy/quai_vat")
            String base = "/enemy/ds";

            up1 = ImageIO.read(getClass().getResourceAsStream(base + "_up1.png"));
            up2 = ImageIO.read(getClass().getResourceAsStream(base + "_up2.png"));
            down1 = ImageIO.read(getClass().getResourceAsStream(base + "_down1.png"));
            down2 = ImageIO.read(getClass().getResourceAsStream(base + "_down2.png"));
            left1 = ImageIO.read(getClass().getResourceAsStream(base + "_left1.png"));
            left2 = ImageIO.read(getClass().getResourceAsStream(base + "_left2.png"));
            right1 = ImageIO.read(getClass().getResourceAsStream(base + "_right1.png"));
            right2 = ImageIO.read(getClass().getResourceAsStream(base + "_right2.png"));

            // Đặt hình mặc định khi spawn
            currentImage = down1;
        } catch (Exception e) {
            System.out.println("LỖI: Không thể nạp ảnh cho Enemy!");
            e.printStackTrace();
        }
    }

    // Cập nhật trạng thái của Enemy mỗi frame
    public void update() {
        double dx = player.worldX - worldX;
        double dy = player.worldY - worldY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Khởi tạo vx, vy bằng 0 mỗi frame
        vx = 0;
        vy = 0;

        // --- AI CƠ BẢN CHO QUÁI ---
        if (distance > minDistance) {
            // Nếu ở xa thì tiến thẳng về phía Player
            vx = (dx / distance) * speed;
            vy = (dy / distance) * speed;
        } else {
            // Nếu tới gần thì lượn lờ đi ngang giữ vị trí
            double nx = dx / distance;
            double ny = dy / distance;

            double tangentX = -ny * strafeDir;
            double tangentY = nx * strafeDir;

            vx = tangentX * (speed * 0.7);
            vy = tangentY * (speed * 0.7);
        }

        // --- LOGIC NÉ ĐẠN CỦA PLAYER ---
        if (canDodge && player.currentWeapon != null) {
            for (Bullet b : player.currentWeapon.bullets) {
                double bdx = worldX - b.worldX;
                double bdy = worldY - b.worldY;
                double bDist = Math.sqrt(bdx * bdx + bdy * bdy);

                if (bDist < 150) {
                    double px = -b.vy;
                    double py = b.vx;

                    if (bdx * px + bdy * py < 0) {
                        px = -px;
                        py = -py;
                    }

                    double pLen = Math.sqrt(px * px + py * py);
                    if (pLen > 0) {
                        px /= pLen;
                        py /= pLen;
                        double dodgeSpeed = speed * 1.5;
                        vx = px * dodgeSpeed;
                        vy = py * dodgeSpeed;
                        break;
                    }
                }
            }
        }

        // --- LOGIC ĐẨY NHAU (Tránh quái đứng đè lên nhau) ---
        double repX = 0;
        double repY = 0;
        for (Enemy other : gp.enemies) {
            if (other != this) {
                double odx = worldX - other.worldX;
                double ody = worldY - other.worldY;
                double odist = Math.sqrt(odx * odx + ody * ody);
                if (odist > 0 && odist < 100) {
                    repX += (odx / odist) * (100 - odist) * 0.15;
                    repY += (ody / odist) * (100 - odist) * 0.15;
                }
            }
        }
        vx += repX;
        vy += repY;

        // --- XÁC ĐỊNH HƯỚNG QUAY MẶT THEO VẬN TỐC ---
        if (Math.abs(vx) > Math.abs(vy)) {
            if (vx > 0) {
                enemyDirection = "right";
            } else if (vx < 0) {
                enemyDirection = "left";
            }
        } else {
            if (vy > 0) {
                enemyDirection = "down";
            } else if (vy < 0) {
                enemyDirection = "up";
            }
        }

        // --- XỬ LÝ ANIMATION HOẠT ẢNH DI CHUYỂN ---
        spriteCounter++;
        if (spriteCounter > 12) { // 12 frame đổi ảnh 1 lần
            if (spriteNum == 1) {
                spriteNum = 2;
            } else if (spriteNum == 2) {
                spriteNum = 1;
            }
            spriteCounter = 0;
        }

        // Gán ảnh dựa theo hướng và frame hiện tại
        switch (enemyDirection) {
            case "up":
                currentImage = (spriteNum == 1) ? up1 : up2;
                break;
            case "down":
                currentImage = (spriteNum == 1) ? down1 : down2;
                break;
            case "left":
                currentImage = (spriteNum == 1) ? left1 : left2;
                break;
            case "right":
                currentImage = (spriteNum == 1) ? right1 : right2;
                break;
        }

        // --- CẬP NHẬT TỌA ĐỘ VÀ GIỚI HẠN BẢN ĐỒ ---
        aimAngle = Math.toDegrees(Math.atan2(dy, dx));
        worldX += vx;
        worldY += vy;

        worldX = Math.max(0, Math.min(worldX, gp.worldWidth - 80));
        worldY = Math.max(0, Math.min(worldY, gp.worldHeight - 80));

        // --- CẬP NHẬT ĐẠN ---
        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).update();
            if (bullets.get(i).isOutOfRange()) {
                bullets.remove(i);
                i--;
            }
        }

        // --- BẮN THEO CHU KỲ ---
        shootCooldown++;
        if (shootCooldown >= shootInterval) {
            shoot();
            shootCooldown = 0;
        }
    }

    public void shoot() {
        Bullet bullet = new Bullet(worldX + 40, worldY + 40, aimAngle);
        bullets.add(bullet);
    }

    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int) (worldX - cameraX);
        int screenY = (int) (worldY - cameraY);

        // Chỉ vẽ khi nằm trong khung hình camera
        if (screenX > -80 && screenX < gp.screenWidth + 80 && screenY > -80 && screenY < gp.screenHeight + 80) {
            g2.drawImage(currentImage, screenX, screenY, 100, 100, null);
            drawHealthBar(g2, screenX, screenY - 10, 80, 10);
        }

        for (Bullet bullet : bullets) {
            bullet.draw(g2, cameraX, cameraY);
        }
    }

    private void drawHealthBar(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(Color.RED);
        g2.fillRect(x, y, width, height);
        g2.setColor(Color.GREEN);
        int healthWidth = (int)((double)health / maxHealth * width);
        g2.fillRect(x, y, healthWidth, height);
        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, width, height);
    }
}